package com.zamcan.nisaacare.domain.health

data class HealthSource(
    val id: String,
    val publisher: String,
    val title: String,
    val url: String,
    val useFor: List<HealthTopic>
)

object HealthSourceCatalog {
    val sources: List<HealthSource> = listOf(
        HealthSource("WHO_MENSTRUAL_HEALTH_2026", "World Health Organization", "Menstrual health",
            "https://www.who.int/news-room/fact-sheets/detail/menstrual-health",
            listOf(HealthTopic.MENSTRUATION, HealthTopic.MENSTRUAL_CYCLE, HealthTopic.FLOW, HealthTopic.COMMON_SYMPTOMS, HealthTopic.EMOTIONAL_WELLBEING, HealthTopic.SEEK_PROFESSIONAL_CARE)),
        HealthSource("WHO_MENSTRUAL_BIOLOGY_2015", "World Health Organization", "Menstrual cycle and menstrual fluid biology",
            "https://platform.who.int/docs/default-source/mca-documents/policy-documents/policy/vut-rh-32-01-policy-2015-eng-policies-standards-fp-services.pdf",
            listOf(HealthTopic.MENSTRUATION, HealthTopic.MENSTRUAL_CYCLE, HealthTopic.FLOW)),
        HealthSource("WHO_REPRODUCTIVE_PHYSIOLOGY_2014", "World Health Organization", "Reproductive physiology",
            "https://platform.who.int/docs/default-source/mca-documents/policy-documents/guideline/BTN-RH-54-01-GUIDELINE-2014-eng-Infertility-Prevention-and-Management-2014.pdf",
            listOf(HealthTopic.MENSTRUAL_CYCLE, HealthTopic.OVULATION, HealthTopic.FERTILITY_EDUCATION)),
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
            listOf(HealthTopic.PAIN, HealthTopic.ENDOMETRIOSIS, HealthTopic.SEEK_PROFESSIONAL_CARE)),
        HealthSource("WHO_PCOS_2026", "World Health Organization", "Polycystic ovary syndrome",
            "https://www.who.int/news-room/fact-sheets/detail/polycystic-ovary-syndrome",
            listOf(HealthTopic.PCOS, HealthTopic.CYCLE_IRREGULARITY, HealthTopic.FERTILITY_EDUCATION, HealthTopic.SEEK_PROFESSIONAL_CARE)),
        HealthSource("WHO_SRHR_2026", "World Health Organization", "Sexual and reproductive health and rights",
            "https://www.who.int/health-topics/sexual-and-reproductive-health-and-rights",
            listOf(HealthTopic.REPRODUCTIVE_HEALTH, HealthTopic.FERTILITY_EDUCATION)),
        HealthSource("WHO_SELF_CARE_2026", "World Health Organization", "Self-care for health and well-being",
            "https://www.who.int/news-room/questions-and-answers/item/self-care-for-health-and-well-being",
            listOf(HealthTopic.SELF_CARE, HealthTopic.SEEK_PROFESSIONAL_CARE)),
        HealthSource("WHO_MENOPAUSE_2024", "World Health Organization", "Menopause",
            "https://www.who.int/news-room/fact-sheets/detail/menopause",
            listOf(HealthTopic.MENOPAUSE, HealthTopic.CYCLE_IRREGULARITY, HealthTopic.SEEK_PROFESSIONAL_CARE))
    )
}
