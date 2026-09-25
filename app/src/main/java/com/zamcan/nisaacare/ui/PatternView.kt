package com.zamcan.nisaacare.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import com.zamcan.nisaacare.R
import kotlin.math.min

class PatternView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = NisaaDesign.dp(context, 1).toFloat()
        color = NisaaDesign.color(context, R.color.nisaa_rose)
        alpha = 24
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val step = min(width, height).coerceAtLeast(1) / 6f
        val path = Path()
        var x = -step
        while (x < width + step) {
            var y = -step
            while (y < height + step) {
                path.reset()
                path.moveTo(x, y + step / 2)
                path.lineTo(x + step / 2, y)
                path.lineTo(x + step, y + step / 2)
                path.lineTo(x + step / 2, y + step)
                path.close()
                canvas.drawPath(path, paint)
                y += step
            }
            x += step
        }
    }
}
