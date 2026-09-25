package com.zamcan.nisaacare.domain.worship

import com.zamcan.nisaacare.domain.model.ContentItem
import com.zamcan.nisaacare.domain.model.ContentType
import com.zamcan.nisaacare.domain.model.ReviewState
import java.time.LocalDate

enum class BiologicalStatus {
    NOT_RECORDED,
    USER_CONFIRMED_MENSTRUATING,
    USER_CONFIRMED_NOT_MENSTRUATING,
    POSTPARTUM_STATUS_NOT_CONFIRMED
}

enum class WorshipTopic {
    DHIKR,
    DUA,
    CHARITY,
    ISLAMIC_LEARNING,
    FAMILY_KINDNESS,
    PURIFICATION,
    PRAYER,
    FASTING,
    QURAN_GUIDANCE
}

data class WorshipContext(
    val biologicalStatus: BiologicalStatus,
    val topics: List<WorshipTopic>,
    val evidenceRequired: Boolean = true,
    val explanation: String = "Biological information does not automatically determine a fiqh ruling."
)

class WorshipContextEngine {
    fun determine(
        status: BiologicalStatus,
        availableContent: List<ContentItem>,
        referenceDate: LocalDate = LocalDate.now()
    ): WorshipContext {
        val base = listOf(
            WorshipTopic.DHIKR,
            WorshipTopic.DUA,
            WorshipTopic.CHARITY,
            WorshipTopic.ISLAMIC_LEARNING,
            WorshipTopic.FAMILY_KINDNESS
        )
        val topics = when (status) {
            BiologicalStatus.NOT_RECORDED,
            BiologicalStatus.POSTPARTUM_STATUS_NOT_CONFIRMED -> base + WorshipTopic.QURAN_GUIDANCE
            BiologicalStatus.USER_CONFIRMED_MENSTRUATING ->
                base + listOf(WorshipTopic.PURIFICATION, WorshipTopic.PRAYER, WorshipTopic.QURAN_GUIDANCE)
            BiologicalStatus.USER_CONFIRMED_NOT_MENSTRUATING ->
                base + listOf(WorshipTopic.PRAYER, WorshipTopic.FASTING, WorshipTopic.QURAN_GUIDANCE)
        }
        val availableTopics = availableContent
            .filter { it.contentType == ContentType.ISLAMIC }
            .filter { it.reviewState == ReviewState.VERIFIED || it.reviewState == ReviewState.PUBLISHED }
            .map { it.topicKey }
            .toSet()
        return WorshipContext(
            biologicalStatus = status,
            topics = topics.filter { topic -> topic.name in availableTopics || topic in base },
            evidenceRequired = true,
            explanation = "Use reviewed evidence and your confirmed context; NISAA CARE does not infer a ruling."
        )
    }
}
