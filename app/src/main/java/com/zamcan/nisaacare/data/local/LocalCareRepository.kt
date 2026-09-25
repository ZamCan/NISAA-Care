package com.zamcan.nisaacare.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.zamcan.nisaacare.core.security.SecureValueProtector
import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.AppPreferences
import com.zamcan.nisaacare.domain.model.ContentVersion
import com.zamcan.nisaacare.domain.model.CycleRecord
import com.zamcan.nisaacare.domain.model.DomainError
import com.zamcan.nisaacare.domain.model.DomainResult
import com.zamcan.nisaacare.domain.model.FertilityEstimate
import com.zamcan.nisaacare.domain.model.FlowIntensity
import com.zamcan.nisaacare.domain.model.MaritalStatus
import com.zamcan.nisaacare.domain.model.PairingInvitation
import com.zamcan.nisaacare.domain.model.PermissionKey
import com.zamcan.nisaacare.domain.model.Relationship
import com.zamcan.nisaacare.domain.model.RelationshipPermissions
import com.zamcan.nisaacare.domain.model.RelationshipStatus
import com.zamcan.nisaacare.domain.model.SupportRequest
import com.zamcan.nisaacare.domain.model.UserProfile
import com.zamcan.nisaacare.domain.model.UserRole
import com.zamcan.nisaacare.domain.relationship.RelationshipPolicy
import java.time.Instant
import java.time.LocalDate

class LocalCareRepository(context: Context) {
    private val database = NisaaDatabase(context.applicationContext)
    private val protector = SecureValueProtector()
    private val relationshipPolicy = RelationshipPolicy()

    fun close() = database.close()

    fun getProfile(): UserProfile? {
        val cursor = database.readableDatabase.query(
            "profiles", null, null, null, null, null, "updated_at DESC"
        )
        return cursor.use { if (it.moveToFirst()) readProfile(it) else null }
    }

    fun saveProfile(profile: UserProfile): DomainResult<UserProfile> {
        val existing = getProfile()
        val values = database.profileValues(
            id = profile.id,
            displayName = profile.displayName.trim(),
            language = profile.language,
            role = profile.role,
            maritalStatus = profile.maritalStatus,
            averageCycleLength = profile.averageCycleLength,
            cycleStartDate = profile.cycleStartDate,
            onboardingComplete = profile.onboardingComplete,
            onboardingStep = profile.onboardingStep,
            now = profile.updatedAt,
            createdAt = existing?.takeIf { it.id == profile.id }?.createdAt ?: profile.createdAt
        )
        val db = database.writableDatabase
        val updated = db.update("profiles", values, "id = ?", arrayOf(profile.id))
        if (updated == 0) {
            val inserted = db.insertOrThrow("profiles", null, values)
            if (inserted == -1L) return databaseFailure("PROFILE_WRITE_FAILED")
        }
        return DomainResult.Success(profile)
    }

    fun getCycles(userId: String): List<CycleRecord> {
        val cursor = database.readableDatabase.query(
            "cycles",
            null,
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            "start_date DESC"
        )
        return cursor.use {
            buildList {
                while (it.moveToNext()) add(readCycle(it))
            }
        }
    }

    fun saveCycle(record: CycleRecord): DomainResult<CycleRecord> {
        val encryptedNotes = if (record.notes.isBlank()) "" else runCatching {
            protector.encrypt(record.notes)
        }.getOrElse { "" }
        if (record.notes.isNotBlank() && encryptedNotes.isBlank()) {
            return databaseFailure("SECURE_VALUE_WRITE_FAILED")
        }
        val values = database.cycleValues(
            id = record.id,
            userId = record.userId,
            startDate = record.startDate,
            endDate = record.endDate,
            flow = record.flow,
            symptoms = record.symptoms,
            notesCiphertext = encryptedNotes,
            source = record.source,
            now = record.updatedAt
        )
        val db = database.writableDatabase
        val existing = db.query(
            "cycles",
            arrayOf("created_at"),
            "id = ? AND user_id = ?",
            arrayOf(record.id, record.userId),
            null,
            null,
            null
        ).use { if (it.moveToFirst()) it.getLong(0) else null }
        if (existing != null) values.remove("created_at")
        val updated = db.update("cycles", values, "id = ? AND user_id = ?", arrayOf(record.id, record.userId))
        if (updated == 0 && db.insertOrThrow("cycles", null, values) == -1L) {
            return databaseFailure("CYCLE_WRITE_FAILED")
        }
        return DomainResult.Success(record)
    }

    fun getLatestFertility(userId: String): FertilityEstimate? {
        val cursor = database.readableDatabase.query(
            "fertility_estimates",
            null,
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            "generated_at DESC",
            "1"
        )
        return cursor.use { if (it.moveToFirst()) readFertility(it) else null }
    }

    fun saveFertility(estimate: FertilityEstimate): DomainResult<FertilityEstimate> {
        val values = database.fertilityValues(
            id = estimate.id,
            userId = estimate.userId,
            referenceDate = estimate.referenceDate,
            predictedStart = estimate.predictedPeriodStart,
            fertileStart = estimate.fertileWindowStart,
            fertileEnd = estimate.fertileWindowEnd,
            ovulation = estimate.ovulationEstimate,
            averageLength = estimate.averageCycleLength,
            variability = estimate.variabilityDays,
            confidence = estimate.confidence.name,
            calculationVersion = estimate.calculationVersion
        )
        val inserted = database.writableDatabase.insertWithOnConflict(
            "fertility_estimates",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        return if (inserted == -1L) databaseFailure("FERTILITY_WRITE_FAILED")
        else DomainResult.Success(estimate)
    }

    fun getPreferences(userId: String): AppPreferences {
        val cursor = database.readableDatabase.query(
            "preferences", null, "user_id = ?", arrayOf(userId), null, null, null, "1"
        )
        return cursor.use {
            if (it.moveToFirst()) {
                AppPreferences(
                    userId = userId,
                    notificationsEnabled = it.int("notifications_enabled") == 1,
                    notificationDetailsEnabled = it.int("notification_details_enabled") == 1,
                    cycleRemindersEnabled = it.int("cycle_reminders_enabled") == 1,
                    contentRemindersEnabled = it.int("content_reminders_enabled") == 1,
                    relationshipRemindersEnabled = it.int("relationship_reminders_enabled") == 1,
                    hapticsEnabled = it.int("haptics_enabled") == 1,
                    preferredContentLanguage = AppLanguage.fromCode(it.string("preferred_content_language"))
                )
            } else {
                AppPreferences(userId = userId)
            }
        }
    }

    fun savePreferences(preferences: AppPreferences): DomainResult<AppPreferences> {
        val values = ContentValues().apply {
            put("user_id", preferences.userId)
            put("notifications_enabled", boolValue(preferences.notificationsEnabled))
            put("notification_details_enabled", boolValue(preferences.notificationDetailsEnabled))
            put("cycle_reminders_enabled", boolValue(preferences.cycleRemindersEnabled))
            put("content_reminders_enabled", boolValue(preferences.contentRemindersEnabled))
            put("relationship_reminders_enabled", boolValue(preferences.relationshipRemindersEnabled))
            put("haptics_enabled", boolValue(preferences.hapticsEnabled))
            put("preferred_content_language", preferences.preferredContentLanguage.code)
        }
        val db = database.writableDatabase
        val updated = db.update("preferences", values, "user_id = ?", arrayOf(preferences.userId))
        if (updated == 0 && db.insertOrThrow("preferences", null, values) == -1L) {
            return databaseFailure("PREFERENCES_WRITE_FAILED")
        }
        return DomainResult.Success(preferences)
    }

    fun getRelationships(userId: String): List<Relationship> {
        val cursor = database.readableDatabase.query(
            "relationships",
            null,
            "(husband_user_id = ? OR wife_user_id = ?)",
            arrayOf(userId, userId),
            null,
            null,
            "created_at DESC"
        )
        return cursor.use {
            buildList {
                while (it.moveToNext()) add(readRelationship(it))
            }
        }
    }

    fun createRelationship(
        candidate: Relationship,
        currentUserId: String
    ): DomainResult<Relationship> {
        val validation = relationshipPolicy.validateNewRelationship(
            candidate,
            getRelationships(currentUserId),
            currentUserId
        )
        if (validation is DomainResult.Failure) return validation
        val relationship = (validation as DomainResult.Success).value
        return saveRelationship(relationship)
    }

    fun saveRelationship(relationship: Relationship): DomainResult<Relationship> {
        val values = ContentValues().apply {
            put("id", relationship.id)
            if (relationship.husbandUserId == null) putNull("husband_user_id") else put("husband_user_id", relationship.husbandUserId)
            if (relationship.wifeUserId == null) putNull("wife_user_id") else put("wife_user_id", relationship.wifeUserId)
            put("status", relationship.status.name)
            if (relationship.connectionToken == null) putNull("connection_token") else put("connection_token", relationship.connectionToken)
            put("permissions", encodePermissions(relationship.permissions.permissions))
            put("permissions_version", relationship.permissions.version)
            put("created_at", relationship.createdAt.toEpochMilli())
            if (relationship.revokedAt == null) putNull("revoked_at") else put("revoked_at", relationship.revokedAt.toEpochMilli())
            put("sync_version", relationship.syncVersion)
        }
        val db = database.writableDatabase
        val updated = db.update("relationships", values, "id = ?", arrayOf(relationship.id))
        if (updated == 0 && db.insertOrThrow("relationships", null, values) == -1L) {
            return databaseFailure("RELATIONSHIP_WRITE_FAILED")
        }
        return DomainResult.Success(relationship)
    }

    fun updatePermission(
        relationship: Relationship,
        permission: PermissionKey,
        granted: Boolean
    ): DomainResult<Relationship> {
        val updated = relationshipPolicy.grantPermission(relationship, permission, granted)
        return saveRelationship(updated)
    }

    fun revokeRelationship(relationship: Relationship): DomainResult<Relationship> =
        saveRelationship(relationshipPolicy.revoke(relationship))

    fun saveInvitation(invitation: PairingInvitation): DomainResult<PairingInvitation> {
        val values = ContentValues().apply {
            put("token", invitation.token)
            put("inviter_user_id", invitation.inviterUserId)
            put("inviter_role", invitation.inviterRole.name)
            put("created_at", invitation.createdAt.toEpochMilli())
            put("expires_at", invitation.expiresAt.toEpochMilli())
            if (invitation.consumedAt == null) putNull("consumed_at") else put("consumed_at", invitation.consumedAt.toEpochMilli())
            if (invitation.revokedAt == null) putNull("revoked_at") else put("revoked_at", invitation.revokedAt.toEpochMilli())
        }
        val inserted = database.writableDatabase.insertWithOnConflict(
            "pairing_tokens",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        return if (inserted == -1L) databaseFailure("INVITATION_WRITE_FAILED")
        else DomainResult.Success(invitation)
    }

    fun findInvitation(token: String): PairingInvitation? {
        val cursor = database.readableDatabase.query(
            "pairing_tokens", null, "token = ?", arrayOf(token), null, null, null, "1"
        )
        return cursor.use {
            if (!it.moveToFirst()) return@use null
            PairingInvitation(
                token = it.string("token"),
                inviterUserId = it.string("inviter_user_id"),
                inviterRole = enumValueOrDefault(it.string("inviter_role"), UserRole.WOMAN),
                createdAt = Instant.ofEpochMilli(it.long("created_at")),
                expiresAt = Instant.ofEpochMilli(it.long("expires_at")),
                consumedAt = it.nullableInstantFromMillis("consumed_at"),
                revokedAt = it.nullableInstantFromMillis("revoked_at")
            )
        }
    }

    fun markInvitationConsumed(token: String, now: Instant = Instant.now()): Boolean {
        val values = ContentValues().apply { put("consumed_at", now.toEpochMilli()) }
        return database.writableDatabase.update("pairing_tokens", values, "token = ?", arrayOf(token)) > 0
    }

    fun addSupportRequest(request: SupportRequest): DomainResult<SupportRequest> {
        val encryptedMessage = runCatching { protector.encrypt(request.message) }.getOrElse { "" }
        if (request.message.isNotBlank() && encryptedMessage.isBlank()) {
            return databaseFailure("SECURE_VALUE_WRITE_FAILED")
        }
        val values = ContentValues().apply {
            put("id", request.id)
            put("relationship_id", request.relationshipId)
            put("sender_user_id", request.senderUserId)
            put("message_ciphertext", encryptedMessage)
            put("status", request.status)
            put("created_at", request.createdAt.toEpochMilli())
        }
        val inserted = database.writableDatabase.insertOrThrow("support_requests", null, values)
        return if (inserted == -1L) databaseFailure("SUPPORT_REQUEST_WRITE_FAILED")
        else DomainResult.Success(request)
    }

    fun getContentVersion(): ContentVersion {
        val cursor = database.readableDatabase.query(
            "content_versions", null, null, null, null, null, null, "1"
        )
        return cursor.use {
            if (it.moveToFirst()) {
                ContentVersion(
                    packageName = it.string("package_name"),
                    version = it.int("version"),
                    installedAt = Instant.ofEpochMilli(it.long("installed_at")),
                    signatureVerified = it.int("signature_verified") == 1
                )
            } else {
                ContentVersion()
            }
        }
    }

    fun saveContentVersion(version: ContentVersion): DomainResult<ContentVersion> {
        val values = ContentValues().apply {
            put("package_name", version.packageName)
            put("version", version.version)
            put("installed_at", version.installedAt.toEpochMilli())
            put("signature_verified", boolValue(version.signatureVerified))
        }
        val inserted = database.writableDatabase.insertWithOnConflict(
            "content_versions", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        return if (inserted == -1L) databaseFailure("CONTENT_VERSION_WRITE_FAILED")
        else DomainResult.Success(version)
    }

    private fun readProfile(cursor: Cursor): UserProfile = UserProfile(
        id = cursor.string("id"),
        displayName = cursor.string("display_name"),
        language = AppLanguage.fromCode(cursor.string("language")),
        role = enumValueOrDefault(cursor.string("role"), UserRole.WOMAN),
        maritalStatus = enumValueOrDefault(cursor.string("marital_status"), MaritalStatus.SINGLE),
        averageCycleLength = cursor.nullableInt("average_cycle_length"),
        cycleStartDate = cursor.nullableDate("cycle_start_date"),
        onboardingComplete = cursor.int("onboarding_complete") == 1,
        onboardingStep = cursor.int("onboarding_step"),
        createdAt = Instant.ofEpochMilli(cursor.long("created_at")),
        updatedAt = Instant.ofEpochMilli(cursor.long("updated_at"))
    )

    private fun readCycle(cursor: Cursor): CycleRecord {
        val encryptedNotes = cursor.string("notes_ciphertext")
        val notes = if (encryptedNotes.isBlank()) "" else runCatching { protector.decrypt(encryptedNotes) }.getOrElse { "" }
        val symptoms = cursor.string("symptoms").split('|').filter { it.isNotBlank() }
        return CycleRecord(
            id = cursor.string("id"),
            userId = cursor.string("user_id"),
            startDate = LocalDate.parse(cursor.string("start_date")),
            endDate = cursor.nullableDate("end_date"),
            flow = enumValueOrDefault(cursor.string("flow"), FlowIntensity.UNKNOWN),
            symptoms = symptoms,
            notes = notes,
            source = cursor.string("source"),
            createdAt = Instant.ofEpochMilli(cursor.long("created_at")),
            updatedAt = Instant.ofEpochMilli(cursor.long("updated_at"))
        )
    }

    private fun readFertility(cursor: Cursor): FertilityEstimate = FertilityEstimate(
        id = cursor.string("id"),
        userId = cursor.string("user_id"),
        referenceDate = LocalDate.parse(cursor.string("reference_date")),
        predictedPeriodStart = cursor.nullableDate("predicted_period_start"),
        fertileWindowStart = cursor.nullableDate("fertile_window_start"),
        fertileWindowEnd = cursor.nullableDate("fertile_window_end"),
        ovulationEstimate = cursor.nullableDate("ovulation_estimate"),
        averageCycleLength = cursor.nullableInt("average_cycle_length"),
        variabilityDays = cursor.nullableInt("variability_days"),
        confidence = enumValueOrDefault(cursor.string("confidence"), com.zamcan.nisaacare.domain.model.Confidence.INSUFFICIENT_DATA),
        calculationVersion = cursor.string("calculation_version"),
        generatedAt = Instant.ofEpochMilli(cursor.long("generated_at"))
    )

    private fun readRelationship(cursor: Cursor): Relationship {
        val enabled = cursor.string("permissions").split(',').filter { it.isNotBlank() }.toSet()
        val permissions = PermissionKey.entries.associateWith { it.name in enabled }
        val relationshipId = cursor.string("id")
        return Relationship(
            id = relationshipId,
            husbandUserId = cursor.nullableString("husband_user_id"),
            wifeUserId = cursor.nullableString("wife_user_id"),
            status = enumValueOrDefault(cursor.string("status"), RelationshipStatus.PENDING),
            connectionToken = cursor.nullableString("connection_token"),
            permissions = RelationshipPermissions(
                relationshipId = relationshipId,
                permissions = permissions,
                version = cursor.long("permissions_version"),
                updatedAt = Instant.ofEpochMilli(cursor.long("created_at"))
            ),
            createdAt = Instant.ofEpochMilli(cursor.long("created_at")),
            revokedAt = cursor.nullableInstantFromMillis("revoked_at"),
            syncVersion = cursor.long("sync_version")
        )
    }

    private fun encodePermissions(permissions: Map<PermissionKey, Boolean>): String =
        permissions.filterValues { it }.keys.joinToString(",") { it.name }

    private fun boolValue(value: Boolean): Int = if (value) 1 else 0

    private fun Cursor.string(name: String): String = getString(getColumnIndexOrThrow(name)) ?: ""
    private fun Cursor.nullableString(name: String): String? {
        val index = getColumnIndexOrThrow(name)
        return if (isNull(index)) null else getString(index)
    }
    private fun Cursor.int(name: String): Int = getInt(getColumnIndexOrThrow(name))
    private fun Cursor.long(name: String): Long = getLong(getColumnIndexOrThrow(name))
    private fun Cursor.nullableInt(name: String): Int? {
        val index = getColumnIndexOrThrow(name)
        return if (isNull(index)) null else getInt(index)
    }
    private fun Cursor.nullableDate(name: String): LocalDate? = nullableString(name)?.let(LocalDate::parse)
    private fun Cursor.nullableInstant(name: String): Instant? = nullableString(name)?.let { Instant.parse(it) }
    private fun Cursor.nullableInstantFromMillis(name: String): Instant? {
        val index = getColumnIndexOrThrow(name)
        return if (isNull(index)) null else Instant.ofEpochMilli(getLong(index))
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, default: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(default)

    private fun <T> databaseFailure(code: String): DomainResult<T> =
        DomainResult.Failure(DomainError(code, "Local database operation failed"))
}
