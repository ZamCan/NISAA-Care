package com.zamcan.nisaacare.domain

import com.zamcan.nisaacare.domain.cycle.BiologicalEngine
import com.zamcan.nisaacare.domain.model.Confidence
import com.zamcan.nisaacare.domain.model.CyclePhase
import com.zamcan.nisaacare.domain.model.DataQuality
import com.zamcan.nisaacare.domain.model.CycleRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BiologicalEngineTest {
    private val engine = BiologicalEngine()

    @Test
    fun normalCyclesProduceStableAverageAndPrediction() {
        val insight = engine.calculate(
            listOf(
                cycle("2025-01-01"),
                cycle("2025-01-29"),
                cycle("2025-02-26")
            ),
            LocalDate.parse("2025-03-01")
        )

        assertEquals(2, insight.observedCycleCount)
        assertEquals(28, insight.averageCycleLength)
        assertEquals(0, insight.variabilityDays)
        assertEquals(LocalDate.parse("2025-03-26"), insight.predictedPeriodStart)
        assertEquals(Confidence.MODERATE, insight.confidence)
        assertEquals(4, insight.cycleDay)
        assertEquals(CyclePhase.MENSTRUATION, insight.currentPhase)
        assertEquals(DataQuality.DEVELOPING, insight.dataQuality)
        assertEquals("nisaa-biological-1.0.0", insight.calculationVersion)
    }

    @Test
    fun shortAndLongCyclesRemainEstimates() {
        val insight = engine.calculate(
            listOf(cycle("2025-01-01"), cycle("2025-01-22"), cycle("2025-02-12")),
            LocalDate.parse("2025-02-20")
        )
        assertEquals(21, insight.averageCycleLength)
        assertNotNull(insight.fertileWindowStart)
        assertTrue(insight.confidence == Confidence.MODERATE || insight.confidence == Confidence.LOW)
    }

    @Test
    fun variableCyclesLowerConfidence() {
        val insight = engine.calculate(
            listOf(
                cycle("2025-01-01"),
                cycle("2025-01-22"),
                cycle("2025-02-26"),
                cycle("2025-03-19")
            ),
            LocalDate.parse("2025-03-20")
        )
        assertTrue((insight.variabilityDays ?: 0) >= 7)
        assertEquals(Confidence.LOW, insight.confidence)
    }

    @Test
    fun missingRecordsReturnNoPrediction() {
        val insight = engine.calculate(emptyList(), LocalDate.parse("2025-01-01"))
        assertEquals(0, insight.observedCycleCount)
        assertNull(insight.averageCycleLength)
        assertNull(insight.predictedPeriodStart)
        assertEquals(Confidence.INSUFFICIENT_DATA, insight.confidence)
    }

    @Test
    fun extremeDateDoesNotOverflowEstimate() {
        val insight = engine.calculate(listOf(cycle("+999999999-12-31")), LocalDate.parse("+999999999-12-31"))
        assertNull(insight.predictedPeriodStart)
        assertNull(insight.fertileWindowStart)
    }

    @Test
    fun leapDayBoundaryIsHandled() {
        val insight = engine.calculate(
            listOf(cycle("2024-02-20"), cycle("2024-03-19"), cycle("2024-04-16")),
            LocalDate.parse("2024-04-20")
        )
        assertEquals(28, insight.averageCycleLength)
        assertEquals(LocalDate.parse("2024-05-14"), insight.predictedPeriodStart)
    }

    private fun cycle(date: String) = CycleRecord(userId = "u", startDate = LocalDate.parse(date))
}
