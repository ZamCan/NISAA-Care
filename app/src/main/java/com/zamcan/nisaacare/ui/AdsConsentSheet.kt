package com.zamcan.nisaacare.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.zamcan.nisaacare.R
import com.zamcan.nisaacare.core.ads.AdsConsent
import com.zamcan.nisaacare.core.ads.AdsConsentStore
import com.zamcan.nisaacare.core.ads.AdsConfiguration

/**
 * Consent sheet for advertising.
 *
 * Presented before any advertising behaviour is enabled. The sheet is honest
 * about what the app does and does not do:
 *
 *  - It states that NISAA CARE works fully offline and that cycle, fertility
 *    and health data never leaves the device.
 *  - It states that opting in permits non-personalised ads only.
 *  - It states that declining costs the user nothing.
 *
 * That last point matters. A consent flow that implies a cost for refusal is
 * not consent, it is coercion, and regulators treat it that way.
 */
object AdsConsentSheet {

    /**
     * Whether the consent sheet should be shown.
     *
     * False when advertising is not enabled in this build, or when the SDK is
     * not linked. In those cases the app has no advertising to consent to, so
     * asking would be misleading.
     */
    fun shouldShow(context: Context, store: AdsConsentStore): Boolean {
        if (!AdsConfiguration.enabled) return false
        if (!AdsConfiguration.isConfigured) return false
        if (!AdsConfiguration.isSdkPresent()) return false
        return !store.hasDecision()
    }

    /**
     * Builds the consent sheet content.
     *
     * [onDecision] is called with the user's choice. The caller is responsible
     * for persisting it and for dismissing the sheet.
     */
    fun build(
        context: Context,
        onDecision: (AdsConsent) -> Unit
    ): View {
        val root = FrameLayout(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }

        val sheet = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = Glass.surface(context, Glass.Level.THREE, Glass.Radius.XL)
            setPadding(
                NisaaDesign.dp(context, 24), NisaaDesign.dp(context, 24),
                NisaaDesign.dp(context, 24), NisaaDesign.dp(context, 20)
            )
        }

        val scroll = ScrollView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isFillViewport = false
        }
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        body.addView(NisaaDesign.eyebrow(context, context.getString(R.string.ads_consent_eyebrow)))
        body.addView(NisaaDesign.space(context, 0, 8))
        body.addView(NisaaDesign.serif(context, context.getString(R.string.ads_consent_title), 24f))
        body.addView(NisaaDesign.space(context, 0, 12))
        body.addView(
            NisaaDesign.body(context, context.getString(R.string.ads_consent_body))
                .apply { setLineSpacing(NisaaDesign.dp(context, 5).toFloat(), 1.05f) }
        )
        body.addView(NisaaDesign.space(context, 0, 14))

        // The three facts that make the choice meaningful. Presented as a list
        // rather than a paragraph so each point is separately scannable.
        listOf(
            R.string.ads_consent_point_offline,
            R.string.ads_consent_point_no_personalisation,
            R.string.ads_consent_point_no_cost
        ).forEach { pointRes ->
            body.addView(
                LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.TOP
                    addView(
                        GlassIcons.well(
                            context, R.drawable.ic_check, R.color.nisaa_sage,
                            sizeDp = 14, wellPaddingDp = 5, radiusDp = 9
                        )
                    )
                    addView(NisaaDesign.space(context, 10, 1))
                    addView(
                        NisaaDesign.body(context, context.getString(pointRes))
                            .apply { textSize = 14f },
                        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    )
                }
            )
            body.addView(NisaaDesign.space(context, 0, 10))
        }

        body.addView(NisaaDesign.space(context, 0, 4))
        body.addView(
            NisaaDesign.body(context, context.getString(R.string.ads_consent_detail))
                .apply {
                    textSize = 12.5f
                    setTextColor(NisaaDesign.color(context, R.color.nisaa_ink_soft))
                }
        )

        scroll.addView(
            body,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
        sheet.addView(
            scroll,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )

        // Actions are laid out as a row on wide sheets and stacked on narrow
        // ones, so the buttons never get squeezed into unreadable slivers.
        val actions = LinearLayout(context).apply {
            orientation = if (AdaptiveLayout.sizeClass(context).isCompact) {
                LinearLayout.VERTICAL
            } else {
                LinearLayout.HORIZONTAL
            }
            gravity = Gravity.CENTER
        }
        val accept = NisaaDesign.primaryButton(
            context,
            context.getString(R.string.ads_consent_accept),
            R.drawable.ic_check
        ) {
            onDecision(AdsConsent.GRANTED)
        }
        val decline = NisaaDesign.outlineButton(
            context,
            context.getString(R.string.ads_consent_decline)
        ) {
            onDecision(AdsConsent.DENIED)
        }
        if (actions.orientation == LinearLayout.HORIZONTAL) {
            actions.addView(decline, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            actions.addView(NisaaDesign.space(context, 10, 1))
            actions.addView(accept, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        } else {
            actions.addView(accept)
            actions.addView(NisaaDesign.space(context, 0, 10))
            actions.addView(decline)
        }
        sheet.addView(NisaaDesign.space(context, 0, 16))
        sheet.addView(actions)

        root.addView(
            sheet,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
        )
        return root
    }
}
