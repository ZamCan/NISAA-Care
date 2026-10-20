package com.zamcan.nisaacare.core.ads

import android.content.Context

/**
 * Decides when an interstitial may be shown.
 *
 * "Timely" is the whole requirement here. An interstitial that fires on a
 * timer is an interruption; one that appears at a natural break, at a
 * reasonable rate, is an acceptable trade for the user.
 *
 * Two rules govern it:
 *
 *  1. A minimum interval between ads, so the user is never shown one
 *     immediately after the last.
 *  2. A minimum number of navigations between ads, so short bursts of
 *     navigation do not produce a run of interstitials.
 *
 * Both must be satisfied. This is the difference between an ad experience
 * that feels designed and one that feels hostile.
 */
class InterstitialPolicy(
    /** Minimum wall-clock gap between two interstitials. */
    private val minimumIntervalMillis: Long = DEFAULT_MINIMUM_INTERVAL_MILLIS,

    /** Minimum number of screen navigations between two interstitials. */
    private val minimumNavigations: Int = DEFAULT_MINIMUM_NAVIGATIONS
) {

    private var lastShownAtMillis: Long = 0L
    private var lastShownNavigation: Int = 0
    private var navigationCount: Int = 0

    /**
     * Records a navigation.
     *
     * Called every time the user moves between screens, whether or not an ad
     * is shown.
     */
    fun onNavigation() {
        navigationCount++
    }

    /**
     * Whether an interstitial may be shown right now.
     */
    fun canShow(nowMillis: Long): Boolean {
        if (lastShownAtMillis == 0L) return true
        if (nowMillis - lastShownAtMillis < minimumIntervalMillis) return false
        if (navigationCount - lastShownNavigation < minimumNavigations) return false
        return true
    }

    /**
     * Records that an interstitial was shown.
     */
    fun onShown(nowMillis: Long) {
        lastShownAtMillis = nowMillis
        lastShownNavigation = navigationCount
    }

    /**
     * Forgets the history.
     *
     * Used when the user revokes consent, so a new consent does not inherit
     * the old rate limit.
     */
    fun reset() {
        lastShownAtMillis = 0L
        lastShownNavigation = 0
        navigationCount = 0
    }

    companion object {
        /**
         * Five minutes between interstitials.
         *
         * Long enough that a user working through several screens in a row
         * sees at most one.
         */
        const val DEFAULT_MINIMUM_INTERVAL_MILLIS: Long = 5 * 60 * 1000L

        /**
         * Three navigations between interstitials.
         *
         * Combined with the time interval, this stops an ad appearing every
         * time the user taps into a different section.
         */
        const val DEFAULT_MINIMUM_NAVIGATIONS: Int = 3
    }
}
