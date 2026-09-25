package com.zamcan.nisaacare.domain.content

import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.ContentItem
import com.zamcan.nisaacare.domain.model.ContentSeason
import com.zamcan.nisaacare.domain.model.Quotation
import com.zamcan.nisaacare.domain.model.ReviewState
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ContentManifest(
    val packageName: String,
    val version: Int,
    val contentTypes: Set<String>,
    val createdAt: String,
    val sha256: String,
    val minimumAppVersion: Int = 1,
    val signature: String? = null
)

data class ContentPackage(
    val manifest: ContentManifest,
    val healthItems: List<ContentItem> = emptyList(),
    val islamicItems: List<ContentItem> = emptyList(),
    val quotations: List<Quotation> = emptyList()
)

fun interface ContentSignatureVerifier {
    fun verify(manifest: ContentManifest, packageBytes: ByteArray): Boolean
}

class UnconfiguredContentSignatureVerifier : ContentSignatureVerifier {
    override fun verify(manifest: ContentManifest, packageBytes: ByteArray): Boolean = false
}

sealed class ContentUpdateResult {
    data class Accepted(val content: ContentPackage) : ContentUpdateResult()
    data class Rejected(val reason: String) : ContentUpdateResult()
}

class ContentUpdateValidator {
    fun validate(
        candidate: ContentPackage,
        installedVersion: Int,
        expectedSha256: String? = null
    ): ContentUpdateResult {
        val manifest = candidate.manifest
        if (manifest.packageName.isBlank()) return ContentUpdateResult.Rejected("Missing package name")
        if (manifest.version <= installedVersion) return ContentUpdateResult.Rejected("Version is not newer")
        if (manifest.contentTypes.isEmpty()) return ContentUpdateResult.Rejected("No content types declared")
        if (manifest.sha256.length != 64) return ContentUpdateResult.Rejected("SHA-256 is not 64 characters")
        if (expectedSha256 != null && expectedSha256 != manifest.sha256) {
            return ContentUpdateResult.Rejected("Manifest digest mismatch")
        }
        if (candidate.healthItems.any { it.reviewState == ReviewState.PUBLISHED && it.source.isBlank() }) {
            return ContentUpdateResult.Rejected("Published health content needs a source")
        }
        if (candidate.islamicItems.any { it.reviewState == ReviewState.PUBLISHED && it.reference.isBlank() }) {
            return ContentUpdateResult.Rejected("Published Islamic content needs a reference")
        }
        return ContentUpdateResult.Accepted(candidate)
    }
}

class ContentEngine {

    fun textFor(
        item: ContentItem,
        requestedLanguage: AppLanguage,
        fallbackLanguage: AppLanguage = AppLanguage.SWAHILI
    ): String? = item.translations[requestedLanguage]
        ?: item.translations[fallbackLanguage]

    fun quotationText(
        quotation: Quotation,
        requestedLanguage: AppLanguage,
        fallbackLanguage: AppLanguage = AppLanguage.SWAHILI
    ): String? = quotation.translations[requestedLanguage]
        ?: quotation.translations[fallbackLanguage]

    /**
     * Selects a deterministic daily record. Unreviewed records are intentionally
     * excluded from rotation and surfaced by the UI as pending editorial work.
     */
    fun selectDailyQuotation(
        quotations: List<Quotation>,
        date: LocalDate,
        audience: String = "GENERAL",
        season: ContentSeason? = null
    ): Quotation? {
        val day = date.toEpochDay()
        val eligible = quotations.filter { quotation ->
            quotation.intendedAudience == audience &&
                quotation.reviewState in PUBLISHABLE_STATES &&
                (season == null || quotation.season == season || quotation.season == ContentSeason.EVERGREEN)
        }
        if (eligible.isEmpty()) return null
        val index = Math.floorMod(day * 31L + stableHash(eligible.first().translationGroupId), eligible.size.toLong()).toInt()
        return eligible.sortedBy { it.id }[index]
    }

    fun rotationKey(date: LocalDate, salt: String = "nisaa-care"): String {
        val day = date.toEpochDay()
        return "$salt:${Math.floorMod(day * 2654435761L, 1_000_000_007L)}"
    }

    fun packageDigest(contentPackage: ContentPackage): String {
        val canonical = buildString {
            append(contentPackage.manifest.packageName)
            append('|').append(contentPackage.manifest.version)
            contentPackage.healthItems.sortedBy { it.id }.forEach {
                append('|').append(it.id).append(':').append(it.version).append(':').append(it.reviewState)
            }
            contentPackage.islamicItems.sortedBy { it.id }.forEach {
                append('|').append(it.id).append(':').append(it.version).append(':').append(it.reviewState)
            }
            contentPackage.quotations.sortedBy { it.id }.forEach {
                append('|').append(it.id).append(':').append(it.contentVersion).append(':').append(it.reviewState)
            }
        }
        return sha256(canonical)
    }

    fun isPublishable(state: ReviewState): Boolean = state in PUBLISHABLE_STATES

    fun daysInYear(date: LocalDate): Int = date.dayOfYear

    fun hasTranslation(record: Quotation, language: AppLanguage): Boolean =
        record.translations[language]?.isNotBlank() == true

    private fun stableHash(value: String): Int = value.fold(17) { acc, char -> acc * 31 + char.code }

    companion object {
        val PUBLISHABLE_STATES = setOf(ReviewState.VERIFIED, ReviewState.PUBLISHED)

        fun sha256(value: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(value.toByteArray(StandardCharsets.UTF_8))
            return digest.joinToString("") { byte -> "%02x".format(byte) }
        }
    }
}
