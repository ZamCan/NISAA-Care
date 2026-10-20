package com.zamcan.nisaacare.core.ads

/**
 * Advertising consent state.
 *
 * The app is offline-first and privacy-first by design. Advertising is opt-in
 * and it is off by default: the app ships with no advertising behaviour at all
 * until a user explicitly consents, and the SDK is not even linked into the
 * default build.
 *
 * Three states, and the third one matters:
 *
 *  - [UNDECIDED] The user has not been asked yet. Ads are not served.
 *  - [GRANTED] The user actively opted in. Non-personalised ads only.
 *  - [DENIED] The user actively opted out. Ads are never served.
 *
 * There is no state in which ads are served without a recorded opt-in. A user
 * who dismisses the consent sheet without choosing stays in [UNDECIDED], which
 * behaves exactly like [DENIED].
 */
enum class AdsConsent {
    UNDECIDED,
    GRANTED,
    DENIED;

    /**
     * Whether ads may be requested at all.
     *
     * Only an explicit opt-in qualifies. This is deliberately stricter than
     * "not denied", because treating silence as consent is the exact failure
     * mode that regulatory consent flows exist to prevent.
     */
    val allowsAds: Boolean get() = this == GRANTED

    companion object {
        fun fromStored(value: String?): AdsConsent = when (value) {
            "GRANTED" -> GRANTED
            "DENIED" -> DENIED
            else -> UNDECIDED
        }
    }
}

/**
 * Where an ad slot may appear.
 *
 * Ad placement is restricted by content sensitivity. NISAA CARE discusses
 * menstrual biology, fertility and Islamic worship, and placing advertising
 * directly against those screens would undermine both the privacy promise and
 * the editorial tone.
 *
 * [SAFE] slots are neutral surfaces with no health or religious content
 * nearby. [PROHIBITED] slots are never populated, regardless of consent.
 */
enum class AdSlot {
    /** Neutral interstitial between unrelated sections. */
    SAFE,

    /** Never served. Reserved for health, faith, fertility and quotation content. */
    PROHIBITED;

    val canServe: Boolean get() = this == SAFE
}

/**
 * Policy for which ad formats the app is willing to show.
 *
 * The set is deliberately small. A banner placed at the bottom of a list, and
 * an interstitial shown at a natural break between screens, are the two formats
 * that behave predictably and do not interrupt a task in progress. Rewarded
 * video is not used: it would require the app to offer something in exchange
 * for watching an ad, which changes the character of the product.
 */
enum class AdFormat {
    /** Banner, anchored to the bottom of a scrolling list. */
    BANNER,

    /** Full-screen ad, shown only at a natural break between screens. */
    INTERSTITIAL;

    companion object {
        /** Formats permitted in a [AdSlot.SAFE] slot. */
        val allowedInSafeSlots: Set<AdFormat> = setOf(BANNER, INTERSTITIAL)
    }
}

/**
 * Resolves whether a specific request is permitted.
 *
 * This is the single place where consent, slot policy and format policy are
 * combined. Keeping the decision in one function means an ad request cannot
 * bypass any of the three rules by being constructed somewhere else.
 */
object AdsPolicy {

    /**
     * Decides if an ad may be requested.
     *
     * @param consent the recorded consent state.
     * @param slot where the ad would appear.
     * @param format the requested format.
     */
    fun canServe(
        consent: AdsConsent,
        slot: AdSlot,
        format: AdFormat
    ): Boolean = consent.allowsAds &&
        slot.canServe &&
        format in AdFormat.allowedInSafeSlots

    /**
     * Non-personalised ads only.
     *
     * Personalised ads build a profile from the user's activity. In an app
     * where that activity is menstrual and fertility data, that profile is
     * sensitive by construction, so it is not something the app is willing to
     * hand to an ad network even with consent.
     */
    const val PERSONALISED_ADS_PERMITTED: Boolean = false

    /**
     * Whether the advertising ID may be requested.
     *
     * Kept false so the app does not request the advertising identifier at
     * all. Without it, the SDK can still serve non-personalised ads, but it
     * cannot build a cross-app profile.
     */
    const val ADVERTISING_ID_PERMITTED: Boolean = false
}
