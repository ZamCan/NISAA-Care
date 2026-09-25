package com.zamcan.nisaacare.domain.islamic

import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.ReviewState

enum class EvidenceType { QURAN, HADITH, SCHOLARLY_TEXT, FIQH_POSITION }

data class EvidenceReference(
    val id: String,
    val type: EvidenceType,
    val textByLanguage: Map<AppLanguage, String> = emptyMap(),
    val source: String,
    val reference: String,
    val grading: String? = null,
    val reviewState: ReviewState
)

data class ScholarlyPosition(
    val id: String,
    val methodology: String,
    val summaryByLanguage: Map<AppLanguage, String> = emptyMap(),
    val evidenceIds: List<String>,
    val reviewState: ReviewState,
    val version: Int
)

data class FiqhQuestion(
    val id: String,
    val topicKey: String,
    val questionByLanguage: Map<AppLanguage, String>,
    val evidence: List<EvidenceReference>,
    val positions: List<ScholarlyPosition>,
    val reviewState: ReviewState,
    val version: Int
) {
    fun requiresEditorialReview(): Boolean = reviewState != ReviewState.PUBLISHED
}

interface IslamicKnowledgeRepository {
    fun questions(topicKey: String): List<FiqhQuestion>
    fun verifiedQuestions(): List<FiqhQuestion>
}
