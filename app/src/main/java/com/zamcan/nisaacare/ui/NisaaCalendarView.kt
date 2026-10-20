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
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * Compact native cycle calendar: circular day states rather than web-style cells.
 * Colours represent recorded period, fertile estimate, other estimates and selection.
 */
class NisaaCalendarView(
    context: Context,
    private val locale: Locale = Locale.getDefault()
) : View(context) {
    var month: YearMonth = YearMonth.now()
        private set
    var selectedDate: LocalDate? = null
        set(value) { field = value; invalidate() }
    var periodDates: Set<LocalDate> = emptySet()
        set(value) { field = value; invalidate() }
    var fertileDates: Set<LocalDate> = emptySet()
        set(value) { field = value; invalidate() }
    var estimatedDates: Set<LocalDate> = emptySet()
        set(value) { field = value; invalidate() }
    var onDateSelected: ((LocalDate) -> Unit)? = null

    private val density = resources.displayMetrics.density
    private val headerHeight = 42f * density
    private val dayLabelHeight = 28f * density
    private val cellHeight = 51f * density
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val monthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD)
    }
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }

    init {
        isFocusable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        minimumHeight = (headerHeight + dayLabelHeight + cellHeight * 6f).toInt()
    }

    fun showMonth(value: YearMonth) { month = value; invalidate() }
    fun previousMonth() = showMonth(month.minusMonths(1))
    fun nextMonth() = showMonth(month.plusMonths(1))

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val surface = NisaaDesign.color(context, R.color.nisaa_surface)
        val ink = NisaaDesign.color(context, R.color.nisaa_ink)
        val softInk = NisaaDesign.color(context, R.color.nisaa_ink_soft)
        val rose = NisaaDesign.color(context, R.color.nisaa_rose)
        val roseSoft = NisaaDesign.color(context, R.color.nisaa_rose_soft)
        val gold = NisaaDesign.color(context, R.color.nisaa_gold)
        val goldSoft = NisaaDesign.color(context, R.color.nisaa_gold_soft)
        val sage = NisaaDesign.color(context, R.color.nisaa_sage)
        val sageSoft = NisaaDesign.color(context, R.color.nisaa_sage_soft)

        canvas.drawColor(surface)
        monthPaint.color = ink
        monthPaint.textSize = 18f * density
        val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", locale)
        canvas.drawText(month.atDay(1).format(monthFormatter), 18f * density, 27f * density, monthPaint)

        textPaint.color = softInk
        textPaint.textSize = 10.5f * density
        val labels = DayOfWeek.entries
        for (index in 0..6) {
            val day = labels[(index + 1) % 7]
            canvas.drawText(day.getDisplayName(TextStyle.SHORT, locale).take(2), cellCenterX(index), headerHeight + 18f * density, textPaint)
        }

        val firstDay = month.atDay(1)
        val offset = firstDay.dayOfWeek.value % 7
        val today = LocalDate.now()
        val daysInMonth = month.lengthOfMonth()

        for (dayNumber in 1..daysInMonth) {
            val date = month.atDay(dayNumber)
            val index = dayNumber + offset - 1
            val row = index / 7
            val column = index % 7
            val cx = cellCenterX(column)
            val cy = headerHeight + dayLabelHeight + row * cellHeight + cellHeight * 0.5f
            val radius = min(width / 7f, cellHeight) * 0.32f

            when {
                date in periodDates -> {
                    circlePaint.color = roseSoft
                    canvas.drawCircle(cx, cy, radius, circlePaint)
                    ringPaint.color = rose
                    canvas.drawCircle(cx, cy, radius, ringPaint)
                }
                date in fertileDates -> {
                    circlePaint.color = sageSoft
                    canvas.drawCircle(cx, cy, radius, circlePaint)
                    ringPaint.color = sage
                    canvas.drawCircle(cx, cy, radius, ringPaint)
                }
                date in estimatedDates -> {
                    circlePaint.color = goldSoft
                    canvas.drawCircle(cx, cy, radius, circlePaint)
                    ringPaint.color = gold
                    canvas.drawCircle(cx, cy, radius, ringPaint)
                }
            }

            if (date == today) {
                todayPaint.color = ink
                canvas.drawCircle(cx, cy, radius + 5f * density, todayPaint)
            }

            if (date == selectedDate) {
                ringPaint.color = rose
                ringPaint.strokeWidth = 3f * density
                canvas.drawCircle(cx, cy, radius + 5f * density, ringPaint)
                ringPaint.strokeWidth = 2f * density
            }

            textPaint.color = ink
            textPaint.textSize = 14f * density
            textPaint.typeface = if (date == today) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
            canvas.drawText(dayNumber.toString(), cx, cy + 5f * density, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        val y = event.y - headerHeight - dayLabelHeight
        if (y < 0) {
            if (event.x < width * 0.25f) previousMonth()
            else if (event.x > width * 0.75f) nextMonth()
            return true
        }
        val column = min(6, max(0, (event.x / (width / 7f)).toInt()))
        val row = max(0, (y / cellHeight).toInt())
        val offset = month.atDay(1).dayOfWeek.value % 7
        val dayNumber = row * 7 + column - offset + 1
        if (dayNumber in 1..month.lengthOfMonth()) {
            val date = month.atDay(dayNumber)
            selectedDate = date
            contentDescription = date.toString()
            onDateSelected?.invoke(date)
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun cellCenterX(column: Int): Float = column * width / 7f + width / 14f
}
