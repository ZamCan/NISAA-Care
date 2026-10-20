package com.zamcan.nisaacare.core.ads

import android.content.Context
import com.zamcan.nisaacare.BuildConfig

/**
 * Build-time advertising configuration.
 *
 * Every value here comes from a Gradle property that has no default in the
 * repository, so a checkout builds with advertising fully inert: no AdMob
 * account identifier is committed to the source tree.
 *
 * The one exception is [TEST_APP_ID] and the matching test ad units. These are
 * Google's own published test identifiers, which serve test ads only and earn
 * nothing. They are what makes the ad slots visibly work during development
 * without anyone having to hand over a live account.
 *
 * Supplying a real account replaces them entirely:
 *   gradle -Pnisaa.ads.enabled=true \
 *           -Pnisaa.admob.appId=ca-app-pub-... \
 *           -Pnisaa.admob.safeUnitId=ca-app-pub-.../... \
 *           -Pnisaa.admob.interstitialUnitId=ca-app-pub-.../... \
 *           assembleRelease
 */
object AdsConfiguration {

    /**
     * Google's published test application identifier.
     *
     * Serves test ads only. Never earns revenue. Used when the build does not
     * supply a real AdMob account. Defined in the build script so the manifest
     * placeholder and this class cannot drift apart.
     */
    val testAppId: String get() = BuildConfig.ADMOB_TEST_APP_ID

    /** Google's published test banner ad unit. */
    val testBannerUnitId: String get() = BuildConfig.ADMOB_TEST_BANNER_UNIT_ID

    /** Google's published test interstitial ad unit. */
    val testInterstitialUnitId: String get() = BuildConfig.ADMOB_TEST_INTERSTITIAL_UNIT_ID

    /**
     * Whether the advertising layer is permitted to run.
     *
     * Read from `nisaa.ads.enabled` at build time. False by default.
     */
    val enabled: Boolean get() = BuildConfig.ADS_ENABLED

    /**
     * The AdMob application identifier.
     *
     * Empty unless the build supplies `nisaa.admob.appId`.
     */
    val appId: String get() = BuildConfig.ADMOB_APP_ID

    /**
     * Whether a live AdMob account was supplied to this build.
     *
     * When false the build falls back to Google's test identifiers so the slots
     * still render during development.
     */
    val usesLiveAccount: Boolean get() = appId.isNotBlank()

    /**
     * The application identifier actually used by the SDK.
     *
     * Falls back to the test identifier so the SDK always has something valid
     * to initialise with. The SDK refuses to start without one, which would
     * otherwise leave the slots silently empty.
     */
    val effectiveAppId: String get() = if (usesLiveAccount) appId else testAppId

    /**
     * Banner ad unit used in a [AdSlot.SAFE] slot.
     */
    val bannerAdUnitId: String
        get() = BuildConfig.ADMOB_SAFE_UNIT_ID.ifBlank {
            if (usesLiveAccount) "" else testBannerUnitId
        }

    /**
     * Interstitial ad unit used at natural breaks between screens.
     */
    val interstitialAdUnitId: String
        get() = BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID.ifBlank {
            if (usesLiveAccount) "" else testInterstitialUnitId
        }

    /** Whether an AdMob application identifier is available at all. */
    val isConfigured: Boolean get() = effectiveAppId.isNotBlank()

    /**
     * Whether the SDK is linked into this build.
     *
     * Checked at runtime so the app degrades cleanly if the advertising module
     * is removed.
     */
    fun isSdkPresent(): Boolean = try {
        Class.forName("com.google.android.gms.ads.MobileAds")
        true
    } catch (_: Throwable) {
        false
    }

    /**
     * Whether the advertising identifier may be requested.
     *
     * Delegated to [AdsPolicy] so there is one place that decides this.
     */
    val advertisingIdPermitted: Boolean get() = AdsPolicy.ADVERTISING_ID_PERMITTED

    /**
     * Whether personalised ads may be requested.
     */
    val personalisedAdsPermitted: Boolean get() = AdsPolicy.PERSONALISED_ADS_PERMITTED
}
