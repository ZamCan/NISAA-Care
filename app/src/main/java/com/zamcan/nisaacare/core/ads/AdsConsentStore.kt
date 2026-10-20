package com.zamcan.nisaacare.core.ads

import android.content.Context
import android.content.SharedPreferences

/**
 * Stores the user's advertising consent.
 *
 * The choice is written once and then respected. A user who opts out is never
 * asked again, because repeatedly prompting a user who already declined is
 * both annoying and, in several jurisdictions, unlawful.
 */
class AdsConsentStore(context: Context) {

    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    /** The recorded consent state. */
    fun current(): AdsConsent = AdsConsent.fromStored(preferences.getString(KEY_CONSENT, null))

    /** Whether the user has already made a choice, one way or the other. */
    fun hasDecision(): Boolean = current() != AdsConsent.UNDECIDED

    /** Records an explicit opt-in. */
    fun grant() = preferences.edit().putString(KEY_CONSENT, AdsConsent.GRANTED.name).apply()

    /** Records an explicit opt-out. */
    fun deny() = preferences.edit().putString(KEY_CONSENT, AdsConsent.DENIED.name).apply()

    /**
     * Clears the stored decision.
     *
     * Only used by the settings "reset consent" action, so a user who changed
     * their mind can be asked again. The app never calls this on its own.
     */
    fun reset() = preferences.edit().remove(KEY_CONSENT).apply()

    companion object {
        private const val FILE_NAME = "nisaa_ads_consent"
        private const val KEY_CONSENT = "consent_state"
    }
}
