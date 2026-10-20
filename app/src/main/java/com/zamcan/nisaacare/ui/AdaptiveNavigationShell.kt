package com.zamcan.nisaacare.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.zamcan.nisaacare.R

/**
 * Navigation shell that adapts to the window size.
 *
 * On a compact window the navigation is a bottom bar, which is the reachable
 * position for one-handed use. On an expanded window it becomes a side rail,
 * because a bottom bar on a 10-inch tablet is an unreasonable distance from
 * the top of the screen and wastes the vertical space that made the window
 * large in the first place.
 *
 * The rail and the bar present exactly the same destinations in the same
 * order. Only their placement changes, so muscle memory and expectations carry
 * across form factors.
 */
class AdaptiveNavigationShell(
    context: Context,
    private val sizeClass: WindowSizeClass
) : FrameLayout(context) {

    private val railWidthDp = 88

    /** Navigation entries. Each renders as a bar item or a rail item. */
    data class Destination(
        val iconRes: Int,
        val labelRes: Int
    )

    private val barContainer = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        background = Glass.bar(context, Glass.Radius.XL)
        elevation = 0f
        minimumHeight = NisaaDesign.dp(context, 74)
        setPadding(
            NisaaDesign.dp(context, 14), NisaaDesign.dp(context, 8),
            NisaaDesign.dp(context, 14), NisaaDesign.dp(context, 8)
        )
    }

    private val railContainer = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(0, NisaaDesign.dp(context, 16), 0, NisaaDesign.dp(context, 16))
    }

    /**
     * Builds the shell.
     *
     * [isSelected] tells the shell which destination is active.
     * [onSelected] is invoked when the user picks a destination.
     */
    fun build(
        destinations: List<Destination>,
        isSelected: (Destination) -> Boolean,
        onSelected: (Destination) -> Unit
    ) {
        setBackgroundColor(Color.TRANSPARENT)

        if (sizeClass.isExpanded) {
            addView(
                railContainer,
                LayoutParams(NisaaDesign.dp(context, railWidthDp), LayoutParams.MATCH_PARENT)
            )
            destinations.forEach { destination ->
                railContainer.addView(railItem(destination, isSelected(destination), onSelected))
            }
        } else {
            addView(
                barContainer,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM)
            )
            destinations.forEach { destination ->
                barContainer.addView(barItem(destination, isSelected(destination), onSelected))
            }
        }
    }

    private fun barItem(
        destination: Destination,
        selected: Boolean,
        onSelected: (Destination) -> Unit
    ): View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
        // Minimum 48dp touch target, per the accessibility rules in
        // docs/PREMIUM_NATIVE_UI_SYSTEM.md.
        minimumWidth = NisaaDesign.dp(context, 56)
        minimumHeight = NisaaDesign.dp(context, 56)
        contentDescription = context.getString(destination.labelRes)
        addView(
            GlassIcons.well(
                context,
                destination.iconRes,
                if (selected) R.color.nisaa_rose_dark else R.color.nisaa_ink_soft,
                sizeDp = 20,
                wellPaddingDp = 8,
                radiusDp = 14
            ).apply {
                alpha = if (selected) 1f else 0.72f
            }
        )
        addView(NisaaDesign.space(context, 0, 4))
        addView(
            NisaaDesign.text(
                context,
                context.getString(destination.labelRes),
                10.5f,
                if (selected) R.color.nisaa_rose_dark else R.color.nisaa_ink_soft,
                selected,
                Gravity.CENTER
            ).apply { maxLines = 1 }
        )
        setOnClickListener { onSelected(destination) }
    }

    private fun railItem(
        destination: Destination,
        selected: Boolean,
        onSelected: (Destination) -> Unit
    ): View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
        minimumWidth = NisaaDesign.dp(context, 64)
        minimumHeight = NisaaDesign.dp(context, 64)
        contentDescription = context.getString(destination.labelRes)
        setPadding(0, NisaaDesign.dp(context, 6), 0, NisaaDesign.dp(context, 6))
        // A selected rail entry gets a glass pill so the active destination is
        // marked by shape as well as by colour.
        background = if (selected) {
            Glass.surface(context, Glass.Level.THREE, Glass.Radius.MD, tintRes = R.color.nisaa_rose_soft, tintAlpha = 170)
        } else {
            null
        }
        addView(
            GlassIcons.well(
                context,
                destination.iconRes,
                if (selected) R.color.nisaa_rose_dark else R.color.nisaa_ink_soft,
                sizeDp = 22,
                wellPaddingDp = 9,
                radiusDp = 15
            ).apply {
                alpha = if (selected) 1f else 0.72f
            }
        )
        addView(NisaaDesign.space(context, 0, 5))
        addView(
            NisaaDesign.text(
                context,
                context.getString(destination.labelRes),
                11f,
                if (selected) R.color.nisaa_rose_dark else R.color.nisaa_ink_soft,
                selected,
                Gravity.CENTER
            ).apply { maxLines = 2 }
        )
        setOnClickListener { onSelected(destination) }
    }
}
