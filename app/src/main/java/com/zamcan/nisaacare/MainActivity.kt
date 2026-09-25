package com.zamcan.nisaacare

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.zamcan.nisaacare.core.localization.LocaleManager
import com.zamcan.nisaacare.core.notifications.NotificationScheduler
import com.zamcan.nisaacare.data.content.SeedContent
import com.zamcan.nisaacare.data.local.LocalCareRepository
import com.zamcan.nisaacare.domain.content.ContentEngine
import com.zamcan.nisaacare.domain.cycle.BiologicalEngine
import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.AppPreferences
import com.zamcan.nisaacare.domain.model.Confidence
import com.zamcan.nisaacare.domain.model.ContentItem
import com.zamcan.nisaacare.domain.model.CycleInsight
import com.zamcan.nisaacare.domain.model.CycleRecord
import com.zamcan.nisaacare.domain.model.DomainResult
import com.zamcan.nisaacare.domain.model.FlowIntensity
import com.zamcan.nisaacare.domain.model.MaritalStatus
import com.zamcan.nisaacare.domain.model.PairingInvitation
import com.zamcan.nisaacare.domain.model.PermissionKey
import com.zamcan.nisaacare.domain.model.Relationship
import com.zamcan.nisaacare.domain.model.RelationshipStatus
import com.zamcan.nisaacare.domain.model.UserProfile
import com.zamcan.nisaacare.domain.model.UserRole
import com.zamcan.nisaacare.ui.NisaaDesign
import com.zamcan.nisaacare.ui.PatternView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

internal enum class AppScreen {
    HOME,
    CYCLE,
    FERTILITY,
    CYCLE_ENTRY,
    FAITH,
    HEALTH,
    TOPIC,
    QUOTATION,
    MARRIAGE,
    SETTINGS,
    PRIVACY,
    NOTIFICATIONS,
    CONTENT_VERSION,
    SOURCES,
    ABOUT
}

class MainActivity : Activity() {
    internal lateinit var container: AppContainer
    internal lateinit var repository: LocalCareRepository
    internal lateinit var biologicalEngine: BiologicalEngine
    internal lateinit var contentEngine: ContentEngine
    internal lateinit var notificationScheduler: NotificationScheduler

    internal var profile: UserProfile? = null
    internal var preferences: AppPreferences = AppPreferences("local")
    internal var currentScreen: AppScreen = AppScreen.HOME
    internal var selectedTopic: ContentItem? = null
    internal var selectedRelationshipId: String? = null
    internal var lastInvitation: PairingInvitation? = null

    internal var draftProfile: UserProfile? = null
    internal var draftStep: Int = 0
    internal var draftName: String = ""
    internal var draftRole: UserRole = UserRole.WOMAN
    internal var draftAverage: Int? = null
    internal var draftMaritalStatus: MaritalStatus = MaritalStatus.SINGLE
    internal var draftCycleStart: LocalDate? = null
    internal var draftNotifications: Boolean = false
    internal var draftCreateInvitation: Boolean = false

    internal var selectedCycleStart: LocalDate = LocalDate.now()
    internal var selectedCycleEnd: LocalDate? = null
    internal var selectedFlow: FlowIntensity = FlowIntensity.UNKNOWN
    internal val selectedSymptoms: MutableSet<String> = linkedSetOf()
    internal var customObservation: String = ""
    internal var calendarMonth: YearMonth = YearMonth.now()

    private val dateFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(resources.configuration.locales[0])

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = (application as NisaaApplication).container
        repository = container.careRepository
        biologicalEngine = container.biologicalEngine
        contentEngine = container.contentEngine
        notificationScheduler = container.notificationScheduler
        profile = repository.getProfile()
        if (profile != null) preferences = repository.getPreferences(profile!!.id)
        configureSystemBars()
        handlePairingIntent(intent)
        render()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePairingIntent(intent)
        render()
    }

    override fun onResume() {
        super.onResume()
        profile = repository.getProfile()
        if (profile != null) preferences = repository.getPreferences(profile!!.id)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (profile?.onboardingComplete != true) {
            if (draftStep > 0) {
                draftStep--
                render()
            } else {
                super.onBackPressed()
            }
            return
        }
        if (currentScreen != AppScreen.HOME) {
            currentScreen = AppScreen.HOME
            render()
        } else {
            super.onBackPressed()
        }
    }

    internal fun render() {
        if (profile?.onboardingComplete != true) {
            if (draftProfile == null) {
                draftProfile = profile ?: UserProfile(
                    language = LocaleManager.savedLanguage(this),
                    onboardingStep = draftStep
                )
                draftStep = draftProfile?.onboardingStep ?: 0
                draftName = draftProfile?.displayName.orEmpty()
                draftRole = draftProfile?.role ?: UserRole.WOMAN
                draftAverage = draftProfile?.averageCycleLength
                draftMaritalStatus = draftProfile?.maritalStatus ?: MaritalStatus.SINGLE
                draftCycleStart = draftProfile?.cycleStartDate
            }
            renderOnboarding()
        } else {
            renderShell()
        }
    }

    private fun renderOnboarding() {
        val root = FrameLayout(this).apply {
            setBackgroundColor(NisaaDesign.color(this@MainActivity, R.color.nisaa_cream))
        }
        val scroll = NisaaDesign.screenScroll(this)
        val body = scroll.getChildAt(0) as LinearLayout
        body.setPadding(NisaaDesign.dp(this, 24), NisaaDesign.dp(this, 26), NisaaDesign.dp(this, 24), NisaaDesign.dp(this, 30))
        body.addView(buildOnboardingIdentity())
        body.addView(NisaaDesign.space(this, 0, 22))
        body.addView(NisaaDesign.eyebrow(this, getString(R.string.onboarding_step, draftStep + 1, ONBOARDING_STEPS)))
        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = ONBOARDING_STEPS
            progress = draftStep + 1
            progressTintList = android.content.res.ColorStateList.valueOf(
                NisaaDesign.color(this@MainActivity, R.color.nisaa_rose)
            )
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(
                NisaaDesign.color(this@MainActivity, R.color.nisaa_lilac)
            )
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, NisaaDesign.dp(this@MainActivity, 5))
        }
        body.addView(progress)
        body.addView(NisaaDesign.space(this, 0, 24))
        when (draftStep) {
            0 -> onboardingWelcome(body)
            1 -> onboardingLanguage(body)
            2 -> onboardingPrivacy(body)
            3 -> onboardingProfile(body)
            4 -> onboardingBiological(body)
            5 -> onboardingMarital(body)
            6 -> onboardingRelationship(body)
            7 -> onboardingCycle(body)
            8 -> onboardingNotifications(body)
            else -> onboardingComplete(body)
        }
        root.addView(scroll, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        setContentView(root)
    }

    private fun buildOnboardingIdentity(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val mark = NisaaDesign.icon(this@MainActivity, R.drawable.ic_nisaa_mark, R.color.nisaa_rose, 40)
        addView(mark)
        addView(NisaaDesign.space(this@MainActivity, 12, 1))
        addView(NisaaDesign.serif(this@MainActivity, getString(R.string.app_name), 22f))
    }

    private fun onboardingWelcome(body: LinearLayout) {
        body.addView(NisaaDesign.serif(this, getString(R.string.onboarding_welcome_title), 34f))
        body.addView(NisaaDesign.space(this, 0, 14))
        body.addView(NisaaDesign.body(this, getString(R.string.onboarding_welcome_body)))
        body.addView(NisaaDesign.space(this, 0, 28))
        val card = NisaaDesign.softCard(this, 22)
        card.addView(NisaaDesign.eyebrow(this, getString(R.string.app_tagline)))
        card.addView(NisaaDesign.space(this, 0, 10))
        card.addView(NisaaDesign.serif(this, getString(R.string.app_name), 26f))
        card.addView(NisaaDesign.space(this, 0, 8))
        card.addView(NisaaDesign.body(this, getString(R.string.app_description)))
        body.addView(card)
        body.addView(NisaaDesign.space(this, 0, 24))
        body.addView(NisaaDesign.primaryButton(this, getString(R.string.onboarding_continue)) {
            draftStep = 1
            render()
        })
    }

    private fun onboardingLanguage(body: LinearLayout) {
        body.addView(NisaaDesign.sectionTitle(this, getString(R.string.language_title)))
        body.addView(NisaaDesign.body(this, getString(R.string.language_body)))
        body.addView(NisaaDesign.space(this, 0, 20))
        val selected = draftProfile?.language ?: LocaleManager.savedLanguage(this)
        languageChoice(body, getString(R.string.language_swahili), selected == AppLanguage.SWAHILI) {
            chooseOnboardingLanguage(AppLanguage.SWAHILI)
        }
        languageChoice(body, getString(R.string.language_english), selected == AppLanguage.ENGLISH) {
            chooseOnboardingLanguage(AppLanguage.ENGLISH)
        }
        languageChoice(body, getString(R.string.language_arabic), selected == AppLanguage.ARABIC) {
            chooseOnboardingLanguage(AppLanguage.ARABIC)
        }
    }

    private fun languageChoice(body: LinearLayout, label: String, selected: Boolean, onClick: () -> Unit) {
        val row = NisaaDesign.card(this, 4).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(NisaaDesign.dp(this@MainActivity, 16), NisaaDesign.dp(this@MainActivity, 12), NisaaDesign.dp(this@MainActivity, 12), NisaaDesign.dp(this@MainActivity, 12))
            setOnClickListener { onClick() }
            isClickable = true
            isFocusable = true
        }
        row.addView(NisaaDesign.text(this, label, 16f, if (selected) R.color.nisaa_rose_dark else R.color.nisaa_ink, selected).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        if (selected) row.addView(NisaaDesign.icon(this, R.drawable.ic_check, R.color.nisaa_rose, 24))
        body.addView(row)
        body.addView(NisaaDesign.space(this, 0, 10))
    }

    private fun chooseOnboardingLanguage(language: AppLanguage) {
        val base = draftProfile ?: UserProfile(language = language)
        draftProfile = base.copy(language = language, onboardingStep = 2)
        saveDraftProfile(2)
        LocaleManager.setLanguage(this, language)
        recreate()
    }

    private fun onboardingPrivacy(body: LinearLayout) {
        body.addView(NisaaDesign.sectionTitle(this, getString(R.string.privacy_title)))
        body.addView(NisaaDesign.body(this, getString(R.string.privacy_body)))
        body.addView(NisaaDesign.space(this, 0, 22))
        val card = NisaaDesign.card(this, 20)
        card.addView(NisaaDesign.icon(this, R.drawable.ic_lock, R.color.nisaa_rose, 30))
        card.addView(NisaaDesign.space(this, 0, 12))
        card.addView(NisaaDesign.text(this, getString(R.string.home_privacy_note), 15f, R.color.nisaa_ink, true))
        card.addView(NisaaDesign.space(this, 0, 6))
        card.addView(NisaaDesign.body(this, getString(R.string.home_health_disclaimer)))
        body.addView(card)
        body.addView(NisaaDesign.space(this, 0, 24))
        body.addView(NisaaDesign.primaryButton(this, getString(R.string.onboarding_continue)) {
            draftStep = 3
            render()
        })
    }

    private fun onboardingProfile(body: LinearLayout) {
        body.addView(NisaaDesign.sectionTitle(this, getString(R.string.profile_title)))
        body.addView(NisaaDesign.body(this, getString(R.string.profile_body)))
        body.addView(NisaaDesign.space(this, 0, 16))
        val name = NisaaDesign.field(this, getString(R.string.profile_name_hint)).apply {
            setText(draftName)
            setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) draftName = text.toString() }
        }
        body.addView(name)
        body.addView(NisaaDesign.space(this, 0, 22))
        body.addView(NisaaDesign.eyebrow(this, getString(R.string.profile_role_title)))
        body.addView(NisaaDesign.body(this, getString(R.string.profile_role_body)))
        body.addView(NisaaDesign.space(this, 0, 12))
        body.addView(NisaaDesign.chip(this, getString(R.string.role_woman), draftRole == UserRole.WOMAN) {
            draftRole = UserRole.WOMAN
            render()
        })
        body.addView(NisaaDesign.space(this, 0, 8))
        body.addView(NisaaDesign.chip(this, getString(R.string.role_husband), draftRole == UserRole.HUSBAND) {
            draftRole = UserRole.HUSBAND
            render()
        })
        body.addView(NisaaDesign.space(this, 0, 24))
        body.addView(NisaaDesign.primaryButton(this, getString(R.string.onboarding_continue)) {
            draftName = name.text.toString()
            draftStep = 4
            render()
        })
    }

    private fun onboardingBiological(body: LinearLayout) {
        body.addView(NisaaDesign.sectionTitle(this, getString(R.string.biological_title)))
        body.addView(NisaaDesign.body(this, getString(R.string.biological_body)))
        body.addView(NisaaDesign.space(this, 0, 18))
        val average = NisaaDesign.field(this, getString(R.string.biological_average_hint), android.text.InputType.TYPE_CLASS_NUMBER).apply {
            if (draftAverage != null) setText(draftAverage.toString())
            setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) draftAverage = text.toString().toIntOrNull() }
        }
        body.addView(average)
        body.addView(NisaaDesign.space(this, 0, 24))
        body.addView(NisaaDesign.primaryButton(this, getString(R.string.onboarding_continue)) {
            draftAverage = average.text.toString().toIntOrNull()?.takeIf { it in 21..45 }
            draftStep = 5
            render()
        })
        body.addView(NisaaDesign.space(this, 0, 8))
        body.addView(NisaaDesign.outlineButton(this, getString(R.string.onboarding_skip)) {
            draftAverage = null
            draftStep = 5
            render()
        })
    }

    private fun onboardingMarital(body: LinearLayout) {
        body.addView(NisaaDesign.sectionTitle(this, getString(R.string.marital_title)))
        body.addView(NisaaDesign.body(this, getString(R.string.marital_body)))
        body.addView(NisaaDesign.space(this, 0, 16))
        maritalChoice(body, R.string.marital_single, MaritalStatus.SINGLE)
        maritalChoice(body, R.string.marital_married, MaritalStatus.MARRIED)
        maritalChoice(body, R.string.marital_widowed, MaritalStatus.WIDOWED)
        maritalChoice(body, R.string.marital_divorced, MaritalStatus.DIVORCED)
        maritalChoice(body, R.string.marital_other, MaritalStatus.OTHER)
        body.addView(NisaaDesign.space(this, 0, 22))
        body.addView(NisaaDesign.primaryButton(this, getString(R.string.onboarding_continue)) {
            draftStep = 6
            render()
        })
    }

    private fun maritalChoice(body: LinearLayout, labelRes: Int, value: MaritalStatus) {
        val selected = draftMaritalStatus == value
        val row = NisaaDesign.card(this, 4).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(NisaaDesign.dp(this@MainActivity, 16), NisaaDesign.dp(this@MainActivity, 12), NisaaDesign.dp(this@MainActivity, 12), NisaaDesign.dp(this@MainActivity, 12))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                draftMaritalStatus = value
                render()
            }
        }
        row.addView(NisaaDesign.text(this, getString(labelRes), 16f, if (selected) R.color.nisaa_rose_dark else R.color.nisaa_ink, selected).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        if (selected) row.addView(NisaaDesign.icon(this, R.drawable.ic_check, R.color.nisaa_rose, 24))
        body.addView(row)
        body.addView(NisaaDesign.space(this, 0, 8))
    }

    private fun onboardingRelationship(body: LinearLayout) {
        body.addView(NisaaDesign.sectionTitle(this, getString(R.string.relationship_title)))
        body.addView(NisaaDesign.body(this, getString(R.string.relationship_body)))
        body.addView(NisaaDesign.space(this, 0, 22))
        val card = NisaaDesign.softCard(this, 20)
        card.addView(NisaaDesign.icon(this, R.drawable.ic_relationship, R.color.nisaa_rose, 30))
        card.addView(NisaaDesign.space(this, 0, 10))
        card.addView(NisaaDesign.text(this, getString(R.string.home_privacy_note), 15f, R.color.nisaa_ink, true))
        body.addView(card)
        body.addView(NisaaDesign.space(this, 0, 22))
        body.addView(NisaaDesign.primaryButton(this, getString(R.string.relationship_not_now)) {
            draftStep = 7
            render()
        })
        body.addView(NisaaDesign.space(this, 0, 8))
        body.addView(NisaaDesign.outlineButton(this, getString(R.string.create_invitation)) {
            draftCreateInvitation = true
            draftStep = 7
            render()
        })
    }

    private fun onboardingCycle(body: LinearLayout) {
        body.addView(NisaaDesign.sectionTitle(this, getString(R.string.cycle_setup_title)))
        body.addView(NisaaDesign.body(this, getString(R.string.cycle_setup_body)))
        body.addView(NisaaDesign.space(this, 0, 18))
        val date = NisaaDesign.card(this, 16).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setOnClickListener {
                showDatePicker(draftCycleStart ?: LocalDate.now()) {
                    draftCycleStart = it
                    render()
                }
            }
        }
        date.addView(NisaaDesign.icon(this, R.drawable.ic_calendar, R.color.nisaa_rose, 24))
        date.addView(NisaaDesign.space(this, 12, 1))
        date.addView(NisaaDesign.body(this, getString(R.string.cycle_setup_date_hint)).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        date.addView(NisaaDesign.text(this, dateValue(draftCycleStart), 15f, R.color.nisaa_ink, true, Gravity.END))
        body.addView(date)
        body.addView(NisaaDesign.space(this, 0, 22))
        body.addView(NisaaDesign.primaryButton(this, getString(R.string.onboarding_continue)) {
            if (draftCycleStart != null) saveDraftCycle()
            draftStep = 8
            render()
        })
        body.addView(NisaaDesign.space(this, 0, 8))
        body.addView(NisaaDesign.outlineButton(this, getString(R.string.cycle_setup_skip)) {
            draftCycleStart = null
            draftStep = 8
            render()
        })
    }

    private fun onboardingNotifications(body: LinearLayout) {
        body.addView(NisaaDesign.sectionTitle(this, getString(R.string.notifications_title)))
        body.addView(NisaaDesign.body(this, getString(R.string.notifications_body)))
        body.addView(NisaaDesign.space(this, 0, 18))
        val card = NisaaDesign.card(this, 16)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        row.addView(NisaaDesign.body(this, getString(R.string.notifications_enable)).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        val toggle = Switch(this).apply {
            isChecked = draftNotifications
            contentDescription = getString(R.string.notifications_enable)
            setOnCheckedChangeListener { _, checked -> draftNotifications = checked }
        }
        row.addView(toggle)
        card.addView(row)
        body.addView(card)
        body.addView(NisaaDesign.space(this, 0, 24))
        body.addView(NisaaDesign.primaryButton(this, getString(R.string.onboarding_continue)) {
            if (draftNotifications && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST)
            }
            saveDraftProfile(9)
            draftStep = 9
            render()
        })
    }

    private fun onboardingComplete(body: LinearLayout) {
        body.addView(NisaaDesign.serif(this, getString(R.string.onboarding_complete_title), 32f))
        body.addView(NisaaDesign.space(this, 0, 12))
        body.addView(NisaaDesign.body(this, getString(R.string.onboarding_complete_body)))
        body.addView(NisaaDesign.space(this, 0, 24))
        val card = NisaaDesign.softCard(this, 22)
        card.addView(NisaaDesign.icon(this, R.drawable.ic_nisaa_mark, R.color.nisaa_rose, 56))
        card.addView(NisaaDesign.space(this, 0, 10))
        card.addView(NisaaDesign.text(this, getString(R.string.app_tagline), 14f, R.color.nisaa_ink_soft, true, Gravity.CENTER))
        body.addView(card)
        body.addView(NisaaDesign.space(this, 0, 24))
        body.addView(NisaaDesign.primaryButton(this, getString(R.string.onboarding_finish)) {
            val current = draftProfile ?: UserProfile()
            val finished = current.copy(
                displayName = draftName.trim(),
                role = draftRole,
                maritalStatus = draftMaritalStatus,
                averageCycleLength = draftAverage,
                cycleStartDate = draftCycleStart,
                onboardingStep = ONBOARDING_STEPS,
                onboardingComplete = true,
                updatedAt = java.time.Instant.now()
            )
            repository.saveProfile(finished)
            repository.savePreferences(AppPreferences(finished.id, notificationsEnabled = draftNotifications))
            profile = finished
            preferences = repository.getPreferences(finished.id)
            if (draftCreateInvitation) {
                val invitation = container.relationshipPolicy.createInvitation(finished.id, finished.role)
                repository.saveInvitation(invitation)
                lastInvitation = invitation
            }
            currentScreen = AppScreen.HOME
            render()
        })
    }

    private fun saveDraftProfile(step: Int) {
        val current = draftProfile ?: UserProfile()
        val updated = current.copy(
            displayName = draftName.trim().ifBlank { current.displayName },
            language = current.language,
            role = draftRole,
            maritalStatus = draftMaritalStatus,
            averageCycleLength = draftAverage,
            cycleStartDate = draftCycleStart,
            onboardingStep = step,
            onboardingComplete = false,
            updatedAt = java.time.Instant.now()
        )
        draftProfile = updated
        repository.saveProfile(updated)
        profile = updated
    }

    private fun saveDraftCycle() {
        val current = profile ?: draftProfile ?: return
        val date = draftCycleStart ?: return
        repository.saveCycle(CycleRecord(userId = current.id, startDate = date, source = "ONBOARDING"))
        val insight = biologicalEngine.calculate(repository.getCycles(current.id), LocalDate.now(), current.averageCycleLength)
        repository.saveFertility(biologicalEngine.toEstimate(current.id, insight))
    }

    private fun renderShell() {
        val root = FrameLayout(this).apply { setBackgroundColor(NisaaDesign.color(this@MainActivity, R.color.nisaa_cream)) }
        val shell = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(NisaaDesign.color(this@MainActivity, R.color.nisaa_cream))
        }
        val content = FrameLayout(this)
        shell.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        shell.addView(buildBottomNavigation())
        root.addView(shell, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        setContentView(root)
        renderCurrentScreen(content)
    }

    private fun renderCurrentScreen(content: FrameLayout) {
        content.removeAllViews()
        val view: View = when (currentScreen) {
            AppScreen.HOME -> if (profile?.role == UserRole.HUSBAND) buildHusbandHome() else buildWomanHome()
            AppScreen.CYCLE -> if (profile?.role == UserRole.HUSBAND) buildHusbandPrivateNotice() else buildCycleScreen()
            AppScreen.FERTILITY -> if (profile?.role == UserRole.HUSBAND) buildHusbandPrivateNotice() else buildFertilityScreen()
            AppScreen.CYCLE_ENTRY -> if (profile?.role == UserRole.HUSBAND) buildHusbandPrivateNotice() else buildCycleEntryScreen()
            AppScreen.FAITH -> buildFaithScreen()
            AppScreen.HEALTH -> buildHealthScreen()
            AppScreen.TOPIC -> selectedTopic?.let { buildTopicScreen(it) } ?: buildFaithScreen()
            AppScreen.QUOTATION -> buildQuotationScreen()
            AppScreen.MARRIAGE -> buildMarriageScreen()
            AppScreen.SETTINGS -> buildSettingsScreen()
            AppScreen.PRIVACY -> buildPrivacyScreen()
            AppScreen.NOTIFICATIONS -> buildNotificationsScreen()
            AppScreen.CONTENT_VERSION -> buildContentVersionScreen()
            AppScreen.SOURCES -> buildSourcesScreen()
            AppScreen.ABOUT -> buildAboutScreen()
        }
        content.addView(view, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun buildBottomNavigation(): View {
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(NisaaDesign.dp(this@MainActivity, 8), NisaaDesign.dp(this@MainActivity, 6), NisaaDesign.dp(this@MainActivity, 8), NisaaDesign.dp(this@MainActivity, 8))
            setBackgroundColor(NisaaDesign.color(this@MainActivity, R.color.nisaa_surface))
            elevation = NisaaDesign.dp(this@MainActivity, 8).toFloat()
            minimumHeight = NisaaDesign.dp(this@MainActivity, 72)
        }
        nav.addView(navItem(AppScreen.HOME, R.drawable.ic_home, R.string.nav_home))
        nav.addView(navItem(AppScreen.CYCLE, R.drawable.ic_cycle, R.string.nav_cycle))
        nav.addView(navItem(AppScreen.FAITH, R.drawable.ic_faith, R.string.nav_faith))
        nav.addView(navItem(AppScreen.MARRIAGE, R.drawable.ic_relationship, R.string.nav_marriage))
        nav.addView(navItem(AppScreen.SETTINGS, R.drawable.ic_settings, R.string.nav_settings))
        return nav
    }

    private fun navItem(screen: AppScreen, iconRes: Int, labelRes: Int): View {
        val selected = currentScreen == screen
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            contentDescription = getString(labelRes)
            setOnClickListener { currentScreen = screen; render() }
            if (selected) background = NisaaDesign.rounded(this@MainActivity, NisaaDesign.color(this@MainActivity, R.color.nisaa_rose_soft), null, 16)
        }
        val icon = NisaaDesign.icon(this, iconRes, if (selected) R.color.nisaa_rose_dark else R.color.nisaa_ink_soft, 22)
        item.addView(icon)
        item.addView(NisaaDesign.space(this, 0, 3))
        item.addView(NisaaDesign.text(this, getString(labelRes), 10f, if (selected) R.color.nisaa_rose_dark else R.color.nisaa_ink_soft, selected, Gravity.CENTER))
        return item.apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                setMargins(NisaaDesign.dp(this@MainActivity, 2), 0, NisaaDesign.dp(this@MainActivity, 2), 0)
            }
        }
    }

    internal fun navigate(screen: AppScreen) {
        currentScreen = screen
        render()
    }

    internal fun dateText(date: LocalDate?): String = date?.format(dateFormatter) ?: getString(R.string.optional)

    internal fun dateValue(date: LocalDate?): String = date?.format(dateFormatter) ?: getString(R.string.date_picker)

    internal fun showDatePicker(initial: LocalDate, onSelected: (LocalDate) -> Unit) {
        DatePickerDialog(
            this,
            { _, year, month, day -> onSelected(LocalDate.of(year, month + 1, day)) },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }

    internal fun showToast(message: CharSequence) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    internal fun isOnline(): Boolean = runCatching {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return@runCatching false
        val network = manager.activeNetwork ?: return@runCatching false
        val capabilities = manager.getNetworkCapabilities(network) ?: return@runCatching false
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }.getOrDefault(false)

    internal fun calculateInsight(): CycleInsight {
        val current = profile ?: return biologicalEngine.calculate(emptyList())
        return biologicalEngine.calculate(
            repository.getCycles(current.id),
            LocalDate.now(),
            current.averageCycleLength
        )
    }

    internal fun confidenceLabel(confidence: Confidence): String = when (confidence) {
        Confidence.INSUFFICIENT_DATA -> getString(R.string.confidence_insufficient)
        Confidence.LOW -> getString(R.string.confidence_low)
        Confidence.MODERATE -> getString(R.string.confidence_moderate)
        Confidence.HIGHER_HISTORICAL_CONFIDENCE -> getString(R.string.confidence_higher)
    }

    internal fun flowLabel(flow: FlowIntensity): String = when (flow) {
        FlowIntensity.NONE -> getString(R.string.flow_none)
        FlowIntensity.LIGHT -> getString(R.string.flow_light)
        FlowIntensity.MEDIUM -> getString(R.string.flow_medium)
        FlowIntensity.HEAVY -> getString(R.string.flow_heavy)
        FlowIntensity.UNKNOWN -> getString(R.string.flow_unknown)
    }

    internal fun relationshipStatusText(status: RelationshipStatus): String = when (status) {
        RelationshipStatus.ACTIVE -> getString(R.string.relationship_active)
        RelationshipStatus.PENDING -> getString(R.string.relationship_pending)
        RelationshipStatus.REVOKED -> getString(R.string.relationship_revoked)
        RelationshipStatus.EXPIRED -> getString(R.string.state_empty)
    }

    internal fun isPermissionAllowed(relationship: Relationship, permission: PermissionKey): Boolean =
        relationship.status == RelationshipStatus.ACTIVE && relationship.permissions.permissions[permission] == true

    internal fun currentRelationship(): Relationship? {
        val id = profile?.id ?: return null
        return repository.getRelationships(id).firstOrNull { it.id == selectedRelationshipId }
            ?: repository.getRelationships(id).firstOrNull { it.status == RelationshipStatus.ACTIVE }
    }

    internal fun resetEntryState() {
        selectedCycleStart = LocalDate.now()
        selectedCycleEnd = null
        selectedFlow = FlowIntensity.UNKNOWN
        selectedSymptoms.clear()
        customObservation = ""
    }

    internal fun configureSystemBars() {
        window.statusBarColor = NisaaDesign.color(this, R.color.nisaa_cream)
        window.navigationBarColor = NisaaDesign.color(this, R.color.nisaa_cream)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun handlePairingIntent(intent: Intent?) {
        val token = intent?.data?.getQueryParameter("token") ?: return
        if (token.isNotBlank()) {
            lastInvitation = repository.findInvitation(token)
            if (profile?.onboardingComplete == true) navigate(AppScreen.MARRIAGE)
        }
    }

    companion object {
        const val ONBOARDING_STEPS = 10
        const val NOTIFICATION_PERMISSION_REQUEST = 701
    }
}
