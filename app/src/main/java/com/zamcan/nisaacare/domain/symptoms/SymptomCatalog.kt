package com.zamcan.nisaacare.domain.symptoms

/** Stable symptom keys are extensible; custom observations are stored as text. */
enum class BuiltInSymptom(val key: String) {
    CRAMPS("cramps"),
    HEADACHE("headache"),
    BREAST_TENDERNESS("breast_tenderness"),
    BLOATING("bloating"),
    FATIGUE("fatigue"),
    SLEEP_CHANGES("sleep_changes"),
    APPETITE_CHANGES("appetite_changes"),
    MOOD("mood"),
    NAUSEA("nausea"),
    BACK_PAIN("back_pain")
}

data class SymptomObservation(
    val key: String,
    val customText: String? = null
) {
    init {
        require(key.startsWith("custom:") || BuiltInSymptom.entries.any { it.key == key }) {
            "Use a built-in symptom key or a custom: key"
        }
        require(key.startsWith("custom:") || customText.isNullOrBlank()) {
            "Built-in symptoms do not need custom text"
        }
    }
}
