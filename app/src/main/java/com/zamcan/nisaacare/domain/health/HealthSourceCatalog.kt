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

object HealthSourceCatalog {
    val sources: List<HealthSource> = listOf(
        HealthSource(
            id = "WHO_MENSTRUAL_HEALTH_2026",
            publisher = "World Health Organization",
            title = "Menstrual health",
            url = "https://www.who.int/news-room/fact-sheets/detail/menstrual-health",
            useFor = listOf(
                HealthTopic.MENSTRUATION,
                HealthTopic.MENSTRUAL_CYCLE,
                HealthTopic.COMMON_SYMPTOMS,
                HealthTopic.EMOTIONAL_WELLBEING,
                HealthTopic.SEEK_PROFESSIONAL_CARE
            )
        ),
        HealthSource(
            id = "ACOG_ABNORMAL_UTERINE_BLEEDING",
            publisher = "American College of Obstetricians and Gynecologists",
            title = "Abnormal Uterine Bleeding",
            url = "https://www.acog.org/womens-health/faqs/abnormal-uterine-bleeding",
            useFor = listOf(
                HealthTopic.MENSTRUAL_CYCLE,
                HealthTopic.CYCLE_IRREGULARITY,
                HealthTopic.SEEK_PROFESSIONAL_CARE
            )
        ),
        HealthSource(
            id = "ACOG_AMENORRHEA",
            publisher = "American College of Obstetricians and Gynecologists",
            title = "Amenorrhea: Absence of Periods",
            url = "https://www.acog.org/womens-health/faqs/amenorrhea-absence-of-periods",
            useFor = listOf(
                HealthTopic.CYCLE_IRREGULARITY,
                HealthTopic.SEEK_PROFESSIONAL_CARE
            )
        )
    )
}
