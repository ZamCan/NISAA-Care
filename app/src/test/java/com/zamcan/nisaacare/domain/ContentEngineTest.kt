package com.zamcan.nisaacare.domain

import com.zamcan.nisaacare.domain.content.ContentEngine
import com.zamcan.nisaacare.domain.content.ContentManifest
import com.zamcan.nisaacare.domain.content.ContentPackage
import com.zamcan.nisaacare.domain.content.ContentUpdateResult
import com.zamcan.nisaacare.domain.content.ContentUpdateValidator
import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.ContentItem
import com.zamcan.nisaacare.domain.model.ContentSeason
import com.zamcan.nisaacare.domain.model.ContentType
import com.zamcan.nisaacare.domain.model.Quotation
import com.zamcan.nisaacare.domain.model.ReviewState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ContentEngineTest {
    private val engine = ContentEngine()

    @Test
    fun languageSwitchUsesLinkedTranslation() {
        val item = ContentItem(
            id = "q",
            contentType = ContentType.QUOTATION,
            topicKey = "KINDNESS",
            source = "source",
            reference = "ref",
            reviewState = ReviewState.PUBLISHED,
            version = 1,
            translations = mapOf(AppLanguage.SWAHILI to "Swahili text", AppLanguage.ENGLISH to "English text")
        )
        assertEquals("English text", engine.textFor(item, AppLanguage.ENGLISH))
        assertEquals("Swahili text", engine.textFor(item, AppLanguage.ARABIC))
    }

    @Test
    fun quoteRotationIsDeterministicAndSkipsPendingContent() {
        val pending = quotation("pending", ReviewState.REVIEW_REQUIRED)
        val first = quotation("one", ReviewState.PUBLISHED)
        val second = quotation("two", ReviewState.PUBLISHED)
        val date = LocalDate.parse("2025-04-01")
        assertEquals(engine.selectDailyQuotation(listOf(pending, first, second), date), engine.selectDailyQuotation(listOf(first, second, pending), date))
        assertNotNull(engine.selectDailyQuotation(listOf(pending, first, second), date))
    }

    @Test
    fun rotationKeyIsRepeatable() {
        val date = LocalDate.parse("2025-04-01")
        assertEquals(engine.rotationKey(date), engine.rotationKey(date))
        assertNotEquals(engine.rotationKey(date), engine.rotationKey(date.plusDays(1)))
    }

    @Test
    fun updateValidatorRejectsOldAndUndigestedPackages() {
        val item = ContentItem("h", ContentType.HEALTH, "TOPIC", "source", "ref", reviewState = ReviewState.PUBLISHED, version = 1)
        val old = ContentPackage(ContentManifest("base", 1, setOf("health"), "now", "a".repeat(64)))
        val newPackage = ContentPackage(ContentManifest("base", 2, setOf("health"), "now", "a".repeat(64)), healthItems = listOf(item))
        val validator = ContentUpdateValidator()
        assertTrue(validator.validate(old, 1) is ContentUpdateResult.Rejected)
        assertTrue(validator.validate(newPackage, 1) is ContentUpdateResult.Accepted)
        val badDigest = newPackage.copy(manifest = newPackage.manifest.copy(sha256 = "b".repeat(63)))
        assertTrue(validator.validate(badDigest, 1) is ContentUpdateResult.Rejected)
    }

    @Test
    fun unpublishedTextIsNeverSelected() {
        val record = quotation("pending", ReviewState.DRAFT)
        assertNull(engine.selectDailyQuotation(listOf(record), LocalDate.parse("2025-01-01")))
    }

    private fun quotation(id: String, state: ReviewState) = Quotation(
        id = id,
        translationGroupId = "group-$id",
        sourceType = "editorial",
        source = "source",
        reference = "ref",
        topic = "KINDNESS",
        intendedAudience = "GENERAL",
        rotationClass = "daily",
        reviewState = state,
        contentVersion = 1,
        translations = mapOf(AppLanguage.SWAHILI to "text")
    )
}
