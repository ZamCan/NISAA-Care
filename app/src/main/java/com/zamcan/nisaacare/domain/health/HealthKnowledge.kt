package com.zamcan.nisaacare.domain.health

import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.ContentItem
import com.zamcan.nisaacare.domain.model.ContentType
import com.zamcan.nisaacare.domain.model.ReviewState

enum class HealthTopic {
    MENSTRUATION,
    MENSTRUAL_CYCLE,
    PMS,
    OVULATION,
    FERTILITY,
    REPRODUCTIVE_HEALTH,
    COMMON_SYMPTOMS,
    CYCLE_IRREGULARITY,
    PREGNANCY_BASICS,
    NIFAS_HEALTH_EDUCATION,
    EMOTIONAL_WELLBEING,
    SEEK_PROFESSIONAL_CARE
}

data class HealthEducationRecord(
    val topic: HealthTopic,
    val source: String,
    val reference: String,
    val reviewState: ReviewState,
    val version: Int,
    val translations: Map<AppLanguage, String> = emptyMap(),
    val audience: String = "GENERAL"
) {
    fun isSafeToPresent(): Boolean = reviewState == ReviewState.VERIFIED || reviewState == ReviewState.PUBLISHED
}

interface HealthKnowledgeRepository {
    fun topics(): List<HealthEducationRecord>
    fun verifiedTopics(): List<HealthEducationRecord>
}
