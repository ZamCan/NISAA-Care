package com.zamcan.nisaacare.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import com.zamcan.nisaacare.R

/**
 * Glass surface primitives.
 *
 * This app renders with platform views only, so the glass effect is built from
 * three real layers rather than pulled from a library:
 *
 *  1. Translucent fill. Genuinely alpha-blended, so whatever sits behind the
 *     surface shows through. This is what makes it read as glass rather than as
 *     a pale card.
 *  2. Gradient hairline. The stroke is brighter along the top edge and darker
 *     along the bottom, which is what a bevelled edge does when lit from above.
 *     GradientDrawable cannot express a gradient stroke, hence the custom
 *     drawable below.
 *  3. Specular sheen. A thin bright band just inside the top edge.
 *
 * Depth is expressed by stacking [Level] rather than by elevation, because
 * translucent fills do not receive real shadows. What sells depth here is a
 * stronger fill plus a stronger hairline.
 */
object Glass {

    /**
     * Stacking levels. Alphas step up roughly 12% per level so three nested
     * surfaces stay visually distinct instead of collapsing into one another.
     */
    enum class Level { ONE, TWO, THREE }

    /** Radius scale. Glass reads better with slightly larger radii than flat UI. */
    enum class Radius { PILL, SM, MD, LG, XL }

    fun fillRes(level: Level): Int = when (level) {
        Level.ONE -> R.color.nisaa_glass_fill_1
        Level.TWO -> R.color.nisaa_glass_fill_2
        Level.THREE -> R.color.nisaa_glass_fill_3
    }

    fun strokeTopRes(level: Level): Int = when (level) {
        Level.ONE, Level.TWO -> R.color.nisaa_glass_stroke_top
        Level.THREE -> R.color.nisaa_glass_stroke_strong_top
    }

    fun strokeBottomRes(level: Level): Int = when (level) {
        Level.ONE, Level.TWO -> R.color.nisaa_glass_stroke_bottom
        Level.THREE -> R.color.nisaa_glass_stroke_strong_bottom
    }

    fun radiusDp(radius: Radius): Int = when (radius) {
        Radius.PILL -> 999
        Radius.SM -> 14
        Radius.MD -> 20
        Radius.LG -> 26
        Radius.XL -> 34
    }

    /** Standard glass surface for cards, rows and sheets. */
    fun surface(
        context: Context,
        level: Level = Level.TWO,
        radius: Radius = Radius.LG,
        strokeWidthDp: Int = 1,
        sheen: Boolean = true,
        tintRes: Int? = null,
        tintAlpha: Int = 255
    ): Drawable = SurfaceDrawable(
        fill = context.getColor(fillRes(level)),
        strokeTop = context.getColor(strokeTopRes(level)),
        strokeBottom = context.getColor(strokeBottomRes(level)),
        specular = context.getColor(R.color.nisaa_glass_specular),
        radius = NisaaDesign.dp(context, radiusDp(radius)).toFloat(),
        strokeWidth = NisaaDesign.dp(context, strokeWidthDp).toFloat(),
        sheen = sheen,
        // Tint is blended into the fill rather than applied as a ColorFilter.
        // PorterDuff SRC_IN would replace the alpha gradient outright and flatten
        // the surface back to an opaque swatch, which defeats the whole point.
        tint = tintRes?.let { withAlpha(context.getColor(it), tintAlpha) }
    )

    /**
     * Media well. More transparent than a card so artwork reads clearly while
     * the well still feels like it sits behind glass.
     */
    fun well(context: Context, radius: Radius = Radius.MD): Drawable = SurfaceDrawable(
        fill = context.getColor(R.color.nisaa_glass_well),
        strokeTop = context.getColor(R.color.nisaa_glass_well_stroke),
        strokeBottom = context.getColor(R.color.nisaa_glass_well_stroke),
        specular = context.getColor(R.color.nisaa_glass_specular),
        radius = NisaaDesign.dp(context, radiusDp(radius)).toFloat(),
        strokeWidth = NisaaDesign.dp(context, 1).toFloat(),
        sheen = false
    )

    /**
     * Translucent bar for content pinned over scrolling content. Softer than a
     * card so it does not compete with the content passing underneath.
     */
    fun bar(context: Context, radius: Radius = Radius.PILL): Drawable = SurfaceDrawable(
        fill = context.getColor(R.color.nisaa_glass_bar_scrim),
        strokeTop = context.getColor(R.color.nisaa_glass_stroke_top),
        strokeBottom = context.getColor(R.color.nisaa_glass_stroke_bottom),
        specular = context.getColor(R.color.nisaa_glass_specular),
        radius = NisaaDesign.dp(context, radiusDp(radius)).toFloat(),
        strokeWidth = NisaaDesign.dp(context, 1).toFloat(),
        sheen = true
    )

    /**
     * Drawable for the layered glass look.
     *
     * Shaders are cached and rebuilt only when bounds actually change.
     * Rebuilding per draw would allocate every frame and make scrolling visibly
     * janky, which is the usual way a "premium" glass UI falls apart on a
     * mid-range device.
     */
    internal class SurfaceDrawable(
        private val fill: Int,
        private val strokeTop: Int,
        private val strokeBottom: Int,
        private val specular: Int,
        private val radius: Float,
        private val strokeWidth: Float,
        private val sheen: Boolean,
        private val tint: Int? = null
    ) : Drawable() {

        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        private val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val clipPath = Path()

        private val rect = RectF()
        private var fillShader: Shader? = null
        private var strokeShader: Shader? = null
        private var sheenShader: Shader? = null
        private var shaderWidth = -1f
        private var shaderHeight = -1f

        override fun onBoundsChange(bounds: Rect) {
            rect.set(bounds)
            fillShader = null
            strokeShader = null
            sheenShader = null
            shaderWidth = -1f
            shaderHeight = -1f
        }

        private fun ensureShaders() {
            val w = rect.width()
            val h = rect.height()
            if (w == shaderWidth && h == shaderHeight && fillShader != null) return

            // Fill: barely warmer at the top, cooler at the base. The alpha in
            // [fill] does the real work; this is only a whisper of shaping.
            val base = if (tint != null) blend(fill, tint, 0.34f) else fill
            fillShader = LinearGradient(
                0f, rect.top, 0f, rect.bottom,
                intArrayOf(
                    blend(base, Color.WHITE, 0.06f),
                    base,
                    blend(base, Color.BLACK, 0.05f)
                ),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )

            // Hairline: light at the top edge, dark at the base. This vertical
            // gradient is the whole illusion of a bevelled glass edge.
            strokeShader = LinearGradient(
                0f, rect.top, 0f, rect.bottom,
                intArrayOf(strokeTop, strokeBottom),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )

            // Sheen: short bright band hugging the inside of the top edge.
            if (sheen) {
                val band = (h * 0.42f).coerceAtLeast(radius * 0.5f)
                sheenShader = LinearGradient(
                    0f, rect.top, 0f, rect.top + band,
                    intArrayOf(specular, withAlpha(specular, 0)),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
            }

            shaderWidth = w
            shaderHeight = h
        }

        override fun draw(canvas: Canvas) {
            ensureShaders()

            val inset = strokeWidth / 2f
            val body = RectF(
                rect.left + inset,
                rect.top + inset,
                rect.right - inset,
                rect.bottom - inset
            )
            val corner = (radius - inset).coerceAtLeast(0f)

            fillPaint.shader = fillShader
            canvas.drawRoundRect(body, corner, corner, fillPaint)

            if (sheen && sheenShader != null) {
                // Clip the sheen to the body so it cannot bleed past the curve.
                val save = canvas.save()
                clipPath.reset()
                clipPath.addRoundRect(body, corner, corner, Path.Direction.CW)
                canvas.clipPath(clipPath)
                sheenPaint.shader = sheenShader
                canvas.drawRect(body, sheenPaint)
                canvas.restoreToCount(save)
            }

            strokePaint.shader = strokeShader
            strokePaint.strokeWidth = strokeWidth
            canvas.drawRoundRect(body, corner, corner, strokePaint)
        }

        override fun setAlpha(alpha: Int) {
            fillPaint.alpha = alpha
            strokePaint.alpha = alpha
            sheenPaint.alpha = alpha
            invalidateSelf()
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            fillPaint.colorFilter = colorFilter
            strokePaint.colorFilter = colorFilter
            invalidateSelf()
        }

        @Deprecated("Deprecated in Drawable, still required by the abstract API")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }

    internal fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    internal fun blend(base: Int, target: Int, ratio: Float): Int {
        val r = ratio.coerceIn(0f, 1f)
        return Color.argb(
            Color.alpha(base),
            (Color.red(base) + (Color.red(target) - Color.red(base)) * r).toInt(),
            (Color.green(base) + (Color.green(target) - Color.green(base)) * r).toInt(),
            (Color.blue(base) + (Color.blue(target) - Color.blue(base)) * r).toInt()
        )
    }
}

/**
 * Paints the aurora backdrop that every glass surface refracts.
 *
 * This is a FrameLayout rather than a plain View on purpose. The backdrop is two
 * layers with different blur requirements:
 *
 *  - A gradient layer holding four large radial blobs. These must stay crisp,
 *    because the colour variation between them is what gives each glass panel
 *    something different to refract.
 *  - A streak layer holding two defined light strokes. On API 31+ this child
 *    gets a real RenderEffect blur, so the frosted quality is genuine rather
 *    than painted. Below API 31 the strokes are pre-softened by drawing them
 *    wider and at lower alpha.
 *
 * Both layers are opaque-free apart from the gradient base, so the whole
 * backdrop remains a single composited surface that glass panels can sit over.
 */
class AuroraBackdropView(context: Context) : FrameLayout(context) {

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blobPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val streakPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val baseColor = context.getColor(R.color.nisaa_glass_backdrop_base)
    private val blobColors = intArrayOf(
        context.getColor(R.color.nisaa_glass_blob_rose),
        context.getColor(R.color.nisaa_glass_blob_lilac),
        context.getColor(R.color.nisaa_glass_blob_sage),
        context.getColor(R.color.nisaa_glass_blob_gold)
    )

    /** True below API 31, where streaks are faked rather than truly blurred. */
    private val streaksAreFaked: Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S

    private val gradientLayer = object : View(context) {
        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0f || h <= 0f) return

            canvas.drawRect(0f, 0f, w, h, basePaint)

            // Centres are pushed off the grid so successive cards land on
            // differing hues instead of one flat wash down the whole page.
            val centres = arrayOf(
                floatArrayOf(w * 0.14f, h * 0.06f, w * 0.78f),
                floatArrayOf(w * 0.94f, h * 0.34f, w * 0.72f),
                floatArrayOf(w * 0.84f, h * 0.78f, w * 0.66f),
                floatArrayOf(w * 0.34f, h * 1.02f, w * 0.62f)
            )
            for (i in centres.indices) {
                val c = centres[i]
                blobPaint.shader = RadialGradient(
                    c[0], c[1], c[2],
                    Glass.withAlpha(blobColors[i], 150),
                    Glass.withAlpha(blobColors[i], 0),
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(c[0], c[1], c[2], blobPaint)
            }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            val width = w.toFloat()
            val height = h.toFloat()
            basePaint.shader = LinearGradient(
                0f, 0f, 0f, height,
                intArrayOf(
                    Glass.blend(baseColor, Color.WHITE, 0.04f),
                    baseColor,
                    Glass.blend(baseColor, Color.BLACK, 0.05f)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
    }

    private val streakLayer = object : View(context) {
        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0f || h <= 0f) return
            streakPaint.style = Paint.Style.STROKE
            streakPaint.strokeCap = Paint.Cap.ROUND

            // Without a real blur the strokes are widened and dimmed so they
            // still read as soft light rather than as hard arcs.
            val widthScale = if (streaksAreFaked) 1.8f else 1f
            val alphaScale = if (streaksAreFaked) 0.5f else 1f

            streakPaint.strokeWidth = h * 0.16f * widthScale
            streakPaint.color = Glass.withAlpha(Color.WHITE, (90 * alphaScale).toInt())
            canvas.drawArc(
                w * 0.06f, -h * 0.28f, w * 1.02f, h * 0.72f,
                196f, 84f, false, streakPaint
            )

            streakPaint.strokeWidth = h * 0.11f * widthScale
            streakPaint.color = Glass.withAlpha(Color.WHITE, (60 * alphaScale).toInt())
            canvas.drawArc(
                -w * 0.18f, h * 0.28f, w * 0.86f, h * 1.24f,
                8f, 74f, false, streakPaint
            )
        }
    }

    init {
        addView(
            gradientLayer,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        addView(
            streakLayer,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val radius = NisaaDesign.dp(context, 34).toFloat()
            streakLayer.setRenderEffect(
                RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
            )
        }
    }

    /**
     * Glass is only convincing if the surface behind it actually varies. Cards
     * clip their children by default, so this keeps the blur edges crisp.
     */
    init {
        setWillNotDraw(true)
    }
}