package com.zamcan.nisaacare.core.ads

import android.content.Context
import android.view.View

/**
 * Contract for the advertising provider.
 *
 * The interface exists so the rest of the app never depends on the Google
 * Mobile Ads SDK directly. The SDK is only linked into a build that explicitly
 * enables advertising; in every other build a no-op implementation is compiled
 * instead. That keeps the default APK free of the SDK and free of any
 * advertising behaviour.
 *
 * Every method here is safe to call unconditionally. The implementation is
 * responsible for checking consent and slot policy before doing anything.
 */
interface AdProvider {

    /** Whether this provider can actually serve ads in the current build. */
    val isAvailable: Boolean

    /**
     * Initialises the SDK.
     *
     * Must be called only after consent has been recorded as [AdsConsent.GRANTED].
     * Returns false if initialisation was skipped or failed.
     */
    fun initialise(context: Context): Boolean

    /**
     * Builds a banner ad view for the given slot.
     *
     * Returns null when no ad should be shown: consent missing, the slot is
     * prohibited, the SDK is unavailable, or the network failed. A null result
     * must be handled by simply not rendering anything, never by showing a
     * placeholder that reserves space.
     */
    fun createBannerAd(context: Context, slot: AdSlot): View?

    /**
     * Loads an interstitial ad.
     *
     * Interstitials are only ever shown at a natural break, which the caller
     * decides. The provider never shows one on its own.
     */
    fun loadInterstitial(context: Context): Boolean

    /**
     * Shows a loaded interstitial, if one is ready.
     *
     * Returns false when nothing was shown, which the caller must treat as
     * "carry on normally" rather than an error.
     */
    fun showInterstitial(context: Context, onDismissed: () -> Unit): Boolean

    /** Releases any held resources. */
    fun release()
}
