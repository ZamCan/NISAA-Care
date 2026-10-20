package com.zamcan.nisaacare

import com.zamcan.nisaacare.core.ads.AdsConsent
import com.zamcan.nisaacare.core.ads.AdsConfiguration

/**
 * Advertising entry points used by the activity.
 *
 * Kept as extension functions on [MainActivity] so the consent flow lives in
 * one place rather than being spread across the screen builders.
 */

/**
 * Opens the advertising consent sheet so the user can change their choice.
 *
 * The stored decision is cleared first, which means the sheet always opens.
 * If the user dismisses without choosing, no decision is recorded and the
 * previous state stays in effect.
 */
internal fun MainActivity.showAdsConsentSheet() {
    adsConsentStore.reset()
    AdsConsentPresenter.show(this, adsConsentStore)
}

/**
 * Initialises the advertising SDK if the user has opted in.
 *
 * Called once the activity is created. Returns without doing anything when the
 * build has no advertising, when the SDK is absent, or when the user has not
 * consented, so the SDK is never initialised on a user's behalf.
 */
internal fun MainActivity.initialiseAdsIfConsented() {
    if (!AdsConfiguration.enabled) return
    if (adsConsentStore.current() != AdsConsent.GRANTED) return
    if (!adProvider.isAvailable) return
    adProvider.initialise(this)
}
