package com.zamcan.nisaacare.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import com.zamcan.nisaacare.R
import kotlin.math.min

class CycleRingView(context: Context) : View(context) {
    var day: Int? = null
        set(value) { field = value; invalidate() }
    var cycleLength: Int? = null
        set(value) { field = value; invalidate() }
    var phase: String = ""
        set(value) { field = value; invalidate() }

    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val progress = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val center = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    init {
        minimumHeight = NisaaDesign.dp(context, 184)
        contentDescription = context.getString(R.string.cycle_current)
        isFocusable = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rose = NisaaDesign.color(context, R.color.nisaa_rose)
        val line = NisaaDesign.color(context, R.color.nisaa_line)
        val ink = NisaaDesign.color(context, R.color.nisaa_ink)
        val soft = NisaaDesign.color(context, R.color.nisaa_ink_soft)
        val sage = NisaaDesign.color(context, R.color.nisaa_sage)

        val size = min(width, height).toFloat()
        val cx = width / 2f
        val cy = height / 2f
        val radius = size * 0.34f
        track.strokeWidth = size * 0.055f
        progress.strokeWidth = size * 0.055f
        track.color = line
        progress.color = rose
        canvas.drawCircle(cx, cy, radius, track)

        val length = (cycleLength ?: 28).coerceIn(21, 45)
        val current = (day ?: 0).coerceIn(0, length)
        val sweep = if (current > 0) (current.toFloat() / length) * 360f else 0f
        canvas.drawArc(RectF(cx-radius, cy-radius, cx+radius, cy+radius), -90f, sweep, false, progress)

        center.color = ink
        center.textSize = size * 0.17f
        center.typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD)
        canvas.drawText(if (day == null) "—" else day.toString(), cx, cy + center.textSize * 0.35f, center)

        sub.color = soft
        sub.textSize = size * 0.07f
        sub.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL)
        canvas.drawText(context.getString(R.string.cycle_day_label), cx, cy + center.textSize * 0.35f + size * 0.12f, sub)

        sub.color = sage
        sub.textSize = size * 0.065f
        canvas.drawText(phase, cx, cy + radius + size * 0.13f, sub)
    }
}
