package com.zamcan.nisaacare.domain.model

import com.zamcan.nisaacare.domain.cycle.BiologicalEngineVersion
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

enum class AppLanguage(val code: String) {
    SWAHILI("sw"),
    ENGLISH("en"),
    ARABIC("ar");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: SWAHILI
    }
}

enum class UserRole { WOMAN, HUSBAND }

enum class MaritalStatus { SINGLE, MARRIED, WIDOWED, DIVORCED, OTHER }

enum class FlowIntensity { NONE, LIGHT, MEDIUM, HEAVY, UNKNOWN }

enum class ReviewState { DRAFT, REVIEW_REQUIRED, VERIFIED, PUBLISHED, RETIRED }

enum class ContentType { HEALTH, ISLAMIC, EDUCATION, QUOTATION }

enum class RelationshipStatus { PENDING, ACTIVE, REVOKED, EXPIRED }

enum class PermissionKey {
    CYCLE_STATUS,
    FERTILITY_ESTIMATE,
    WELLBEING,
    SUPPORT_REQUESTS,
    EDUCATION
}

enum class ContentSeason { EVERGREEN, SEASONAL, RAMADAN, CONTEXTUAL }

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String = "",
    val language: AppLanguage = AppLanguage.SWAHILI,
    val role: UserRole = UserRole.WOMAN,
    val maritalStatus: MaritalStatus = MaritalStatus.SINGLE,
    val averageCycleLength: Int? = null,
    val cycleStartDate: LocalDate? = null,
    val onboardingComplete: Boolean = false,
    val onboardingStep: Int = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

data class CycleRecord(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val flow: FlowIntensity = FlowIntensity.UNKNOWN,
    val symptoms: List<String> = emptyList(),
    val notes: String = "",
    val source: String = "MANUAL",
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

data class FertilityEstimate(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val referenceDate: LocalDate,
    val predictedPeriodStart: LocalDate? = null,
    val fertileWindowStart: LocalDate? = null,
    val fertileWindowEnd: LocalDate? = null,
    val ovulationEstimate: LocalDate? = null,
    val averageCycleLength: Int? = null,
    val variabilityDays: Int? = null,
    val confidence: Confidence = Confidence.INSUFFICIENT_DATA,
    val calculationVersion: String = BiologicalEngineVersion.CURRENT,
    val generatedAt: Instant = Instant.now()
)

enum class Confidence {
    INSUFFICIENT_DATA,
    LOW,
    MODERATE,
    HIGHER_HISTORICAL_CONFIDENCE
}

data class ContentItem(
    val id: String,
    val contentType: ContentType,
    val topicKey: String,
    val source: String,
    val reference: String,
    val scholarlyMethodology: String? = null,
    val reviewState: ReviewState,
    val version: Int,
    val translations: Map<AppLanguage, String> = emptyMap(),
    val audience: String = "GENERAL",
    val season: ContentSeason = ContentSeason.EVERGREEN,
    val createdAt: Instant = Instant.now()
)

data class Quotation(
    val id: String,
    val translationGroupId: String,
    val sourceType: String,
    val source: String,
    val reference: String,
    val author: String? = null,
    val topic: String,
    val intendedAudience: String,
    val rotationClass: String,
    val season: ContentSeason = ContentSeason.EVERGREEN,
    val reviewState: ReviewState,
    val contentVersion: Int,
    val translations: Map<AppLanguage, String> = emptyMap()
)

data class RelationshipPermissions(
    val relationshipId: String,
    val permissions: Map<PermissionKey, Boolean> = PermissionKey.entries.associateWith { false },
    val version: Long = 1L,
    val updatedAt: Instant = Instant.now()
)

data class Relationship(
    val id: String = UUID.randomUUID().toString(),
    val husbandUserId: String? = null,
    val wifeUserId: String? = null,
    val status: RelationshipStatus = RelationshipStatus.PENDING,
    val connectionToken: String? = null,
    val permissions: RelationshipPermissions,
    val createdAt: Instant = Instant.now(),
    val revokedAt: Instant? = null,
    val syncVersion: Long = 1L
) {
    fun otherUserId(currentUserId: String): String? = when (currentUserId) {
        husbandUserId -> wifeUserId
        wifeUserId -> husbandUserId
        else -> null
    }
}

data class PairingInvitation(
    val token: String,
    val inviterUserId: String,
    val inviterRole: UserRole,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant,
    val consumedAt: Instant? = null,
    val revokedAt: Instant? = null
) {
    fun isUsable(now: Instant = Instant.now()): Boolean =
        consumedAt == null && revokedAt == null && now.isBefore(expiresAt)
}

data class SupportRequest(
    val id: String = UUID.randomUUID().toString(),
    val relationshipId: String,
    val senderUserId: String,
    val message: String,
    val status: String = "OPEN",
    val createdAt: Instant = Instant.now()
)

data class AppPreferences(
    val userId: String,
    val notificationsEnabled: Boolean = false,
    val notificationDetailsEnabled: Boolean = false,
    val cycleRemindersEnabled: Boolean = false,
    val contentRemindersEnabled: Boolean = false,
    val relationshipRemindersEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val preferredContentLanguage: AppLanguage = AppLanguage.SWAHILI
)

data class ContentVersion(
    val packageName: String = "bundled-content",
    val version: Int = 1,
    val installedAt: Instant = Instant.now(),
    val signatureVerified: Boolean = false
)

data class CycleInsight(
    val referenceDate: LocalDate,
    val observedCycleCount: Int,
    val averageCycleLength: Int?,
    val variabilityDays: Int?,
    val lastPeriodStart: LocalDate?,
    val predictedPeriodStart: LocalDate?,
    val fertileWindowStart: LocalDate?,
    val fertileWindowEnd: LocalDate?,
    val ovulationEstimate: LocalDate?,
    val confidence: Confidence,
    val calculationVersion: String = BiologicalEngineVersion.CURRENT,
    val guidance: String = "Estimates only; they cannot prevent pregnancy."
)

data class DomainError(val code: String, val message: String)

sealed class DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>()
    data class Failure(val error: DomainError) : DomainResult<Nothing>()

    fun valueOrNull(): T? = (this as? Success)?.value
}
