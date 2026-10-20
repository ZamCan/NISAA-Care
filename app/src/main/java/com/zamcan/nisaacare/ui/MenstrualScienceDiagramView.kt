package com.zamcan.nisaacare.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import com.zamcan.nisaacare.R
import com.zamcan.nisaacare.domain.health.LearningVisual
import kotlin.math.min

class MenstrualScienceDiagramView(context: Context, private val visual: LearningVisual) : View(context) {
    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density
    private val ink = NisaaDesign.color(context, R.color.nisaa_ink)
    private val softInk = NisaaDesign.color(context, R.color.nisaa_ink_soft)
    private val rose = NisaaDesign.color(context, R.color.nisaa_rose)
    private val roseSoft = NisaaDesign.color(context, R.color.nisaa_rose_soft)
    private val sage = NisaaDesign.color(context, R.color.nisaa_sage)
    private val sageSoft = NisaaDesign.color(context, R.color.nisaa_sage_soft)
    private val gold = NisaaDesign.color(context, R.color.nisaa_gold)
    private val goldSoft = NisaaDesign.color(context, R.color.nisaa_gold_soft)
    private val terracotta = NisaaDesign.color(context, R.color.nisaa_terracotta)
    private val line = NisaaDesign.color(context, R.color.nisaa_line)
    private val surface = NisaaDesign.color(context, R.color.nisaa_surface)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        minimumHeight = dp(178f).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        when (visual) {
            LearningVisual.CYCLE_RING -> drawCycle(canvas)
            LearningVisual.UTERUS_FLOW -> drawUterus(canvas)
            LearningVisual.HORMONE_RHYTHM -> drawHormones(canvas)
            LearningVisual.CARE_SIGNAL -> drawCare(canvas)
            LearningVisual.LIFE_COURSE -> drawLife(canvas)
        }
    }

    private fun circle(c: Canvas, x: Float, y: Float, r: Float, color: Int, stroke: Boolean = false) {
        paint.color = color
        paint.style = if (stroke) Paint.Style.STROKE else Paint.Style.FILL
        paint.strokeWidth = dp(2f)
        c.drawCircle(x, y, r, paint)
    }

    private fun label(c: Canvas, text: String, x: Float, y: Float, size: Float = 12f, color: Int = ink) {
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.textSize = dp(size)
        paint.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        c.drawText(text, x, y, paint)
    }

    private fun drawCycle(c: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) * .30f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(17f)
        paint.strokeCap = Paint.Cap.ROUND
        val rect = RectF(cx-radius, cy-radius, cx+radius, cy+radius)
        paint.color = roseSoft; c.drawArc(rect, -90f, 86f, false, paint)
        paint.color = terracotta; c.drawArc(rect, -4f, 94f, false, paint)
        paint.color = sageSoft; c.drawArc(rect, 90f, 74f, false, paint)
        paint.color = goldSoft; c.drawArc(rect, 164f, 106f, false, paint)
        paint.strokeCap = Paint.Cap.BUTT
        circle(c, cx, cy, radius-dp(20f), surface)
        label(c, "21–35", cx, cy-dp(2f), 23f)
        label(c, "days", cx, cy+dp(18f), 11f, softInk)
        label(c, "period", cx, cy-radius-dp(20f), 10f, rose)
        label(c, "follicular", cx+radius+dp(42f), cy+dp(3f), 9f, terracotta)
        label(c, "ovulation", cx, cy+radius+dp(28f), 9f, sage)
        label(c, "luteal", cx-radius-dp(38f), cy+dp(3f), 9f, gold)
    }

    private fun drawUterus(c: Canvas) {
        val cx = width / 2f
        val top = height * .18f
        paint.style = Paint.Style.FILL
        paint.color = roseSoft
        val body = Path().apply {
            moveTo(cx, top)
            cubicTo(cx-dp(62f), top-dp(8f), cx-dp(72f), top+dp(62f), cx-dp(42f), top+dp(94f))
            cubicTo(cx-dp(25f), top+dp(112f), cx+dp(25f), top+dp(112f), cx+dp(42f), top+dp(94f))
            cubicTo(cx+dp(72f), top+dp(62f), cx+dp(62f), top-dp(8f), cx, top)
            close()
        }
        c.drawPath(body, paint)
        paint.color = terracotta
        c.drawRoundRect(RectF(cx-dp(10f), top+dp(92f), cx+dp(10f), top+dp(138f)), dp(8f), dp(8f), paint)
        paint.color = rose
        c.drawOval(RectF(cx-dp(50f), top+dp(34f), cx+dp(50f), top+dp(78f)), paint)
        paint.color = gold
        circle(c, cx-dp(75f), top+dp(20f), dp(11f), gold)
        circle(c, cx+dp(75f), top+dp(20f), dp(11f), gold)
        paint.color = softInk
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2f)
        c.drawArc(RectF(cx-dp(112f), top-dp(3f), cx-dp(38f), top+dp(43f)), 210f, 110f, false, paint)
        c.drawArc(RectF(cx+dp(38f), top-dp(3f), cx+dp(112f), top+dp(43f)), -140f, 110f, false, paint)
        paint.style = Paint.Style.FILL
        label(c, "uterus", cx, height-dp(24f), 11f)
        label(c, "ovaries", cx-dp(83f), top-dp(8f), 9f, softInk)
        label(c, "lining", cx, top+dp(62f), 9f, surface)
    }

    private fun drawHormones(c: Canvas) {
        val left = dp(24f)
        val right = width-dp(24f)
        val top = dp(26f)
        val bottom = height-dp(38f)
        paint.color = line
        paint.strokeWidth = dp(1f)
        c.drawLine(left, bottom, right, bottom, paint)
        val curves = listOf(
            rose to floatArrayOf(.05f,.08f,.18f,.58f,.95f,.62f,.30f,.15f),
            sage to floatArrayOf(.08f,.10f,.18f,.35f,.58f,.90f,.72f,.28f),
            gold to floatArrayOf(.75f,.62f,.40f,.18f,.12f,.25f,.58f,.72f)
        )
        curves.forEach { pair ->
            val p = Path()
            pair.second.forEachIndexed { index, value ->
                val x = left+(right-left)*index/(pair.second.size-1)
                val y = bottom-(bottom-top)*value
                if (index == 0) p.moveTo(x,y) else p.lineTo(x,y)
            }
            paint.color = pair.first
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(3f)
            paint.strokeCap = Paint.Cap.ROUND
            c.drawPath(p, paint)
        }
        paint.style = Paint.Style.FILL
        label(c, "cycle rhythm", width/2f, height-dp(12f), 10f, softInk)
        circle(c,left+dp(8f),top,dp(5f),rose); label(c,"estrogen",left+dp(48f),top+dp(4f),9f,rose)
        circle(c,left+dp(8f),top+dp(22f),dp(5f),sage); label(c,"LH / FSH",left+dp(45f),top+dp(26f),9f,sage)
        circle(c,left+dp(8f),top+dp(44f),dp(5f),gold); label(c,"progesterone",left+dp(62f),top+dp(48f),9f,gold)
    }

    private fun drawCare(c: Canvas) {
        val cx = width/2f
        val cy = height/2f
        val points = arrayOf(
            floatArrayOf(cx,cy-dp(48f)), floatArrayOf(cx+dp(78f),cy+dp(8f)),
            floatArrayOf(cx,cy+dp(62f)), floatArrayOf(cx-dp(78f),cy+dp(8f))
        )
        val colors = intArrayOf(rose,sage,gold,terracotta)
        points.forEachIndexed { i, p ->
            circle(c,p[0],p[1],dp(28f),colors[i])
            circle(c,p[0],p[1],dp(28f),surface,true)
        }
        label(c,"body",cx,cy-dp(78f),9f,softInk)
        label(c,"support",cx+dp(78f),cy+dp(48f),9f,softInk)
        label(c,"track",cx,cy+dp(103f),9f,softInk)
        label(c,"care",cx-dp(78f),cy+dp(48f),9f,softInk)
        paint.color=line; paint.strokeWidth=dp(2f)
        c.drawLine(cx,cy-dp(20f),cx,cy+dp(34f),paint)
        c.drawLine(cx-dp(52f),cy+dp(8f),cx+dp(52f),cy+dp(8f),paint)
        circle(c,cx,cy+dp(8f),dp(23f),roseSoft)
        label(c,"NISAA",cx,cy+dp(12f),10f)
    }

    private fun drawLife(c: Canvas) {
        val fractions = floatArrayOf(.14f,.37f,.63f,.86f)
        val colors = intArrayOf(rose,terracotta,sage,gold)
        val names = arrayOf("first period","reproductive","TTC / pregnancy","perimenopause")
        fractions.forEachIndexed { i, f ->
            val x = width*f
            val y = height*.47f
            circle(c,x,y,dp(23f),colors[i])
            label(c,(i+1).toString(),x,y+dp(5f),12f,surface)
            label(c,names[i],x,y+dp(48f),8f,softInk)
            if (i < fractions.lastIndex) {
                paint.color=line; paint.strokeWidth=dp(2f)
                c.drawLine(x+dp(25f),y,fractions[i+1]*width-dp(25f),y,paint)
            }
        }
        label(c,"one body • changing seasons",width/2f,height-dp(12f),9f,softInk)
    }
}
