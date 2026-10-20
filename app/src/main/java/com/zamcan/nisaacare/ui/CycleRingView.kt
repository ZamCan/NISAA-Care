package com.zamcan.nisaacare.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import com.zamcan.nisaacare.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * A personal cycle wheel, not a measured biological scan.
 *
 * The wheel separates recorded/estimated cycle phases into a circular timeline.
 * Its phase boundaries are illustrative estimates derived from cycle length;
 * they must never be read as confirmation of ovulation or a contraception method.
 */
class CycleRingView(context: Context) : View(context) {
    var day: Int? = null
        set(value) { field = value; invalidate() }
    var cycleLength: Int? = null
        set(value) { field = value; invalidate() }
    var phase: String = ""
        set(value) { field = value; invalidate() }

    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val segment = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val center = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val marker = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    init {
        minimumHeight = NisaaDesign.dp(context, 190)
        contentDescription = context.getString(R.string.cycle_current)
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rose = NisaaDesign.color(context, R.color.nisaa_rose)
        val terracotta = NisaaDesign.color(context, R.color.nisaa_terracotta)
        val line = NisaaDesign.color(context, R.color.nisaa_line)
        val ink = NisaaDesign.color(context, R.color.nisaa_ink)
        val soft = NisaaDesign.color(context, R.color.nisaa_ink_soft)
        val sage = NisaaDesign.color(context, R.color.nisaa_sage)
        val gold = NisaaDesign.color(context, R.color.nisaa_gold)

        val size = min(width, height).toFloat()
        if (size <= 0f) return
        val cx = width / 2f
        val cy = height / 2f
        val radius = size * 0.315f
        val stroke = (size * 0.075f).coerceAtLeast(8f * resources.displayMetrics.density)
        val oval = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        track.strokeWidth = stroke
        track.color = line
        canvas.drawCircle(cx, cy, radius, track)

        val length = (cycleLength ?: 28).coerceIn(21, 45)
        val current = day?.takeIf { it in 1..length }
        val periodEnd = min(5, length)
        val estimatedOvulation = (length - 14).coerceIn(1, length)
        val fertileStart = (estimatedOvulation - 5).coerceAtLeast(periodEnd + 1)
        val fertileEnd = (estimatedOvulation + 1).coerceAtMost(length)

        // Every arc is a fraction of the whole cycle. Small gaps distinguish
        // phases without suggesting that the biology changes at an exact instant.
        fun arc(startDay: Int, endDay: Int, color: Int) {
            if (endDay < startDay) return
            segment.color = color
            segment.strokeWidth = stroke
            val start = -90f + (startDay - 1).toFloat() / length * 360f + 1.4f
            val sweep = ((endDay - startDay + 1).toFloat() / length * 360f - 2.8f).coerceAtLeast(0f)
            canvas.drawArc(oval, start, sweep, false, segment)
        }

        arc(1, periodEnd, rose)
        arc(periodEnd + 1, fertileStart - 1, terracotta)
        arc(fertileStart, fertileEnd, sage)
        arc(fertileEnd + 1, length, gold)

        // A small dot marks today's estimated position on the wheel.
        if (current != null) {
            val angle = Math.toRadians(-90.0 + ((current - 0.5) / length) * 360.0)
            val dotRadius = radius
            val dx = cx + cos(angle).toFloat() * dotRadius
            val dy = cy + sin(angle).toFloat() * dotRadius
            marker.color = NisaaDesign.color(context, R.color.nisaa_surface)
            canvas.drawCircle(dx, dy, stroke * 0.39f, marker)
            marker.color = ink
            canvas.drawCircle(dx, dy, stroke * 0.24f, marker)
        }

        center.color = ink
        center.textSize = size * 0.19f
        center.typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD)
        canvas.drawText(current?.toString() ?: "—", cx, cy + center.textSize * 0.28f, center)

        sub.color = soft
        sub.textSize = size * 0.066f
        sub.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        canvas.drawText(context.getString(R.string.cycle_day_label), cx, cy + center.textSize * 0.28f + size * 0.105f, sub)

        sub.color = sage
        sub.textSize = size * 0.062f
        canvas.drawText(phase, cx, cy + radius + stroke * 0.9f, sub)

        contentDescription = buildString {
            append(context.getString(R.string.cycle_current))
            append(". ")
            append(if (current == null) "Cycle day unavailable" else "Cycle day $current of $length")
            if (phase.isNotBlank()) {
                append(". ")
                append(phase)
            }
            append(". Phase boundaries are estimates, not confirmation of ovulation.")
        }
    }
}
