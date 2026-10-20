package com.zamcan.nisaacare.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.zamcan.nisaacare.R

/**
 * Icon treatment for the glass system.
 *
 * Two things make an icon sit inside glass rather than on top of it:
 *
 *  1. A translucent tinted well behind the glyph. The icon then has its own
 *     depth cue, because the well tints the aurora behind it rather than
 *     covering it.
 *  2. A tint that darkens slightly with icon size. Small glyphs disappear
 *     against a light glass panel, so compact icons get a stronger ink.
 */
object GlassIcons {

    /**
     * Tinted well sized to the glyph. Returns a container so callers can keep
     * the icon centred and add spacing without measuring.
     *
     * The tint is resolved through [tintFor] so a small glyph receives a
     * slightly stronger ink than a large one, which keeps it legible against
     * the pale glass fill.
     */
    fun well(
        context: Context,
        iconRes: Int,
        tintRes: Int,
        sizeDp: Int = 24,
        wellPaddingDp: Int = 9,
        radiusDp: Int = 15,
        tintOverride: Int? = null
    ): ImageView = ImageView(context).apply {
        setImageResource(iconRes)
        imageTintList = android.content.res.ColorStateList.valueOf(
            tintOverride ?: tintFor(context, tintRes, sizeDp)
        )
        val size = NisaaDesign.dp(context, sizeDp)
        val pad = NisaaDesign.dp(context, wellPaddingDp)
        setPadding(pad, pad, pad, pad)
        background = wellDrawable(context, radiusDp)
        layoutParams = android.view.ViewGroup.LayoutParams(
            size + pad * 2,
            size + pad * 2
        )
        scaleType = ImageView.ScaleType.FIT_CENTER
    }

    /** Small square/rounded well tinted to a single accent colour. */
    fun wellDrawable(context: Context, radiusDp: Int = 15): Drawable {
        val radius = NisaaDesign.dp(context, radiusDp).toFloat()
        val accent = context.getColor(R.color.nisaa_rose)
        return object : Drawable() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
            private val rect = android.graphics.RectF()
            private var shader: Shader? = null
            private var w = -1f
            private var h = -1f

            override fun onBoundsChange(bounds: android.graphics.Rect) {
                rect.set(bounds)
                w = -1f
                h = -1f
            }

            private fun ensure() {
                if (w == rect.width() && h == rect.height() && shader != null) return
                // A short vertical gradient in the accent hue: the top is more
                // saturated, the base more washed out, matching the surface rule.
                shader = LinearGradient(
                    0f, rect.top, 0f, rect.bottom,
                    intArrayOf(
                        Glass.withAlpha(accent, 56),
                        Glass.withAlpha(accent, 30)
                    ),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
                w = rect.width()
                h = rect.height()
            }

            override fun draw(canvas: Canvas) {
                ensure()
                paint.shader = shader
                canvas.drawRoundRect(rect, radius, radius, paint)
            }

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
                invalidateSelf()
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                paint.colorFilter = colorFilter
                invalidateSelf()
            }

            @Deprecated("Deprecated in Drawable")
            override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
        }
    }

    /**
     * Tint that keeps a glyph legible on glass. Small icons get more ink so they
     * do not dissolve into the frosted panel behind them.
     */
    fun tintFor(context: Context, tintRes: Int, sizeDp: Int): Int {
        val base = context.getColor(tintRes)
        val night = (context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        if (night) return base
        return when {
            sizeDp <= 18 -> Glass.blend(base, android.graphics.Color.BLACK, 0.10f)
            sizeDp <= 24 -> Glass.blend(base, android.graphics.Color.BLACK, 0.05f)
            else -> base
        }
    }

    /**
     * Multi-stop gradient stroke glyph, used for the launcher mark and the
     * primary brand marks. Kept as a vector-friendly helper so the same geometry
     * can be reused as a drawable or painted into a canvas.
     */
    fun paintGradientPath(
        canvas: Canvas,
        path: Path,
        colors: IntArray,
        top: Float,
        bottom: Float,
        strokeWidth: Float = 0f
    ): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(0f, top, 0f, bottom, colors, null, Shader.TileMode.CLAMP)
        style = if (strokeWidth > 0f) Paint.Style.STROKE else Paint.Style.FILL
        this.strokeWidth = strokeWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }.also { canvas.drawPath(path, it) }
}

/**
 * Small utility: attaches a glass surface and keeps the view opaque-free so the
 * backdrop shows through. Centralised here so every screen does it the same way.
 */
fun View.applyGlass(
    context: Context,
    level: Glass.Level = Glass.Level.TWO,
    radius: Glass.Radius = Glass.Radius.LG,
    sheen: Boolean = true
) {
    background = Glass.surface(context, level, radius, sheen = sheen)
    // Translucent surfaces must not be flattened into the parent's background.
    setWillNotDraw(false)
}