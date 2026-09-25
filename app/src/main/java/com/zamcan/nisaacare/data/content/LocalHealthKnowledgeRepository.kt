package com.zamcan.nisaacare.data.content

import com.zamcan.nisaacare.domain.health.HealthEducationRecord
import com.zamcan.nisaacare.domain.health.HealthKnowledgeRepository
import com.zamcan.nisaacare.domain.health.HealthTopic
import com.zamcan.nisaacare.domain.model.ReviewState

class LocalHealthKnowledgeRepository : HealthKnowledgeRepository {
    override fun topics(): List<HealthEducationRecord> = HealthTopic.entries.map { topic ->
        HealthEducationRecord(
            topic = topic,
            source = "CONTENT_REVIEW_REQUIRED",
            reference = "Editorial review pending",
            reviewState = ReviewState.REVIEW_REQUIRED,
            version = 1
        )
    }

    override fun verifiedTopics(): List<HealthEducationRecord> = emptyList()
}
