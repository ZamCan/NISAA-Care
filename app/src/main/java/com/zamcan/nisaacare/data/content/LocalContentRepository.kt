package com.zamcan.nisaacare.data.content

import com.zamcan.nisaacare.domain.content.ContentEngine
import com.zamcan.nisaacare.domain.content.ContentManifest
import com.zamcan.nisaacare.domain.content.ContentPackage
import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.ContentItem
import com.zamcan.nisaacare.domain.model.Quotation
import java.time.LocalDate

interface ContentRepository {
    fun healthTopics(): List<ContentItem>
    fun islamicTopics(): List<ContentItem>
    fun educationTopics(): List<ContentItem>
    fun quotations(): List<Quotation>
    fun dailyQuotation(date: LocalDate, audience: String): Quotation?
    fun translate(item: ContentItem, language: AppLanguage): String?
}

class LocalContentRepository(
    private val engine: ContentEngine = ContentEngine(),
    private val seed: SeedContent = SeedContent
) : ContentRepository {
    override fun healthTopics(): List<ContentItem> = seed.health
    override fun islamicTopics(): List<ContentItem> = seed.islamic
    override fun educationTopics(): List<ContentItem> = seed.husbandEducation
    override fun quotations(): List<Quotation> = seed.quotations
    override fun dailyQuotation(date: LocalDate, audience: String): Quotation? =
        engine.selectDailyQuotation(quotations(), date, audience)
    override fun translate(item: ContentItem, language: AppLanguage): String? = engine.textFor(item, language)
}
