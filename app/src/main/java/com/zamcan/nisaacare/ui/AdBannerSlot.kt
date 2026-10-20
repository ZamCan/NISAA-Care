package com.zamcan.nisaacare.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.zamcan.nisaacare.core.ads.AdProvider
import com.zamcan.nisaacare.core.ads.AdSlot

/**
 * Banner slot rendered at the foot of a scrolling list.
 *
 * The container is a glass surface, so the banner sits on the same design
 * language as the rest of the app rather than looking like a foreign element
 * dropped into the layout.
 *
 * It also collapses to nothing when no ad is available. That matters: an
 * empty reserved strip at the bottom of every screen would look broken, and it
 * would push content around between loads.
 */
object AdBannerSlot {

    /**
     * Builds the banner slot.
     *
     * Returns null when the provider cannot supply an ad, so the caller adds
     * nothing at all rather than adding an empty view.
     */
    fun build(
        context: Context,
        provider: AdProvider,
        slot: AdSlot = AdSlot.SAFE
    ): View? {
        if (!provider.isAvailable) return null
        val adView = provider.createBannerAd(context, slot) ?: return null

        val container = FrameLayout(context).apply {
            background = Glass.surface(context, Glass.Level.ONE, Glass.Radius.MD, sheen = false)
            setPadding(
                NisaaDesign.dp(context, 8), NisaaDesign.dp(context, 8),
                NisaaDesign.dp(context, 8), NisaaDesign.dp(context, 8)
            )
            // The ad content is centered and the container hugs it, so the
            // glass frame matches the height of the banner exactly.
            addView(
                adView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
        }

        // Collapse once the ad view has been laid out at its natural height.
        container.addOnLayoutChangeListener { view, _, top, _, bottom, _, oldTop, _, oldBottom ->
            if (bottom - top > 0 && (oldBottom - oldTop <= 0)) {
                view.requestLayout()
            }
        }

        return container
    }
}
