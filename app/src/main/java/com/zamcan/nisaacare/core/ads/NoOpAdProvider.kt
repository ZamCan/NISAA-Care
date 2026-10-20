package com.zamcan.nisaacare.core.ads

import android.content.Context
import android.view.View

/**
 * Provider used when advertising is not enabled in the build.
 *
 * This is the implementation compiled into every default build, because the
 * Google Mobile Ads SDK is only linked when `-Pnisaa.ads.enabled=true` is
 * passed to Gradle. Every method here is a safe no-op.
 *
 * The point of this class is that no call site needs to know whether the SDK
 * is present. It either returns null or false, and the UI simply renders
 * nothing, so an advertising build and a non-advertising build take the same
 * code path through the app.
 */
object NoOpAdProvider : AdProvider {

    override val isAvailable: Boolean = false

    override fun initialise(context: Context): Boolean = false

    override fun createBannerAd(context: Context, slot: AdSlot): View? = null

    override fun loadInterstitial(context: Context): Boolean = false

    override fun showInterstitial(context: Context, onDismissed: () -> Unit): Boolean {
        // No ad was shown. The callback is still invoked so the caller resumes
        // its flow rather than waiting on an event that will not arrive.
        onDismissed()
        return false
    }

    override fun release() = Unit
}
