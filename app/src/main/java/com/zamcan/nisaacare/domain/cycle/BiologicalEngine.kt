package com.zamcan.nisaacare.domain.cycle

import com.zamcan.nisaacare.domain.model.Confidence
import com.zamcan.nisaacare.domain.model.CycleInsight
import com.zamcan.nisaacare.domain.model.CycleRecord
import com.zamcan.nisaacare.domain.model.FertilityEstimate
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

object BiologicalEngineVersion {
    const val CURRENT = "nisaa-biological-1.0.0"
    const val HISTORY_RETENTION_NOTE = "Historical estimates retain their algorithm version."
}

class BiologicalEngine {

    /**
     * Computes an educational estimate from explicitly recorded period starts.
     * It intentionally does not infer pregnancy status, diagnose a condition,
     * or provide contraception guarantees.
     */
    fun calculate(
        cycles: List<CycleRecord>,
        referenceDate: LocalDate = LocalDate.now(),
        preferredAverage: Int? = null
    ): CycleInsight {
        val starts = cycles
            .map { it.startDate }
            .filter { !it.isAfter(referenceDate) }
            .distinct()
            .sorted()

        if (starts.isEmpty()) {
            return CycleInsight(
                referenceDate = referenceDate,
                observedCycleCount = 0,
                averageCycleLength = null,
                variabilityDays = null,
                lastPeriodStart = null,
                predictedPeriodStart = null,
                fertileWindowStart = null,
                fertileWindowEnd = null,
                ovulationEstimate = null,
                confidence = Confidence.INSUFFICIENT_DATA,
                guidance = "Add a period start to create a personal estimate."
            )
        }

        val intervals = starts.zipWithNext { previous, current ->
            ChronoUnit.DAYS.between(previous, current).toInt()
        }
        val usableIntervals = intervals.filter { it in MIN_OBSERVED_CYCLE..MAX_OBSERVED_CYCLE }
        val lastPeriodStart = starts.last()
        val average = preferredAverage
            ?.takeIf { it in FALLBACK_AVERAGE_RANGE }
            ?: usableIntervals.takeIf { it.isNotEmpty() }?.average()?.roundToInt()
            ?: FALLBACK_AVERAGE_LENGTH
        val variability = usableIntervals
            .takeIf { it.size >= 2 }
            ?.let { values ->
                val mean = values.average()
                sqrt(values.sumOf { value -> (value - mean) * (value - mean) } / values.size)
                    .roundToInt()
            }
        val predictedStart = safePlusDays(lastPeriodStart, average.toLong())
        val ovulation = predictedStart?.let { safePlusDays(it, -OVULATION_DAYS_BEFORE_NEXT_PERIOD.toLong()) }
        val fertileStart = ovulation?.let { safePlusDays(it, -FERTILE_WINDOW_DAYS_BEFORE_OVULATION.toLong()) }
        val fertileEnd = ovulation?.let { safePlusDays(it, FERTILE_WINDOW_DAYS_AFTER_OVULATION.toLong()) }
        val confidence = confidenceFor(usableIntervals, variability, average)

        return CycleInsight(
            referenceDate = referenceDate,
            observedCycleCount = usableIntervals.size,
            averageCycleLength = average,
            variabilityDays = variability,
            lastPeriodStart = lastPeriodStart,
            predictedPeriodStart = predictedStart,
            fertileWindowStart = fertileStart,
            fertileWindowEnd = fertileEnd,
            ovulationEstimate = ovulation,
            confidence = confidence,
            guidance = when {
                usableIntervals.isEmpty() -> "This is a low-information estimate from one recorded start."
                variability != null && variability >= IRREGULAR_VARIABILITY_DAYS ->
                    "Your recorded pattern varies; discuss persistent concerns with a qualified professional."
                else -> "Estimates are based on your recorded history and may change as you add data."
            }
        )
    }

    fun toEstimate(
        userId: String,
        insight: CycleInsight
    ): FertilityEstimate = FertilityEstimate(
        userId = userId,
        referenceDate = insight.referenceDate,
        predictedPeriodStart = insight.predictedPeriodStart,
        fertileWindowStart = insight.fertileWindowStart,
        fertileWindowEnd = insight.fertileWindowEnd,
        ovulationEstimate = insight.ovulationEstimate,
        averageCycleLength = insight.averageCycleLength,
        variabilityDays = insight.variabilityDays,
        confidence = insight.confidence,
        calculationVersion = insight.calculationVersion
    )

    fun lengthBetween(previousStart: LocalDate, currentStart: LocalDate): Int? {
        val length = ChronoUnit.DAYS.between(previousStart, currentStart).toInt()
        return length.takeIf { it > 0 }
    }

    fun isLikelyIrregular(insight: CycleInsight): Boolean =
        insight.variabilityDays != null &&
            insight.variabilityDays >= IRREGULAR_VARIABILITY_DAYS

    private fun safePlusDays(date: LocalDate, days: Long): LocalDate? =
        runCatching { date.plusDays(days) }.getOrNull()

    private fun confidenceFor(
        intervals: List<Int>,
        variability: Int?,
        average: Int
    ): Confidence {
        if (intervals.isEmpty()) return Confidence.LOW
        if (variability == null) return Confidence.LOW
        if (variability >= IRREGULAR_VARIABILITY_DAYS) return Confidence.LOW
        if (intervals.size >= 6 && variability <= 3 && average in 21..35) {
            return Confidence.HIGHER_HISTORICAL_CONFIDENCE
        }
        return Confidence.MODERATE
    }

    companion object {
        const val MIN_OBSERVED_CYCLE = 15
        const val MAX_OBSERVED_CYCLE = 60
        const val FALLBACK_AVERAGE_LENGTH = 28
        const val OVULATION_DAYS_BEFORE_NEXT_PERIOD = 14
        const val FERTILE_WINDOW_DAYS_BEFORE_OVULATION = 5
        const val FERTILE_WINDOW_DAYS_AFTER_OVULATION = 1
        const val IRREGULAR_VARIABILITY_DAYS = 7
        private val FALLBACK_AVERAGE_RANGE = 21..45
    }
}
