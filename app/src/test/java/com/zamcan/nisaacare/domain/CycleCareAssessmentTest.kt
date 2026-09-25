package com.zamcan.nisaacare.domain

import com.zamcan.nisaacare.domain.cycle.CycleCareAssessment
import com.zamcan.nisaacare.domain.model.CycleRecord
import com.zamcan.nisaacare.domain.model.FlowIntensity
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CycleCareAssessmentTest {
    @Test
    fun flagsLoggedPatternsWithoutDiagnosing() {
        val flags = CycleCareAssessment.evaluate(
            listOf(
                CycleRecord(
                    userId = "u",
                    startDate = LocalDate.parse("2026-01-01"),
                    endDate = LocalDate.parse("2026-01-09"),
                    flow = FlowIntensity.HEAVY
                ),
                CycleRecord(userId = "u", startDate = LocalDate.parse("2026-01-25"))
            )
        ).map { it.key }

        assertTrue("PERIOD_OVER_7_DAYS" in flags)
        assertTrue("HEAVY_FLOW_LOGGED" in flags)
        assertTrue("CYCLE_OUTSIDE_21_35_DAYS" in flags)
    }
}
