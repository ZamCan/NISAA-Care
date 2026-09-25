package com.zamcan.nisaacare.data.content

import com.zamcan.nisaacare.domain.islamic.FiqhQuestion
import com.zamcan.nisaacare.domain.islamic.IslamicKnowledgeRepository
import com.zamcan.nisaacare.domain.model.ReviewState

class LocalIslamicKnowledgeRepository : IslamicKnowledgeRepository {
    override fun questions(topicKey: String): List<FiqhQuestion> = emptyList()

    override fun verifiedQuestions(): List<FiqhQuestion> = emptyList()
}
