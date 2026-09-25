package com.zamcan.nisaacare.domain.cycle

import com.zamcan.nisaacare.domain.model.CycleRecord
import java.time.temporal.ChronoUnit

/**
 * Converts logged observations into non-diagnostic care prompts.
 * These prompts are educational and never establish a condition.
 */
data class CycleCareFlag(
    val key: String,
    val severity: Severity
)

enum class Severity { INFO, REVIEW, URGENT }

object CycleCareAssessment {
    fun evaluate(cycles: List<CycleRecord>): List<CycleCareFlag> {
        val flags = mutableListOf<CycleCareFlag>()

        cycles.forEach { cycle ->
            val duration = cycle.endDate?.let {
                ChronoUnit.DAYS.between(cycle.startDate, it).toInt() + 1
            }
            if (duration != null && duration > 7) {
                flags += CycleCareFlag("PERIOD_OVER_7_DAYS", Severity.REVIEW)
            }
            if (cycle.flow.name == "HEAVY") {
                flags += CycleCareFlag("HEAVY_FLOW_LOGGED", Severity.REVIEW)
            }
        }

        val starts = cycles.map { it.startDate }.distinct().sorted()
        starts.zipWithNext { previous, current ->
            ChronoUnit.DAYS.between(previous, current).toInt()
        }.forEach { length ->
            if (length < 21 || length > 35) {
                flags += CycleCareFlag("CYCLE_OUTSIDE_21_35_DAYS", Severity.REVIEW)
            }
        }

        return flags.distinctBy { it.key }
    }
}
