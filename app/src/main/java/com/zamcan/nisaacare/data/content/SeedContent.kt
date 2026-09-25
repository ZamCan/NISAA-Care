package com.zamcan.nisaacare.data.content

import com.zamcan.nisaacare.domain.model.ContentItem
import com.zamcan.nisaacare.domain.model.ContentSeason
import com.zamcan.nisaacare.domain.model.ContentType
import com.zamcan.nisaacare.domain.model.Quotation
import com.zamcan.nisaacare.domain.model.ReviewState

/**
 * Initial catalog entries are deliberately metadata-only. They keep the
 * content surface useful offline without pretending that unreviewed medical or
 * religious claims are authoritative.
 */
object SeedContent {
    private fun pending(
        id: String,
        type: ContentType,
        topic: String,
        audience: String = "GENERAL"
    ) = ContentItem(
        id = id,
        contentType = type,
        topicKey = topic,
        source = "CONTENT_REVIEW_REQUIRED",
        reference = "Editorial review pending",
        reviewState = ReviewState.REVIEW_REQUIRED,
        version = 1,
        audience = audience
    )

    val health: List<ContentItem> = listOf(
        pending("health_menstruation", ContentType.HEALTH, "MENSTRUATION"),
        pending("health_menstrual_cycle", ContentType.HEALTH, "MENSTRUAL_CYCLE"),
        pending("health_pms", ContentType.HEALTH, "PMS"),
        pending("health_ovulation", ContentType.HEALTH, "OVULATION"),
        pending("health_fertility", ContentType.HEALTH, "FERTILITY"),
        pending("health_reproductive", ContentType.HEALTH, "REPRODUCTIVE_HEALTH"),
        pending("health_symptoms", ContentType.HEALTH, "COMMON_SYMPTOMS"),
        pending("health_irregularity", ContentType.HEALTH, "CYCLE_IRREGULARITY"),
        pending("health_pregnancy_basics", ContentType.HEALTH, "PREGNANCY_BASICS"),
        pending("health_nifas", ContentType.HEALTH, "NIFAS_HEALTH_EDUCATION"),
        pending("health_emotional_wellbeing", ContentType.HEALTH, "EMOTIONAL_WELLBEING"),
        pending("health_professional_care", ContentType.HEALTH, "SEEK_PROFESSIONAL_CARE")
    )

    val islamic: List<ContentItem> = listOf(
        pending("islamic_quran", ContentType.ISLAMIC, "QURAN"),
        pending("islamic_hadith", ContentType.ISLAMIC, "HADITH"),
        pending("islamic_hayd", ContentType.ISLAMIC, "HAYD", "WOMAN"),
        pending("islamic_nifas", ContentType.ISLAMIC, "NIFAS", "WOMAN"),
        pending("islamic_taharah", ContentType.ISLAMIC, "TAHARAH", "WOMAN"),
        pending("islamic_ghusl", ContentType.ISLAMIC, "GHUSL", "WOMAN"),
        pending("islamic_salah", ContentType.ISLAMIC, "SALAH"),
        pending("islamic_sawm", ContentType.ISLAMIC, "SAWM"),
        pending("islamic_ramadan", ContentType.ISLAMIC, "RAMADAN"),
        pending("islamic_dhikr", ContentType.ISLAMIC, "DHIKR"),
        pending("islamic_dua", ContentType.ISLAMIC, "DUA"),
        pending("islamic_marriage", ContentType.ISLAMIC, "MARRIAGE"),
        pending("islamic_family", ContentType.ISLAMIC, "FAMILY_RESPONSIBILITIES"),
        pending("islamic_hajj_umrah", ContentType.ISLAMIC, "HAJJ_UMRAH")
    )

    val husbandEducation: List<ContentItem> = listOf(
        pending("husband_kindness", ContentType.EDUCATION, "KINDNESS", "HUSBAND"),
        pending("husband_communication", ContentType.EDUCATION, "COMMUNICATION", "HUSBAND"),
        pending("husband_responsibility", ContentType.EDUCATION, "FAMILY_RESPONSIBILITY", "HUSBAND"),
        pending("husband_support", ContentType.EDUCATION, "EMOTIONAL_SUPPORT", "HUSBAND")
    )

    // No religious quotation text is shipped until an editorial source review
    // records its exact wording, translation, and scholarly metadata.
    val quotations: List<Quotation> = emptyList()

    fun allTopicKeys(): List<String> =
        (health + islamic + husbandEducation).map { it.topicKey }.distinct()
}
