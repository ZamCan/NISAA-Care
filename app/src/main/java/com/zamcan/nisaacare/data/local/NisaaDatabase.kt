package com.zamcan.nisaacare.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.FlowIntensity
import com.zamcan.nisaacare.domain.model.MaritalStatus
import com.zamcan.nisaacare.domain.model.UserRole
import java.time.Instant
import java.time.LocalDate

class NisaaDatabase(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
        db.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE profiles (
                id TEXT PRIMARY KEY NOT NULL,
                display_name TEXT NOT NULL DEFAULT '',
                language TEXT NOT NULL,
                role TEXT NOT NULL,
                marital_status TEXT NOT NULL,
                average_cycle_length INTEGER,
                cycle_start_date TEXT,
                onboarding_complete INTEGER NOT NULL DEFAULT 0,
                onboarding_step INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE cycles (
                id TEXT PRIMARY KEY NOT NULL,
                user_id TEXT NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT,
                flow TEXT NOT NULL,
                symptoms TEXT NOT NULL DEFAULT '',
                notes_ciphertext TEXT NOT NULL DEFAULT '',
                source TEXT NOT NULL DEFAULT 'MANUAL',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES profiles(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX cycles_user_start ON cycles(user_id, start_date DESC)")
        db.execSQL(
            """
            CREATE TABLE fertility_estimates (
                id TEXT PRIMARY KEY NOT NULL,
                user_id TEXT NOT NULL,
                reference_date TEXT NOT NULL,
                predicted_period_start TEXT,
                fertile_window_start TEXT,
                fertile_window_end TEXT,
                ovulation_estimate TEXT,
                average_cycle_length INTEGER,
                variability_days INTEGER,
                confidence TEXT NOT NULL,
                calculation_version TEXT NOT NULL,
                generated_at INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES profiles(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE islamic_content (
                id TEXT PRIMARY KEY NOT NULL,
                content_type TEXT NOT NULL,
                topic_key TEXT NOT NULL,
                source TEXT NOT NULL,
                reference TEXT NOT NULL,
                methodology TEXT,
                review_state TEXT NOT NULL,
                version INTEGER NOT NULL,
                translations TEXT NOT NULL DEFAULT '',
                audience TEXT NOT NULL DEFAULT 'GENERAL',
                season TEXT NOT NULL DEFAULT 'EVERGREEN',
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE quotations (
                id TEXT PRIMARY KEY NOT NULL,
                translation_group_id TEXT NOT NULL,
                source_type TEXT NOT NULL,
                source TEXT NOT NULL,
                reference TEXT NOT NULL,
                author TEXT,
                topic TEXT NOT NULL,
                intended_audience TEXT NOT NULL,
                rotation_class TEXT NOT NULL,
                season TEXT NOT NULL,
                review_state TEXT NOT NULL,
                content_version INTEGER NOT NULL,
                translations TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE relationships (
                id TEXT PRIMARY KEY NOT NULL,
                husband_user_id TEXT,
                wife_user_id TEXT,
                status TEXT NOT NULL,
                connection_token TEXT,
                permissions TEXT NOT NULL DEFAULT '',
                permissions_version INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL,
                revoked_at INTEGER,
                sync_version INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE pairing_tokens (
                token TEXT PRIMARY KEY NOT NULL,
                inviter_user_id TEXT NOT NULL,
                inviter_role TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                expires_at INTEGER NOT NULL,
                consumed_at INTEGER,
                revoked_at INTEGER
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE support_requests (
                id TEXT PRIMARY KEY NOT NULL,
                relationship_id TEXT NOT NULL,
                sender_user_id TEXT NOT NULL,
                message_ciphertext TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'OPEN',
                created_at INTEGER NOT NULL,
                FOREIGN KEY(relationship_id) REFERENCES relationships(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE preferences (
                user_id TEXT PRIMARY KEY NOT NULL,
                notifications_enabled INTEGER NOT NULL DEFAULT 0,
                notification_details_enabled INTEGER NOT NULL DEFAULT 0,
                cycle_reminders_enabled INTEGER NOT NULL DEFAULT 0,
                content_reminders_enabled INTEGER NOT NULL DEFAULT 0,
                relationship_reminders_enabled INTEGER NOT NULL DEFAULT 0,
                haptics_enabled INTEGER NOT NULL DEFAULT 1,
                preferred_content_language TEXT NOT NULL,
                FOREIGN KEY(user_id) REFERENCES profiles(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE content_versions (
                package_name TEXT PRIMARY KEY NOT NULL,
                version INTEGER NOT NULL,
                installed_at INTEGER NOT NULL,
                signature_verified INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 1) return
        // Versioned migrations are added here as the schema evolves. User health
        // records are never dropped or replaced by a content update.
    }

    fun profileValues(
        id: String,
        displayName: String,
        language: AppLanguage,
        role: UserRole,
        maritalStatus: MaritalStatus,
        averageCycleLength: Int?,
        cycleStartDate: LocalDate?,
        onboardingComplete: Boolean,
        onboardingStep: Int,
        now: Instant = Instant.now(),
        createdAt: Instant = now
    ): ContentValues = ContentValues().apply {
        put("id", id)
        put("display_name", displayName)
        put("language", language.code)
        put("role", role.name)
        put("marital_status", maritalStatus.name)
        if (averageCycleLength == null) putNull("average_cycle_length") else put("average_cycle_length", averageCycleLength)
        if (cycleStartDate == null) putNull("cycle_start_date") else put("cycle_start_date", cycleStartDate.toString())
        put("onboarding_complete", if (onboardingComplete) 1 else 0)
        put("onboarding_step", onboardingStep)
        put("created_at", now.toEpochMilli())
        put("updated_at", now.toEpochMilli())
    }

    fun cycleValues(
        id: String,
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        flow: FlowIntensity,
        symptoms: List<String>,
        notesCiphertext: String,
        source: String,
        now: Instant = Instant.now()
    ): ContentValues = ContentValues().apply {
        put("id", id)
        put("user_id", userId)
        put("start_date", startDate.toString())
        if (endDate == null) putNull("end_date") else put("end_date", endDate.toString())
        put("flow", flow.name)
        put("symptoms", symptoms.joinToString("|"))
        put("notes_ciphertext", notesCiphertext)
        put("source", source)
        put("created_at", now.toEpochMilli())
        put("updated_at", now.toEpochMilli())
    }

    fun fertilityValues(
        id: String,
        userId: String,
        referenceDate: LocalDate,
        predictedStart: LocalDate?,
        fertileStart: LocalDate?,
        fertileEnd: LocalDate?,
        ovulation: LocalDate?,
        averageLength: Int?,
        variability: Int?,
        confidence: String,
        calculationVersion: String,
        now: Instant = Instant.now()
    ): ContentValues = ContentValues().apply {
        put("id", id)
        put("user_id", userId)
        put("reference_date", referenceDate.toString())
        if (predictedStart == null) putNull("predicted_period_start") else put("predicted_period_start", predictedStart.toString())
        if (fertileStart == null) putNull("fertile_window_start") else put("fertile_window_start", fertileStart.toString())
        if (fertileEnd == null) putNull("fertile_window_end") else put("fertile_window_end", fertileEnd.toString())
        if (ovulation == null) putNull("ovulation_estimate") else put("ovulation_estimate", ovulation.toString())
        if (averageLength == null) putNull("average_cycle_length") else put("average_cycle_length", averageLength)
        if (variability == null) putNull("variability_days") else put("variability_days", variability)
        put("confidence", confidence)
        put("calculation_version", calculationVersion)
        put("generated_at", now.toEpochMilli())
    }

    companion object {
        const val DATABASE_NAME = "nisaa_care.db"
        const val DATABASE_VERSION = 1
    }
}
