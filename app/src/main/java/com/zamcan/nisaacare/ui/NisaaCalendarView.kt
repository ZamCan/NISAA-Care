package com.zamcan.nisaacare.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.zamcan.nisaacare.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class NisaaCalendarView(
    context: Context,
    private val locale: Locale = Locale.getDefault()
) : View(context) {
    var month: YearMonth = YearMonth.now()
        private set
    var selectedDate: LocalDate? = null
        set(value) {
            field = value
            invalidate()
        }
    var periodDates: Set<LocalDate> = emptySet()
        set(value) {
            field = value
            invalidate()
        }
    var estimatedDates: Set<LocalDate> = emptySet()
        set(value) {
            field = value
            invalidate()
        }
    var onDateSelected: ((LocalDate) -> Unit)? = null

    private val density = resources.displayMetrics.density
    private val cellHeight = 58f * density
    private val headerHeight = 36f * density
    private val dayLabelHeight = 28f * density
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val monthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }
    private val surfacePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val periodPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val estimatePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        isFocusable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        minimumHeight = (headerHeight + dayLabelHeight + cellHeight * 6f).toInt()
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun showMonth(value: YearMonth) {
        month = value
        invalidate()
    }

    fun previousMonth() = showMonth(month.minusMonths(1))

    fun nextMonth() = showMonth(month.plusMonths(1))

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val background = NisaaDesign.color(context, R.color.nisaa_surface)
        val ink = NisaaDesign.color(context, R.color.nisaa_ink)
        val softInk = NisaaDesign.color(context, R.color.nisaa_ink_soft)
        val rose = NisaaDesign.color(context, R.color.nisaa_rose)
        val roseSoft = NisaaDesign.color(context, R.color.nisaa_rose_soft)
        val goldSoft = NisaaDesign.color(context, R.color.nisaa_gold_soft)
        surfacePaint.color = background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), surfacePaint)

        monthPaint.color = ink
        monthPaint.textSize = 19f * density
        val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", locale)
        canvas.drawText(month.atDay(1).format(monthFormatter), 18f * density, 25f * density, monthPaint)

        textPaint.color = softInk
        textPaint.textSize = 11f * density
        val firstDay = month.atDay(1)
        val offset = firstDay.dayOfWeek.value % 7
        val labels = DayOfWeek.entries
        for (index in 0..6) {
            val day = labels[(index + 1) % 7]
            val x = cellCenterX(index)
            val label = day.getDisplayName(TextStyle.SHORT, locale)
            canvas.drawText(label.take(3), x, headerHeight + 18f * density, textPaint)
        }

        val daysInMonth = month.lengthOfMonth()
        for (dayNumber in 1..daysInMonth) {
            val date = month.atDay(dayNumber)
            val index = dayNumber + offset - 1
            val row = index / 7
            val column = index % 7
            val left = cellLeft(column) + 3f * density
            val top = headerHeight + dayLabelHeight + row * cellHeight + 3f * density
            val right = cellLeft(column) + width / 7f - 3f * density
            val bottom = top + cellHeight - 6f * density
            val rect = RectF(left, top, right, bottom)

            if (date in periodDates) {
                periodPaint.color = roseSoft
                canvas.drawRoundRect(rect, 14f * density, 14f * density, periodPaint)
            } else if (date in estimatedDates) {
                estimatePaint.color = goldSoft
                canvas.drawRoundRect(rect, 14f * density, 14f * density, estimatePaint)
            }
            if (date == selectedDate) {
                selectedPaint.color = rose
                canvas.drawRoundRect(rect, 14f * density, 14f * density, selectedPaint)
            }
            textPaint.color = if (date == selectedDate) NisaaDesign.color(context, R.color.nisaa_white) else ink
            textPaint.textSize = 15f * density
            canvas.drawText(dayNumber.toString(), cellCenterX(column), top + 29f * density, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        val y = event.y - headerHeight - dayLabelHeight
        if (y < 0) {
            val headerWidth = width / 3f
            when {
                event.x < headerWidth -> previousMonth()
                event.x > width - headerWidth -> nextMonth()
            }
            return true
        }
        val column = min(6, max(0, (event.x / (width / 7f)).toInt()))
        val row = max(0, (y / cellHeight).toInt())
        val offset = month.atDay(1).dayOfWeek.value % 7
        val dayNumber = row * 7 + column - offset + 1
        if (dayNumber in 1..month.lengthOfMonth()) {
            val date = month.atDay(dayNumber)
            selectedDate = date
            onDateSelected?.invoke(date)
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun cellLeft(column: Int): Float = column * width / 7f
    private fun cellCenterX(column: Int): Float = cellLeft(column) + width / 14f
}
