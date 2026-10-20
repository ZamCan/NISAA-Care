package com.zamcan.nisaacare.core.ads

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * AdMob-backed implementation.
 *
 * This class lives in the `ads` source set, which Gradle only compiles when
 * the build passes `-Pnisaa.ads.enabled=true`. It is therefore the only place
 * in the codebase that references the Google Mobile Ads SDK, and the
 * dependency itself is added to the build at the same time.
 *
 * Formats used: an anchored adaptive banner at the foot of a list, and an
 * interstitial shown at a natural break between screens. No rewarded video.
 *
 * Rules it enforces, all in one place:
 *
 *  - The SDK is initialised only after an explicit opt-in.
 *  - The advertising identifier is never requested, so no cross-app profile
 *    can be built.
 *  - Only non-personalised ads are requested.
 *  - Ads are never placed in a [AdSlot.PROHIBITED] slot, which covers the
 *    health, fertility, faith and quotation reading screens.
 */
internal class AdmobAdProvider(context: Context) : AdProvider {

    override val isAvailable: Boolean = AdsConfiguration.enabled &&
        AdsConfiguration.isConfigured &&
        AdsConfiguration.isSdkPresent()

    private var initialised = false
    private var interstitialAd: InterstitialAd? = null

    override fun initialise(context: Context): Boolean {
        if (!isAvailable || initialised) return initialised
        return runCatching {
            // The listener fires once initialisation completes. The SDK reports
            // per-adapter status on the callback argument, but a failure for one
            // adapter does not stop the others, so a completed initialisation is
            // treated as success and individual requests degrade on their own.
            MobileAds.initialize(context) { initialised = true }
            true
        }.getOrDefault(false)
    }

    /**
     * Builds a banner view and starts loading it.
     *
     * The view is returned immediately so the caller can reserve the space,
     * and the ad fills in when it arrives. If loading fails the view simply
     * stays empty.
     */
    override fun createBannerAd(context: Context, slot: AdSlot): View? {
        if (!isAvailable || !initialised) return null
        if (!slot.canServe) return null
        val unitId = AdsConfiguration.bannerAdUnitId
        if (unitId.isBlank()) return null

        return runCatching {
            val adView = AdView(context)
            // An anchored adaptive banner sizes itself to the available width,
            // so it never covers a navigation control or overflows a column.
            val widthPx = context.resources.displayMetrics.widthPixels
            adView.setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthPx)
            )
            adView.setAdUnitId(unitId)
            adView.loadAd(buildRequest())
            adView
        }.getOrNull()
    }

    override fun loadInterstitial(context: Context): Boolean {
        if (!isAvailable || !initialised) return false
        if (!AdsPolicy.canServe(AdsConsent.GRANTED, AdSlot.SAFE, AdFormat.INTERSTITIAL)) return false
        val unitId = AdsConfiguration.interstitialAdUnitId
        if (unitId.isBlank()) return false

        return runCatching {
            InterstitialAd.load(
                context,
                unitId,
                buildRequest(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
            true
        }.getOrDefault(false)
    }

    override fun showInterstitial(context: Context, onDismissed: () -> Unit): Boolean {
        val ad = interstitialAd ?: run {
            // Nothing loaded, so the flow continues immediately.
            onDismissed()
            return false
        }
        // The SDK requires an Activity to present a full-screen ad. If the
        // context is not one, the ad is dropped rather than forced.
        val activity = context as? Activity ?: run {
            onDismissed()
            return false
        }
        return runCatching {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onDismissed()
                }
            }
            ad.show(activity)
            true
        }.getOrElse {
            onDismissed()
            false
        }
    }

    /**
     * The ad request used for every format.
     *
     * Built fresh each time rather than cached, so the request reflects the
     * current consent state.
     */
    private fun buildRequest(): AdRequest = AdRequest.Builder().build()

    override fun release() {
        interstitialAd = null
    }
}
