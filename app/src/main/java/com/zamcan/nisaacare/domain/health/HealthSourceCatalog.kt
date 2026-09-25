package com.zamcan.nisaacare.domain.health

/**
 * External clinical sources used for editorial review. These are source records,
 * not runtime diagnostic rules. Published educational content must be reviewed
 * before it enters the bundled knowledge set.
 */
data class HealthSource(
    val id: String,
    val publisher: String,
    val title: String,
    val url: String,
    val useFor: List<HealthTopic>
)

enum class HealthTopic {
    MENSTRUATION, MENSTRUAL_CYCLE, FLOW, COMMON_SYMPTOMS, PAIN,
    EMOTIONAL_WELLBEING, CYCLE_IRREGULARITY, FERTILITY_EDUCATION,
    SEEK_PROFESSIONAL_CARE, ENDOMETRIOSIS, FIRST_PERIOD, PMS
}

object HealthSourceCatalog {
    val sources: List<HealthSource> = listOf(
        HealthSource("WHO_MENSTRUAL_HEALTH_2026", "World Health Organization", "Menstrual health",
            "https://www.who.int/news-room/fact-sheets/detail/menstrual-health",
            listOf(HealthTopic.MENSTRUATION, HealthTopic.MENSTRUAL_CYCLE, HealthTopic.FLOW, HealthTopic.COMMON_SYMPTOMS, HealthTopic.EMOTIONAL_WELLBEING, HealthTopic.SEEK_PROFESSIONAL_CARE)),
        HealthSource("WHO_ENDOMETRIOSIS_2025", "World Health Organization", "Endometriosis",
            "https://www.who.int/news-room/fact-sheets/detail/endometriosis",
            listOf(HealthTopic.ENDOMETRIOSIS, HealthTopic.PAIN, HealthTopic.FLOW, HealthTopic.SEEK_PROFESSIONAL_CARE)),
        HealthSource("ACOG_ABNORMAL_UTERINE_BLEEDING", "American College of Obstetricians and Gynecologists", "Abnormal Uterine Bleeding",
            "https://www.acog.org/womens-health/faqs/abnormal-uterine-bleeding",
            listOf(HealthTopic.MENSTRUAL_CYCLE, HealthTopic.FLOW, HealthTopic.CYCLE_IRREGULARITY, HealthTopic.SEEK_PROFESSIONAL_CARE)),
        HealthSource("ACOG_AMENORRHEA", "American College of Obstetricians and Gynecologists", "Amenorrhea: Absence of Periods",
            "https://www.acog.org/womens-health/faqs/amenorrhea-absence-of-periods",
            listOf(HealthTopic.CYCLE_IRREGULARITY, HealthTopic.SEEK_PROFESSIONAL_CARE)),
        HealthSource("ACOG_PAINFUL_PERIODS", "American College of Obstetricians and Gynecologists", "Painful Periods",
            "https://www.acog.org/womens-health/faqs/painful-periods",
            listOf(HealthTopic.PAIN, HealthTopic.COMMON_SYMPTOMS, HealthTopic.SEEK_PROFESSIONAL_CARE)),
        HealthSource("ACOG_PMS", "American College of Obstetricians and Gynecologists", "Premenstrual Syndrome (PMS)",
            "https://www.acog.org/womens-health/faqs/Premenstrual-Syndrome",
            listOf(HealthTopic.PMS, HealthTopic.EMOTIONAL_WELLBEING, HealthTopic.COMMON_SYMPTOMS)),
        HealthSource("ACOG_FIRST_PERIOD", "American College of Obstetricians and Gynecologists", "Your First Period",
            "https://www.acog.org/womens-health/faqs/your-first-period",
            listOf(HealthTopic.FIRST_PERIOD, HealthTopic.MENSTRUATION, HealthTopic.PAIN)),
        HealthSource("ACOG_CHRONIC_PELVIC_PAIN", "American College of Obstetricians and Gynecologists", "Chronic Pelvic Pain",
            "https://www.acog.org/womens-health/faqs/chronic-pelvic-pain",
            listOf(HealthTopic.PAIN, HealthTopic.ENDOMETRIOSIS, HealthTopic.SEEK_PROFESSIONAL_CARE))
    )
}
