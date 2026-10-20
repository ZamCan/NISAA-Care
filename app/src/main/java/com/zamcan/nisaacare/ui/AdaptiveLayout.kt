package com.zamcan.nisaacare.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * Adaptive window sizing.
 *
 * The app is not locked to portrait and does not simply stretch a phone layout
 * on a tablet. Instead it classifies the current window and picks a layout
 * mode, which is the behaviour Android's current adaptive guidance asks for.
 *
 * Classification follows the standard three-bucket model, using the width of
 * the window rather than the width of the physical display. That distinction
 * matters: in split-screen or on a foldable the window can be narrow even
 * though the device is large, and a layout chosen from the display size would
 * overflow the window it is actually drawn in.
 */
enum class WindowSizeClass {
    /** Phone, and any window narrower than 600dp. Single column, bottom nav. */
    COMPACT,

    /** 600dp to 839dp. Single column but with room for a wider reading gutter. */
    MEDIUM,

    /** 840dp and wider. Two-pane: navigation rail beside content. */
    EXPANDED;

    val isExpanded: Boolean get() = this == EXPANDED
    val isCompact: Boolean get() = this == COMPACT
}

/**
 * Reads the current window size class.
 *
 * On API 30+ the window metrics are the source of truth because they reflect
 * the actual window, including split-screen and letterboxing. Below that we
 * fall back to the configuration's screen width, which is the closest
 * available approximation.
 */
object AdaptiveLayout {

    /**
     * Maximum reading width for body content. Long lines of text are hard to
     * read, so on wide windows the content column is capped and centred rather
     * than stretched edge to edge.
     */
    const val MAX_CONTENT_WIDTH_DP = 720

    /** Width at which the layout is considered expanded. */
    const val EXPANDED_MIN_WIDTH_DP = 840

    /** Width at which the layout is considered medium. */
    const val MEDIUM_MIN_WIDTH_DP = 600

    fun sizeClass(context: Context): WindowSizeClass {
        val widthDp = windowWidthDp(context)
        return when {
            widthDp >= EXPANDED_MIN_WIDTH_DP -> WindowSizeClass.EXPANDED
            widthDp >= MEDIUM_MIN_WIDTH_DP -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.COMPACT
        }
    }

    fun windowWidthDp(context: Context): Int {
        val configuration = context.resources.configuration
        val widthDp = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val bounds = context.resources.displayMetrics
            // displayMetrics reflects the app window on API 30+, so it already
            // accounts for multi-window mode.
            (bounds.widthPixels / bounds.density).toInt()
        } else {
            configuration.screenWidthDp
        }
        // Guard against a zero-width window during the first layout pass, which
        // would otherwise classify as COMPACT and then relayout, causing a
        // visible jump.
        return if (widthDp <= 0) configuration.screenWidthDp else widthDp
    }

    fun isLandscape(context: Context): Boolean =
        context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun isNight(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    /**
     * Wraps content in a centred column of at most [MAX_CONTENT_WIDTH_DP].
     *
     * This is what stops a tablet from rendering full-bleed text columns. The
     * wrapper is transparent so the aurora backdrop still shows on either side
     * of the content, which keeps the wide layout visually connected to the
     * phone layout rather than looking like a different app.
     */
    fun centeredColumn(
        context: Context,
        content: View,
        horizontalPaddingDp: Int = 20
    ): FrameLayout = FrameLayout(context).apply {
        setBackgroundColor(Color.TRANSPARENT)
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            clipToPadding = false
        }
        val width = content.layoutParams?.width ?: ViewGroup.LayoutParams.MATCH_PARENT
        addView(
            column,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL
            )
        )
        column.addView(
            content,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        column.setPadding(
            NisaaDesign.dp(context, horizontalPaddingDp),
            0,
            NisaaDesign.dp(context, horizontalPaddingDp),
            0
        )
        // Cap the column width. The inner content still fills the column.
        val maxWidth = NisaaDesign.dp(context, MAX_CONTENT_WIDTH_DP)
        post {
            val lp = column.layoutParams as? FrameLayout.LayoutParams ?: return@post
            if (lp.width == FrameLayout.LayoutParams.MATCH_PARENT && width > 0) {
                lp.width = maxWidth.coerceAtMost(width)
                column.layoutParams = lp
            }
        }
    }

    /**
     * Number of grid columns for card content. On a wide window a single
     * column of cards wastes horizontal space, so card lists reflow into more
     * columns instead of stretching.
     */
    fun gridColumns(context: Context): Int = when (sizeClass(context)) {
        WindowSizeClass.COMPACT -> 1
        WindowSizeClass.MEDIUM -> 2
        WindowSizeClass.EXPANDED -> 3
    }
}
