package com.zamcan.nisaacare

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import com.zamcan.nisaacare.core.localization.LocaleManager
import com.zamcan.nisaacare.data.content.SeedContent
import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.ContentItem
import com.zamcan.nisaacare.domain.model.ContentType
import com.zamcan.nisaacare.domain.model.CycleRecord
import com.zamcan.nisaacare.domain.model.DomainResult
import com.zamcan.nisaacare.domain.model.FlowIntensity
import com.zamcan.nisaacare.domain.model.PermissionKey
import com.zamcan.nisaacare.domain.model.Relationship
import com.zamcan.nisaacare.domain.model.ReviewState
import com.zamcan.nisaacare.domain.model.UserRole
import com.zamcan.nisaacare.ui.NisaaCalendarView
import com.zamcan.nisaacare.ui.NisaaDesign
import com.zamcan.nisaacare.ui.PatternView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

internal fun MainActivity.addSpaced(parent: LinearLayout, child: View, topDp: Int = 12) {
    parent.addView(child)
    parent.addView(NisaaDesign.space(this, 0, topDp))
}

internal fun MainActivity.standardScreen(
    title: CharSequence,
    onBack: () -> Unit = { navigate(AppScreen.HOME) },
    buildBody: (LinearLayout) -> Unit
): View {
    val root = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(NisaaDesign.color(this@standardScreen, R.color.nisaa_cream))
    }
    root.addView(NisaaDesign.header(this, title, onBack))
    val scroll = NisaaDesign.screenScroll(this)
    val body = scroll.getChildAt(0) as LinearLayout
    buildBody(body)
    root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    return root
}

internal fun MainActivity.cardTitle(body: LinearLayout, title: CharSequence, subtitle: CharSequence? = null) {
    body.addView(NisaaDesign.eyebrow(this, title))
    if (subtitle != null) {
        body.addView(NisaaDesign.space(this, 0, 7))
        body.addView(NisaaDesign.body(this, subtitle))
    }
    body.addView(NisaaDesign.space(this, 0, 12))
}

private fun MainActivity.metricRow(label: CharSequence, value: CharSequence) {
    addViewWithTop(
        NisaaDesign.dividerLabel(this, label, value),
        10
    )
}

private fun MainActivity.addViewWithTop(view: View, topDp: Int) {
    NisaaDesign.setMargins(view, this, 0, NisaaDesign.dp(this, topDp), 0, 0)
}

private fun MainActivity.infoCard(
    title: CharSequence,
    bodyText: CharSequence,
    iconRes: Int? = null,
    soft: Boolean = false
): LinearLayout = (if (soft) NisaaDesign.softCard(this, 20) else NisaaDesign.card(this, 20)).apply {
    if (iconRes != null) {
        addView(NisaaDesign.icon(this@infoCard, iconRes, R.color.nisaa_rose, 28))
        addView(NisaaDesign.space(this@infoCard, 0, 10))
    }
    addView(NisaaDesign.text(this@infoCard, title, 18f, R.color.nisaa_ink, true))
    addView(NisaaDesign.space(this@infoCard, 0, 7))
    addView(NisaaDesign.body(this@infoCard, bodyText))
}

private fun MainActivity.contentTopicLabel(topic: String): String = when (topic) {
    "MENSTRUATION" -> getString(R.string.cycle_title)
    "MENSTRUAL_CYCLE" -> getString(R.string.cycle_title)
    "PMS" -> getString(R.string.topic_pms)
    "OVULATION" -> getString(R.string.ovulation_estimate)
    "FERTILITY" -> getString(R.string.fertility_title)
    "REPRODUCTIVE_HEALTH" -> getString(R.string.health_title)
    "COMMON_SYMPTOMS" -> getString(R.string.symptoms_title)
    "CYCLE_IRREGULARITY" -> getString(R.string.irregular_note)
    "PREGNANCY_BASICS" -> getString(R.string.health_title)
    "NIFAS_HEALTH_EDUCATION" -> getString(R.string.faith_title)
    "EMOTIONAL_WELLBEING" -> getString(R.string.home_wellbeing)
    "SEEK_PROFESSIONAL_CARE" -> getString(R.string.health_title)
    "QURAN" -> getString(R.string.quran_title)
    "HADITH" -> getString(R.string.hadith_title)
    "HAYD" -> getString(R.string.topic_hayd)
    "NIFAS" -> getString(R.string.topic_nifas)
    "TAHARAH" -> getString(R.string.topic_taharah)
    "GHUSL" -> getString(R.string.topic_ghusl)
    "SALAH" -> getString(R.string.topic_salah)
    "SAWM" -> getString(R.string.topic_sawm)
    "RAMADAN" -> getString(R.string.topic_ramadan)
    "DHIKR" -> getString(R.string.topic_dhikr)
    "DUA" -> getString(R.string.topic_dua)
    "MARRIAGE" -> getString(R.string.marriage_title)
    "FAMILY_RESPONSIBILITIES" -> getString(R.string.husband_education)
    "HAJJ_UMRAH" -> getString(R.string.topic_hajj_umrah)
    "KINDNESS" -> getString(R.string.topic_kindness)
    "COMMUNICATION" -> getString(R.string.topic_communication)
    "FAMILY_RESPONSIBILITY" -> getString(R.string.topic_family_responsibility)
    "EMOTIONAL_SUPPORT" -> getString(R.string.topic_emotional_support)
    else -> topic.replace('_', ' ').lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() }
}

private fun MainActivity.reviewLabel(state: ReviewState): String = when (state) {
    ReviewState.DRAFT -> getString(R.string.state_empty)
    ReviewState.REVIEW_REQUIRED -> getString(R.string.content_review_required)
    ReviewState.VERIFIED -> getString(R.string.content_verified)
    ReviewState.PUBLISHED -> getString(R.string.content_verified)
    ReviewState.RETIRED -> getString(R.string.content_retired)
}

internal fun MainActivity.buildWomanHome(): View {
    val root = FrameLayout(this)
    val pattern = PatternView(this).apply {
        contentDescription = getString(R.string.app_description)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    root.addView(pattern, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, NisaaDesign.dp(this, 138)))
    val scroll = NisaaDesign.screenScroll(this)
    val body = scroll.getChildAt(0) as LinearLayout
    body.setPadding(NisaaDesign.dp(this, 20), NisaaDesign.dp(this, 22), NisaaDesign.dp(this, 20), NisaaDesign.dp(this, 30))
    val current = profile
    val name = current?.displayName?.takeIf { it.isNotBlank() }
    body.addView(
        NisaaDesign.heroCard(
            this,
            getString(R.string.home_today),
            if (name == null) getString(R.string.home_greeting_generic) else getString(R.string.home_greeting, name),
            getString(R.string.app_tagline),
            R.drawable.nisaa_hero_wellness
        )
    )
    body.addView(NisaaDesign.space(this, 0, 18))

    val insight = calculateInsight()
    val cycleCard = NisaaDesign.card(this, 20)
    cycleCard.addView(NisaaDesign.eyebrow(this, getString(R.string.home_cycle_title)))
    cycleCard.addView(NisaaDesign.space(this, 0, 9))
    if (insight.lastPeriodStart == null) {
        cycleCard.addView(NisaaDesign.text(this, getString(R.string.home_no_cycle_data), 17f, R.color.nisaa_ink, true))
        cycleCard.addView(NisaaDesign.space(this, 0, 14))
        cycleCard.addView(NisaaDesign.primaryButton(this, getString(R.string.home_record_period), R.drawable.ic_add) {
            resetEntryState()
            navigate(AppScreen.CYCLE_ENTRY)
        })
    } else {
        cycleCard.addView(NisaaDesign.text(this, getString(R.string.last_period_start, dateText(insight.lastPeriodStart)), 17f, R.color.nisaa_ink, true))
        cycleCard.addView(NisaaDesign.space(this, 0, 8))
        cycleCard.addView(NisaaDesign.body(this, getString(R.string.estimate_guidance)))
        cycleCard.addView(NisaaDesign.space(this, 0, 12))
        cycleCard.addView(NisaaDesign.statusPill(this, confidenceLabel(insight.confidence), insight.confidence != com.zamcan.nisaacare.domain.model.Confidence.INSUFFICIENT_DATA))
        cycleCard.addView(NisaaDesign.space(this, 0, 14))
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val fertilityButton = NisaaDesign.secondaryButton(this, getString(R.string.home_view_fertility), R.drawable.ic_fertility) { navigate(AppScreen.FERTILITY) }
        fertilityButton.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        row.addView(fertilityButton)
        row.addView(NisaaDesign.space(this, 8, 1))
        val addButton = NisaaDesign.outlineButton(this, getString(R.string.cycle_add_record), R.drawable.ic_add) {
            resetEntryState()
            navigate(AppScreen.CYCLE_ENTRY)
        }
        addButton.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        row.addView(addButton)
        cycleCard.addView(row)
    }
    addSpaced(body, cycleCard, 0)

    val wellbeing = infoCard(
        getString(R.string.home_wellbeing),
        getString(R.string.health_intro),
        R.drawable.ic_health,
        soft = true
    )
    wellbeing.addView(NisaaDesign.space(this, 0, 13))
    wellbeing.addView(NisaaDesign.secondaryButton(this, getString(R.string.home_wellbeing), R.drawable.ic_chevron_right) { navigate(AppScreen.HEALTH) })
    addSpaced(body, wellbeing, 14)

    val faith = infoCard(
        getString(R.string.home_faith),
        getString(R.string.faith_intro),
        R.drawable.ic_book,
        soft = true
    )
    faith.addView(NisaaDesign.space(this, 0, 13))
    faith.addView(NisaaDesign.secondaryButton(this, getString(R.string.faith_title), R.drawable.ic_chevron_right) { navigate(AppScreen.FAITH) })
    addSpaced(body, faith, 14)

    val reflection = infoCard(
        getString(R.string.quotation_title),
        getString(R.string.content_quotation_placeholder),
        R.drawable.ic_faith
    )
    reflection.addView(NisaaDesign.space(this, 0, 13))
    reflection.addView(NisaaDesign.outlineButton(this, getString(R.string.content_open_topic), R.drawable.ic_chevron_right) { navigate(AppScreen.QUOTATION) })
    addSpaced(body, reflection, 14)

    if (!isOnline()) {
        addSpaced(body, NisaaDesign.statusPill(this, getString(R.string.state_offline), false), 0)
    }
    addSpaced(body, NisaaDesign.body(this, getString(R.string.home_health_disclaimer)), 18)
    root.addView(scroll, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    return root
}

internal fun MainActivity.buildCycleScreen(): View = standardScreen(getString(R.string.cycle_title)) { body ->
    cardTitle(body, getString(R.string.cycle_current), getString(R.string.home_health_disclaimer))
    val insight = calculateInsight()
    val summary = NisaaDesign.card(this, 18)
    if (insight.lastPeriodStart == null) {
        summary.addView(NisaaDesign.text(this, getString(R.string.cycle_no_records), 18f, R.color.nisaa_ink, true))
        summary.addView(NisaaDesign.space(this, 0, 12))
        summary.addView(NisaaDesign.primaryButton(this, getString(R.string.cycle_add_record), R.drawable.ic_add) {
            resetEntryState()
            navigate(AppScreen.CYCLE_ENTRY)
        })
    } else {
        summary.addView(NisaaDesign.body(this, getString(R.string.last_period_start, dateText(insight.lastPeriodStart))))
        addViewWithTop(NisaaDesign.dividerLabel(this, getString(R.string.predicted_period_start), dateText(insight.predictedPeriodStart)), 12)
        addViewWithTop(NisaaDesign.dividerLabel(this, getString(R.string.confidence_low), confidenceLabel(insight.confidence)), 10)
    }
    addSpaced(body, summary, 0)
    body.addView(NisaaDesign.primaryButton(this, getString(R.string.cycle_add_record), R.drawable.ic_add) {
        resetEntryState()
        navigate(AppScreen.CYCLE_ENTRY)
    })
    body.addView(NisaaDesign.space(this, 0, 24))

    val cycles = profile?.id?.let { repository.getCycles(it) }.orEmpty()
    val periodDates = cycles.flatMap { cycle ->
        val end = cycle.endDate ?: cycle.startDate
        generateSequence(cycle.startDate) { current -> current.plusDays(1) }
            .takeWhile { it <= end }
            .toList()
    }.toSet()
    val estimatedDates = buildSet {
        insight.predictedPeriodStart?.let { addAll(generateSequence(it) { d -> d.plusDays(1) }.take(5).toList()) }
        if (insight.fertileWindowStart != null && insight.fertileWindowEnd != null) {
            addAll(generateSequence(insight.fertileWindowStart) { d -> d.plusDays(1) }.takeWhile { it <= insight.fertileWindowEnd }.toList())
        }
    }
    val recordedPeriodDates = periodDates
    val patternDates = estimatedDates
    val calendarCard = NisaaDesign.card(this, 14)
    val calendarHeader = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    calendarHeader.addView(NisaaDesign.text(this, getString(R.string.cycle_calendar), 18f, R.color.nisaa_ink, true).apply {
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    val previousMonthButton = NisaaDesign.iconButton(this, R.drawable.ic_chevron_right, getString(R.string.onboarding_back)) {
        calendarMonth = calendarMonth.minusMonths(1)
    }
    previousMonthButton.rotation = 180f
    calendarHeader.addView(previousMonthButton)
    calendarHeader.addView(NisaaDesign.iconButton(this, R.drawable.ic_chevron_right, getString(R.string.onboarding_continue)) {
        calendarMonth = calendarMonth.plusMonths(1)
    })
    calendarCard.addView(calendarHeader)
    val calendar = NisaaCalendarView(this, resources.configuration.locales[0]).apply {
        showMonth(this@buildCycleScreen.calendarMonth)
        this.periodDates = recordedPeriodDates
        this.estimatedDates = patternDates
        contentDescription = getString(R.string.cycle_calendar)
        onDateSelected = { date ->
            selectedCycleStart = date
            selectedCycleEnd = null
            selectedFlow = FlowIntensity.UNKNOWN
            selectedSymptoms.clear()
            navigate(AppScreen.CYCLE_ENTRY)
        }
    }
    calendarCard.addView(calendar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, NisaaDesign.dp(this, 400)))
    val legends = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    legends.addView(NisaaDesign.statusPill(this, getString(R.string.calendar_period_legend), false))
    legends.addView(NisaaDesign.space(this, 8, 1))
    legends.addView(NisaaDesign.statusPill(this, getString(R.string.calendar_predicted_legend), true))
    calendarCard.addView(legends)
    addSpaced(body, calendarCard, 20)

    cardTitle(body, getString(R.string.cycle_history))
    if (cycles.isEmpty()) {
        addSpaced(body, NisaaDesign.statePanel(this, getString(R.string.state_empty), getString(R.string.cycle_no_records), R.drawable.ic_cycle), 0)
    } else {
        cycles.forEach { cycle -> addSpaced(body, cycleHistoryCard(cycle), 10) }
    }
}

private fun MainActivity.cycleHistoryCard(cycle: CycleRecord): LinearLayout = NisaaDesign.card(this, 16).apply {
    addView(NisaaDesign.text(this@cycleHistoryCard, dateText(cycle.startDate), 16f, R.color.nisaa_ink, true))
    addView(NisaaDesign.space(this@cycleHistoryCard, 0, 5))
    addView(NisaaDesign.body(this@cycleHistoryCard, getString(R.string.flow_intensity) + ": " + flowLabel(cycle.flow)))
    if (cycle.symptoms.isNotEmpty()) {
        addView(NisaaDesign.space(this@cycleHistoryCard, 0, 5))
        addView(NisaaDesign.body(this@cycleHistoryCard, cycle.symptoms.joinToString(" • ")))
    }
}

internal fun MainActivity.buildFertilityScreen(): View = standardScreen(getString(R.string.fertility_title)) { body ->
    val insight = calculateInsight()
    addSpaced(body, NisaaDesign.statusPill(this, confidenceLabel(insight.confidence), insight.confidence != com.zamcan.nisaacare.domain.model.Confidence.INSUFFICIENT_DATA), 0)
    body.addView(NisaaDesign.space(this, 0, 12))
    val disclaimer = infoCard(getString(R.string.fertility_title), getString(R.string.fertility_disclaimer), R.drawable.ic_shield, soft = true)
    addSpaced(body, disclaimer, 0)
    val metrics = NisaaDesign.card(this, 20)
    metrics.addView(NisaaDesign.eyebrow(this, getString(R.string.content_rotation_title)))
    metrics.addView(NisaaDesign.space(this, 0, 10))
    metrics.addView(NisaaDesign.dividerLabel(this, getString(R.string.average_cycle_length), insight.averageCycleLength?.let { getString(R.string.days_format, it) } ?: getString(R.string.confidence_insufficient)))
    addViewWithTop(NisaaDesign.dividerLabel(this, getString(R.string.variability), insight.variabilityDays?.let { getString(R.string.days_format, it) } ?: getString(R.string.confidence_insufficient)), 12)
    addViewWithTop(NisaaDesign.dividerLabel(this, getString(R.string.predicted_period_start), dateText(insight.predictedPeriodStart)), 12)
    addViewWithTop(NisaaDesign.dividerLabel(this, getString(R.string.fertile_window_estimate), dateRange(insight.fertileWindowStart, insight.fertileWindowEnd)), 12)
    addViewWithTop(NisaaDesign.dividerLabel(this, getString(R.string.ovulation_estimate), dateText(insight.ovulationEstimate)), 12)
    addViewWithTop(NisaaDesign.body(this, getString(R.string.calculation_version, insight.calculationVersion)), 18)
    addSpaced(body, metrics, 18)
    if (insight.observedCycleCount == 0) {
        val addRecordAction = NisaaDesign.primaryButton(this, getString(R.string.cycle_add_record), R.drawable.ic_add) {
            resetEntryState()
            navigate(AppScreen.CYCLE_ENTRY)
        }
        addSpaced(body, NisaaDesign.statePanel(
            this,
            getString(R.string.state_empty),
            getString(R.string.home_no_cycle_data),
            R.drawable.ic_cycle,
            addRecordAction
        ), 0)
    } else {
        addSpaced(body, NisaaDesign.softCard(this, 18).apply {
            addView(NisaaDesign.icon(this@buildFertilityScreen, R.drawable.ic_health, R.color.nisaa_rose, 26))
            addView(NisaaDesign.space(this@buildFertilityScreen, 0, 8))
            addView(NisaaDesign.body(this@buildFertilityScreen, getString(R.string.irregular_note)))
        }, 0)
    }
}

private fun MainActivity.dateRange(start: LocalDate?, end: LocalDate?): String {
    if (start == null || end == null) return getString(R.string.confidence_insufficient)
    return getString(R.string.date_range_format, dateText(start), dateText(end))
}

internal fun MainActivity.buildCycleEntryScreen(): View = standardScreen(getString(R.string.cycle_entry_title)) { body ->
    val startField = dateSelectionCard(getString(R.string.start_date), dateValue(selectedCycleStart), R.drawable.ic_calendar) {
        showDatePicker(selectedCycleStart) {
            selectedCycleStart = it
            if (selectedCycleEnd != null && selectedCycleEnd!! < it) selectedCycleEnd = null
            render()
        }
    }
    addSpaced(body, startField, 0)
    val endField = dateSelectionCard(getString(R.string.end_date), dateValue(selectedCycleEnd), R.drawable.ic_calendar) {
        showDatePicker(selectedCycleEnd ?: selectedCycleStart) {
            selectedCycleEnd = it
            render()
        }
    }
    addSpaced(body, endField, 10)
    cardTitle(body, getString(R.string.flow_intensity))
    val flowRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
    FlowIntensity.entries.forEach { flow ->
        val chip = NisaaDesign.chip(this, flowLabel(flow), selectedFlow == flow) {
            selectedFlow = flow
            render()
        }
        flowRow.addView(chip)
        flowRow.addView(NisaaDesign.space(this, 6, 1))
    }
    val flowScroll = android.widget.HorizontalScrollView(this).apply {
        isHorizontalScrollBarEnabled = false
        addView(flowRow)
    }
    addSpaced(body, flowScroll, 0)
    cardTitle(body, getString(R.string.symptoms_title))
    symptomKeyList().forEach { (key, label) ->
        addSpaced(body, NisaaDesign.chip(this, label, selectedSymptoms.contains(key)) {
            if (!selectedSymptoms.add(key)) selectedSymptoms.remove(key)
            render()
        }, 7)
    }
    val other = NisaaDesign.multilineField(this, getString(R.string.other_observation_hint)).apply {
        setText(customObservation)
        setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) customObservation = text.toString() }
    }
    addSpaced(body, other, 12)
    val notes = NisaaDesign.multilineField(this, getString(R.string.notes_label))
    body.addView(notes)
    body.addView(NisaaDesign.space(this, 0, 22))
    body.addView(NisaaDesign.primaryButton(this, getString(R.string.save_cycle), R.drawable.ic_check) {
        customObservation = other.text.toString().trim()
        saveCycleFromEntry(notes.text.toString())
    })
    body.addView(NisaaDesign.space(this, 0, 8))
    body.addView(NisaaDesign.outlineButton(this, getString(R.string.cancel)) { navigate(AppScreen.CYCLE) })
}

private fun MainActivity.dateSelectionCard(label: CharSequence, value: CharSequence, iconRes: Int, onClick: () -> Unit): LinearLayout = NisaaDesign.card(this, 14).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
    isClickable = true
    isFocusable = true
    setOnClickListener { onClick() }
    addView(NisaaDesign.icon(this@dateSelectionCard, iconRes, R.color.nisaa_rose, 24))
    addView(NisaaDesign.space(this@dateSelectionCard, 12, 1))
    addView(NisaaDesign.body(this@dateSelectionCard, label).apply {
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    addView(NisaaDesign.text(this@dateSelectionCard, value, 14f, R.color.nisaa_ink, true, Gravity.END))
}

private fun MainActivity.symptomKeyList(): List<Pair<String, String>> = listOf(
    "cramps" to getString(R.string.symptom_cramps),
    "headache" to getString(R.string.symptom_headache),
    "breast_tenderness" to getString(R.string.symptom_breast_tenderness),
    "bloating" to getString(R.string.symptom_bloating),
    "fatigue" to getString(R.string.symptom_fatigue),
    "sleep_changes" to getString(R.string.symptom_sleep_changes),
    "appetite_changes" to getString(R.string.symptom_appetite_changes),
    "mood" to getString(R.string.symptom_mood),
    "nausea" to getString(R.string.symptom_nausea),
    "back_pain" to getString(R.string.symptom_back_pain)
)

private fun MainActivity.saveCycleFromEntry(notes: String) {
    val current = profile ?: return
    if (selectedCycleEnd != null && selectedCycleEnd!! < selectedCycleStart) {
        showToast(getString(R.string.end_date))
        return
    }
    val alreadyRecorded = repository.getCycles(current.id).any { it.startDate == selectedCycleStart }
    if (alreadyRecorded) {
        showToast(getString(R.string.cycle_record_saved))
        return
    }
    val symptoms = selectedSymptoms.toMutableList()
    if (customObservation.isNotBlank()) symptoms.add("custom:$customObservation")
    val saveResult = repository.saveCycle(
        CycleRecord(
            userId = current.id,
            startDate = selectedCycleStart,
            endDate = selectedCycleEnd,
            flow = selectedFlow,
            symptoms = symptoms,
            notes = notes
        )
    )
    if (saveResult is DomainResult.Failure) {
        showToast(saveResult.error.message)
        return
    }
    val updatedProfile = current.copy(
        cycleStartDate = repository.getCycles(current.id).maxByOrNull { it.startDate }?.startDate,
        updatedAt = java.time.Instant.now()
    )
    repository.saveProfile(updatedProfile)
    profile = updatedProfile
    val insight = biologicalEngine.calculate(repository.getCycles(current.id), LocalDate.now(), current.averageCycleLength)
    repository.saveFertility(biologicalEngine.toEstimate(current.id, insight))
    resetEntryState()
    showToast(getString(R.string.cycle_record_saved))
    navigate(AppScreen.CYCLE)
}

internal fun MainActivity.buildHealthScreen(): View = standardScreen(getString(R.string.health_title)) { body ->
    cardTitle(body, getString(R.string.health_title), getString(R.string.health_intro))
    SeedContent.health.forEach { topic ->
        addSpaced(body, topicCard(topic), 10)
    }
    addSpaced(body, NisaaDesign.softCard(this, 18).apply {
        addView(NisaaDesign.icon(this@buildHealthScreen, R.drawable.ic_lock, R.color.nisaa_rose, 26))
        addView(NisaaDesign.space(this@buildHealthScreen, 0, 8))
        addView(NisaaDesign.body(this@buildHealthScreen, getString(R.string.home_health_disclaimer)))
    }, 20)
}

internal fun MainActivity.buildFaithScreen(): View = standardScreen(getString(R.string.faith_title)) { body ->
    cardTitle(body, getString(R.string.faith_title), getString(R.string.faith_intro))
    val worship = infoCard(getString(R.string.worship_context_title), getString(R.string.worship_context_body), R.drawable.ic_faith, soft = true)
    addSpaced(body, worship, 0)
    val quran = NisaaDesign.sectionTitle(this, getString(R.string.quran_title))
    body.addView(quran)
    SeedContent.islamic.filter { it.topicKey == "QURAN" }.forEach { addSpaced(body, topicCard(it), 9) }
    body.addView(NisaaDesign.sectionTitle(this, getString(R.string.hadith_title)))
    SeedContent.islamic.filter { it.topicKey == "HADITH" }.forEach { addSpaced(body, topicCard(it), 9) }
    body.addView(NisaaDesign.sectionTitle(this, getString(R.string.fiqh_title)))
    SeedContent.islamic.filter { it.topicKey !in setOf("QURAN", "HADITH") }.forEach { addSpaced(body, topicCard(it), 9) }
    addSpaced(body, NisaaDesign.outlineButton(this, getString(R.string.quotation_title), R.drawable.ic_faith) { navigate(AppScreen.QUOTATION) }, 22)
}

private fun MainActivity.topicCard(item: ContentItem): LinearLayout = NisaaDesign.card(this, 15).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
    isClickable = true
    isFocusable = true
    setOnClickListener {
        selectedTopic = item
        navigate(AppScreen.TOPIC)
    }
    addView(NisaaDesign.icon(this@topicCard, if (item.contentType == ContentType.HEALTH) R.drawable.ic_health else R.drawable.ic_book, R.color.nisaa_rose, 24))
    addView(NisaaDesign.space(this@topicCard, 12, 1))
    addView(LinearLayout(this@topicCard).apply {
        orientation = LinearLayout.VERTICAL
        addView(NisaaDesign.text(this@topicCard, contentTopicLabel(item.topicKey), 16f, R.color.nisaa_ink, true))
        addView(NisaaDesign.space(this@topicCard, 0, 3))
        addView(NisaaDesign.text(this@topicCard, reviewLabel(item.reviewState), 11f, R.color.nisaa_ink_soft, true))
    }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
    addView(NisaaDesign.icon(this@topicCard, R.drawable.ic_chevron_right, R.color.nisaa_ink_soft, 20))
}

internal fun MainActivity.buildTopicScreen(item: ContentItem): View = standardScreen(contentTopicLabel(item.topicKey), { navigate(if (item.contentType == ContentType.HEALTH) AppScreen.HEALTH else AppScreen.FAITH) }) { body ->
    val status = NisaaDesign.card(this, 20)
    status.addView(NisaaDesign.statusPill(this, reviewLabel(item.reviewState), item.reviewState == ReviewState.VERIFIED || item.reviewState == ReviewState.PUBLISHED))
    status.addView(NisaaDesign.space(this, 0, 13))
    status.addView(NisaaDesign.body(this, getString(R.string.content_topic_review)))
    addSpaced(body, status, 0)
    val metadata = NisaaDesign.card(this, 18)
    metadata.addView(NisaaDesign.eyebrow(this, getString(R.string.sources_title)))
    metadata.addView(NisaaDesign.space(this, 0, 8))
    metadata.addView(NisaaDesign.dividerLabel(this, getString(R.string.sources_title), item.source))
    addViewWithTop(NisaaDesign.dividerLabel(this, "Reference", item.reference), 10)
    item.scholarlyMethodology?.let { addViewWithTop(NisaaDesign.dividerLabel(this, "Methodology", it), 10) }
    addViewWithTop(NisaaDesign.body(this, getString(R.string.sources_body)), 16)
    addSpaced(body, metadata, 14)
    if (item.translations.isNotEmpty()) {
        val translationCard = NisaaDesign.card(this, 18)
        translationCard.addView(NisaaDesign.eyebrow(this, getString(R.string.quotation_other_translations)))
        translationCard.addView(NisaaDesign.space(this, 0, 8))
        AppLanguage.entries.forEach { language ->
            if (item.translations.containsKey(language)) {
                translationCard.addView(NisaaDesign.chip(this, languageName(language), language == LocaleManager.savedLanguage(this)) {
                    val text = contentEngine.textFor(item, language)
                    if (!text.isNullOrBlank()) {
                        AlertDialog.Builder(this@buildTopicScreen)
                            .setTitle(contentTopicLabel(item.topicKey))
                            .setMessage(text)
                            .setPositiveButton(R.string.close, null)
                            .show()
                    }
                })
                translationCard.addView(NisaaDesign.space(this, 0, 7))
            }
        }
        addSpaced(body, translationCard, 0)
    } else {
        addSpaced(body, NisaaDesign.softCard(this, 18).apply {
            addView(NisaaDesign.body(this@buildTopicScreen, getString(R.string.content_source_pending)))
        }, 0)
    }
}

private fun MainActivity.languageName(language: AppLanguage): String = when (language) {
    AppLanguage.SWAHILI -> getString(R.string.language_swahili)
    AppLanguage.ENGLISH -> getString(R.string.language_english)
    AppLanguage.ARABIC -> getString(R.string.language_arabic)
}

internal fun MainActivity.buildQuotationScreen(): View = standardScreen(getString(R.string.quotation_title)) { body ->
    val card = infoCard(getString(R.string.content_rotation_title), getString(R.string.content_rotation_body), R.drawable.ic_faith, soft = true)
    addSpaced(body, card, 0)
    val placeholder = NisaaDesign.card(this, 20)
    placeholder.addView(NisaaDesign.eyebrow(this, getString(R.string.content_review_required)))
    placeholder.addView(NisaaDesign.space(this, 0, 10))
    placeholder.addView(NisaaDesign.text(this, getString(R.string.content_quotation_placeholder), 17f, R.color.nisaa_ink, true))
    placeholder.addView(NisaaDesign.space(this, 0, 12))
    placeholder.addView(NisaaDesign.body(this, getString(R.string.quotation_other_translations)))
    val translations = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
    AppLanguage.entries.forEach { language ->
        translations.addView(NisaaDesign.chip(this, languageName(language), language == LocaleManager.savedLanguage(this)))
        translations.addView(NisaaDesign.space(this, 6, 1))
    }
    placeholder.addView(translations)
    addSpaced(body, placeholder, 18)
    addSpaced(body, NisaaDesign.outlineButton(this, getString(R.string.content_open_topic), R.drawable.ic_chevron_right) { navigate(AppScreen.FAITH) }, 0)
}

internal fun MainActivity.buildSettingsScreen(): View = standardScreen(getString(R.string.settings_title)) { body ->
    cardTitle(body, getString(R.string.settings_title))
    addSpaced(body, settingsRow(getString(R.string.settings_language), languageName(LocaleManager.savedLanguage(this)), R.drawable.ic_faith) {
        showLanguageDialog()
    }, 0)
    addSpaced(body, settingsRow(getString(R.string.settings_privacy), getString(R.string.home_privacy_note), R.drawable.ic_lock) { navigate(AppScreen.PRIVACY) }, 10)
    addSpaced(body, settingsRow(getString(R.string.settings_notifications), getString(R.string.notifications_body), R.drawable.ic_bell) { navigate(AppScreen.NOTIFICATIONS) }, 10)
    addSpaced(body, settingsRow(getString(R.string.settings_content), getString(R.string.content_version_title), R.drawable.ic_education) { navigate(AppScreen.CONTENT_VERSION) }, 10)
    addSpaced(body, settingsRow(getString(R.string.settings_about), getString(R.string.version_format, BuildConfig.VERSION_NAME), R.drawable.ic_nisaa_mark) { navigate(AppScreen.ABOUT) }, 10)
    val roleLabel = if (profile?.role == UserRole.HUSBAND) getString(R.string.settings_role_husband) else getString(R.string.settings_role_woman)
    addSpaced(body, settingsRow(getString(R.string.settings_role), roleLabel, R.drawable.ic_relationship) { switchExperienceRole() }, 10)
    val status = NisaaDesign.softCard(this, 18)
    status.addView(NisaaDesign.icon(this, R.drawable.ic_lock, R.color.nisaa_rose, 25))
    status.addView(NisaaDesign.space(this, 0, 8))
    status.addView(NisaaDesign.body(this, getString(R.string.settings_privacy_body)))
    addSpaced(body, status, 18)
}

private fun MainActivity.settingsRow(title: CharSequence, value: CharSequence, iconRes: Int, onClick: () -> Unit): LinearLayout = NisaaDesign.card(this, 14).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
    isClickable = true
    isFocusable = true
    setOnClickListener { onClick() }
    addView(NisaaDesign.icon(this@settingsRow, iconRes, R.color.nisaa_rose, 23))
    addView(NisaaDesign.space(this@settingsRow, 12, 1))
    addView(LinearLayout(this@settingsRow).apply {
        orientation = LinearLayout.VERTICAL
        addView(NisaaDesign.text(this@settingsRow, title, 15f, R.color.nisaa_ink, true))
        addView(NisaaDesign.space(this@settingsRow, 0, 3))
        addView(NisaaDesign.text(this@settingsRow, value, 12f, R.color.nisaa_ink_soft))
    }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
    addView(NisaaDesign.icon(this@settingsRow, R.drawable.ic_chevron_right, R.color.nisaa_ink_soft, 20))
}

private fun MainActivity.showLanguageDialog() {
    val languages = AppLanguage.entries
    val labels = languages.map { languageName(it) }.toTypedArray()
    val selected = languages.indexOf(LocaleManager.savedLanguage(this))
    AlertDialog.Builder(this)
        .setTitle(R.string.settings_language)
        .setSingleChoiceItems(labels, selected) { dialog, which ->
            LocaleManager.setLanguage(this, languages[which])
            profile?.let {
                val updated = it.copy(language = languages[which], updatedAt = java.time.Instant.now())
                repository.saveProfile(updated)
                profile = updated
                preferences = preferences.copy(preferredContentLanguage = languages[which])
                repository.savePreferences(preferences)
            }
            dialog.dismiss()
            recreate()
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}

private fun MainActivity.switchExperienceRole() {
    val current = profile ?: return
    val next = if (current.role == UserRole.WOMAN) UserRole.HUSBAND else UserRole.WOMAN
    val updated = current.copy(role = next, updatedAt = java.time.Instant.now())
    repository.saveProfile(updated)
    profile = updated
    showToast(if (next == UserRole.HUSBAND) getString(R.string.switch_to_husband) else getString(R.string.switch_to_woman))
    currentScreen = AppScreen.HOME
    render()
}

internal fun MainActivity.buildPrivacyScreen(): View = standardScreen(getString(R.string.settings_privacy_title)) { body ->
    val card = infoCard(getString(R.string.settings_privacy_title), getString(R.string.settings_privacy_body), R.drawable.ic_lock, soft = true)
    addSpaced(body, card, 0)
    cardTitle(body, getString(R.string.sharing_permissions))
    val relationships = profile?.id?.let { repository.getRelationships(it) }.orEmpty()
    if (relationships.isEmpty()) {
        addSpaced(body, NisaaDesign.statePanel(this, getString(R.string.no_relationship), getString(R.string.pairing_not_configured), R.drawable.ic_relationship), 0)
    } else {
        relationships.forEach { relationship -> addSpaced(body, relationshipPrivacyCard(relationship), 10) }
    }
    addSpaced(body, NisaaDesign.outlineButton(this, getString(R.string.marriage_title), R.drawable.ic_relationship) { navigate(AppScreen.MARRIAGE) }, 18)
}

private fun MainActivity.relationshipPrivacyCard(relationship: Relationship): LinearLayout = NisaaDesign.card(this, 16).apply {
    addView(NisaaDesign.text(this@relationshipPrivacyCard, getString(R.string.relationship_status), 13f, R.color.nisaa_ink_soft, true))
    addView(NisaaDesign.space(this@relationshipPrivacyCard, 0, 5))
    addView(NisaaDesign.statusPill(this@relationshipPrivacyCard, relationshipStatusText(relationship.status), relationship.status == com.zamcan.nisaacare.domain.model.RelationshipStatus.ACTIVE))
    PermissionKey.entries.forEach { permission ->
        val row = LinearLayout(this@relationshipPrivacyCard).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(NisaaDesign.body(this@relationshipPrivacyCard, permissionLabel(permission)).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            val toggle = Switch(this@relationshipPrivacyCard).apply {
                isChecked = relationship.permissions.permissions[permission] == true
                contentDescription = permissionLabel(permission)
                setOnCheckedChangeListener { _, checked ->
                    val result = repository.updatePermission(relationship, permission, checked)
                    if (result is DomainResult.Failure) showToast(result.error.message)
                    render()
                }
            }
            addView(toggle)
        }
        addView(row)
    }
}

internal fun MainActivity.permissionLabel(permission: PermissionKey): String = when (permission) {
    PermissionKey.CYCLE_STATUS -> getString(R.string.permission_cycle_status)
    PermissionKey.FERTILITY_ESTIMATE -> getString(R.string.permission_fertility)
    PermissionKey.WELLBEING -> getString(R.string.permission_wellbeing)
    PermissionKey.SUPPORT_REQUESTS -> getString(R.string.permission_support)
    PermissionKey.EDUCATION -> getString(R.string.permission_education)
}

internal fun MainActivity.buildNotificationsScreen(): View = standardScreen(getString(R.string.settings_notifications)) { body ->
    cardTitle(body, getString(R.string.notifications_title), getString(R.string.notifications_body))
    val card = NisaaDesign.card(this, 18)
    addNotificationToggle(card, getString(R.string.notifications_enable)) { value ->
        preferences = preferences.copy(notificationsEnabled = value)
        repository.savePreferences(preferences)
    }
    addNotificationToggle(card, getString(R.string.notification_details)) { value ->
        preferences = preferences.copy(notificationDetailsEnabled = value)
        repository.savePreferences(preferences)
    }
    addNotificationToggle(card, getString(R.string.cycle_reminders)) { value ->
        preferences = preferences.copy(cycleRemindersEnabled = value)
        repository.savePreferences(preferences)
    }
    addNotificationToggle(card, getString(R.string.content_reminders)) { value ->
        preferences = preferences.copy(contentRemindersEnabled = value)
        repository.savePreferences(preferences)
    }
    addNotificationToggle(card, getString(R.string.relationship_reminders)) { value ->
        preferences = preferences.copy(relationshipRemindersEnabled = value)
        repository.savePreferences(preferences)
    }
    addSpaced(body, card, 0)
    addSpaced(body, NisaaDesign.secondaryButton(this, getString(R.string.test_private_notification), R.drawable.ic_bell) {
        try {
            notificationScheduler.showPrivateUpdate()
        } catch (_: SecurityException) {
            showToast(getString(R.string.notification_permission_needed))
        }
    }, 18)
}

private fun MainActivity.addNotificationToggle(parent: LinearLayout, label: String, onChanged: (Boolean) -> Unit) {
    val row = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(NisaaDesign.body(this@addNotificationToggle, label).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        val enabled = when (label) {
            getString(R.string.notifications_enable) -> preferences.notificationsEnabled
            getString(R.string.notification_details) -> preferences.notificationDetailsEnabled
            getString(R.string.cycle_reminders) -> preferences.cycleRemindersEnabled
            getString(R.string.content_reminders) -> preferences.contentRemindersEnabled
            else -> preferences.relationshipRemindersEnabled
        }
        addView(Switch(this@addNotificationToggle).apply {
            isChecked = enabled
            contentDescription = label
            setOnCheckedChangeListener { _, checked -> onChanged(checked) }
        })
    }
    parent.addView(row)
    parent.addView(NisaaDesign.space(this, 0, 10))
}

internal fun MainActivity.buildContentVersionScreen(): View = standardScreen(getString(R.string.content_version_title)) { body ->
    val version = repository.getContentVersion()
    val card = infoCard(getString(R.string.content_version_title), getString(R.string.content_version_body, version.version), R.drawable.ic_education, soft = true)
    addSpaced(body, card, 0)
    addSpaced(body, NisaaDesign.statePanel(this, getString(R.string.content_update_not_configured), getString(R.string.content_rotation_body), R.drawable.ic_lock), 0)
}

internal fun MainActivity.buildSourcesScreen(): View = standardScreen(getString(R.string.sources_title)) { body ->
    val card = infoCard(getString(R.string.sources_title), getString(R.string.sources_body), R.drawable.ic_education, soft = true)
    addSpaced(body, card, 0)
    cardTitle(body, getString(R.string.content_review_required))
    addSpaced(body, NisaaDesign.body(this, getString(R.string.content_topic_review)), 0)
}

internal fun MainActivity.buildAboutScreen(): View = standardScreen(getString(R.string.settings_about)) { body ->
    val mark = NisaaDesign.icon(this, R.drawable.ic_nisaa_mark, R.color.nisaa_rose, 82)
    mark.layoutParams = LinearLayout.LayoutParams(NisaaDesign.dp(this, 82), NisaaDesign.dp(this, 82)).apply { gravity = Gravity.CENTER_HORIZONTAL }
    body.addView(mark)
    body.addView(NisaaDesign.space(this, 0, 16))
    body.addView(NisaaDesign.serif(this, getString(R.string.app_name), 30f))
    body.addView(NisaaDesign.space(this, 0, 7))
    body.addView(NisaaDesign.body(this, getString(R.string.app_tagline)).apply { gravity = Gravity.CENTER })
    body.addView(NisaaDesign.space(this, 0, 18))
    val card = infoCard(getString(R.string.version_format, BuildConfig.VERSION_NAME), getString(R.string.about_body), R.drawable.ic_lock, soft = true)
    addSpaced(body, card, 0)
}

internal fun MainActivity.buildHusbandPrivateNotice(): View = standardScreen(getString(R.string.husband_title)) { body ->
    val marriageAction = NisaaDesign.primaryButton(this, getString(R.string.marriage_title), R.drawable.ic_relationship) {
        navigate(AppScreen.MARRIAGE)
    }
    addSpaced(body, NisaaDesign.statePanel(
        this,
        getString(R.string.husband_permission_required),
        getString(R.string.home_health_disclaimer),
        R.drawable.ic_lock,
        marriageAction
    ), 0)
}

internal fun MainActivity.buildHusbandHome(): View = standardScreen(getString(R.string.husband_title)) { body ->
    addSpaced(body, NisaaDesign.softCard(this, 20).apply {
        addView(NisaaDesign.icon(this@buildHusbandHome, R.drawable.ic_relationship, R.color.nisaa_rose, 30))
        addView(NisaaDesign.space(this@buildHusbandHome, 0, 10))
        addView(NisaaDesign.text(this@buildHusbandHome, getString(R.string.husband_welcome), 18f, R.color.nisaa_ink, true))
        addView(NisaaDesign.space(this@buildHusbandHome, 0, 7))
        addView(NisaaDesign.body(this@buildHusbandHome, getString(R.string.husband_permission_required)))
    }, 0)
    val relationships = profile?.id?.let { repository.getRelationships(it) }.orEmpty()
    if (relationships.isEmpty()) {
        val connectAction = NisaaDesign.primaryButton(this, getString(R.string.create_invitation), R.drawable.ic_add) {
            navigate(AppScreen.MARRIAGE)
        }
        addSpaced(body, NisaaDesign.statePanel(
            this,
            getString(R.string.no_relationship),
            getString(R.string.connection_body),
            R.drawable.ic_relationship,
            connectAction
        ), 16)
    } else {
        relationships.forEach { relationship ->
            val card = NisaaDesign.card(this, 18)
            card.addView(NisaaDesign.text(this, getString(R.string.relationship_status), 13f, R.color.nisaa_ink_soft, true))
            card.addView(NisaaDesign.space(this, 0, 6))
            card.addView(NisaaDesign.statusPill(this, relationshipStatusText(relationship.status), relationship.status == com.zamcan.nisaacare.domain.model.RelationshipStatus.ACTIVE))
            val wifeId = relationship.wifeUserId
            if (relationship.status == com.zamcan.nisaacare.domain.model.RelationshipStatus.ACTIVE && wifeId != null) {
                if (isPermissionAllowed(relationship, PermissionKey.CYCLE_STATUS)) {
                    val cycles = repository.getCycles(wifeId)
                    card.addView(NisaaDesign.space(this, 0, 12))
                    card.addView(NisaaDesign.body(this, getString(R.string.permission_cycle_status)))
                    card.addView(NisaaDesign.space(this, 0, 5))
                    card.addView(NisaaDesign.text(this, cycles.maxByOrNull { it.startDate }?.let { dateText(it.startDate) } ?: getString(R.string.state_empty), 16f, R.color.nisaa_ink, true))
                }
                if (isPermissionAllowed(relationship, PermissionKey.FERTILITY_ESTIMATE)) {
                    val insight = biologicalEngine.calculate(repository.getCycles(wifeId), LocalDate.now())
                    card.addView(NisaaDesign.space(this, 0, 12))
                    card.addView(NisaaDesign.body(this, getString(R.string.permission_fertility)))
                    card.addView(NisaaDesign.space(this, 0, 5))
                    card.addView(NisaaDesign.text(this, dateRange(insight.fertileWindowStart, insight.fertileWindowEnd), 16f, R.color.nisaa_ink, true))
                }
            }
            addSpaced(body, card, 12)
        }
    }
    cardTitle(body, getString(R.string.husband_education))
    SeedContent.husbandEducation.forEach { addSpaced(body, topicCard(it), 9) }
    addSpaced(body, NisaaDesign.outlineButton(this, getString(R.string.marriage_title), R.drawable.ic_relationship) { navigate(AppScreen.MARRIAGE) }, 18)
}
