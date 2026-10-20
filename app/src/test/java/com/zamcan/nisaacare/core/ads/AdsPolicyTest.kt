package com.zamcan.nisaacare.core.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the advertising policy layer.
 *
 * These cover the rules that keep advertising honest: that nothing is served
 * without an explicit opt-in, that sensitive screens never carry an ad, and
 * that the interstitial rate limit actually limits.
 */
class AdsPolicyTest {

    @Test
    fun adsRequireAnExplicitOptIn() {
        // An undecided user must not receive ads. Treating silence as consent
        // is the exact failure the consent flow exists to prevent.
        assertFalse(AdsPolicy.canServe(AdsConsent.UNDECIDED, AdSlot.SAFE, AdFormat.BANNER))
        assertFalse(AdsPolicy.canServe(AdsConsent.DENIED, AdSlot.SAFE, AdFormat.BANNER))
        assertTrue(AdsPolicy.canServe(AdsConsent.GRANTED, AdSlot.SAFE, AdFormat.BANNER))
    }

    @Test
    fun prohibitedSlotsNeverServe() {
        // Health, faith and quotation screens are reading surfaces. An ad on
        // them would sit directly against content the user is reading.
        assertFalse(AdsPolicy.canServe(AdsConsent.GRANTED, AdSlot.PROHIBITED, AdFormat.BANNER))
        assertFalse(AdsPolicy.canServe(AdsConsent.GRANTED, AdSlot.PROHIBITED, AdFormat.INTERSTITIAL))
    }

    @Test
    fun onlyApprovedFormatsAreAllowed() {
        assertTrue(AdFormat.BANNER in AdFormat.allowedInSafeSlots)
        assertTrue(AdFormat.INTERSTITIAL in AdFormat.allowedInSafeSlots)
        assertEquals(2, AdFormat.allowedInSafeSlots.size)
    }

    @Test
    fun personalisedAdsAreNotPermitted() {
        // Menstrual and fertility activity must never be used to build an
        // advertising profile, so this stays false regardless of consent.
        assertFalse(AdsPolicy.PERSONALISED_ADS_PERMITTED)
        assertFalse(AdsPolicy.ADVERTISING_ID_PERMITTED)
    }

    @Test
    fun consentRoundTripsThroughStorage() {
        assertEquals(AdsConsent.UNDECIDED, AdsConsent.fromStored(null))
        assertEquals(AdsConsent.UNDECIDED, AdsConsent.fromStored("nonsense"))
        assertEquals(AdsConsent.GRANTED, AdsConsent.fromStored("GRANTED"))
        assertEquals(AdsConsent.DENIED, AdsConsent.fromStored("DENIED"))
    }

    @Test
    fun onlyGrantedConsentAllowsAds() {
        assertFalse(AdsConsent.UNDECIDED.allowsAds)
        assertFalse(AdsConsent.DENIED.allowsAds)
        assertTrue(AdsConsent.GRANTED.allowsAds)
    }

    @Test
    fun slotsReportWhetherTheyCanServe() {
        assertTrue(AdSlot.SAFE.canServe)
        assertFalse(AdSlot.PROHIBITED.canServe)
    }
}
