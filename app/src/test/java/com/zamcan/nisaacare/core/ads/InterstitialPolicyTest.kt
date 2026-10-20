package com.zamcan.nisaacare.core.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the interstitial rate limit.
 *
 * The point of these rules is that an ad appears at a natural break and at a
 * reasonable rate. Without them an interstitial fires on every navigation,
 * which is the behaviour that makes an ad experience feel hostile.
 */
class InterstitialPolicyTest {

    @Test
    fun firstAdIsAllowedImmediately() {
        val policy = InterstitialPolicy()
        assertTrue(policy.canShow(nowMillis = 1_000_000L))
    }

    @Test
    fun secondAdIsBlockedUntilTheIntervalPasses() {
        val policy = InterstitialPolicy()
        policy.onNavigation()
        policy.onShown(nowMillis = 1_000_000L)

        // One second later the interval has not elapsed.
        assertFalse(policy.canShow(nowMillis = 1_001_000L))
    }

    @Test
    fun secondAdIsAllowedAfterTheIntervalAndNavigationCount() {
        val policy = InterstitialPolicy()
        policy.onNavigation()
        policy.onShown(nowMillis = 1_000_000L)

        // Past the five minute interval, but not enough navigations yet.
        policy.onNavigation()
        assertFalse(policy.canShow(nowMillis = 2_000_000L))

        // Past the interval and past the navigation count.
        policy.onNavigation()
        policy.onNavigation()
        assertTrue(policy.canShow(nowMillis = 2_000_000L))
    }

    @Test
    fun navigationCountAloneIsNotEnough() {
        val policy = InterstitialPolicy()
        policy.onNavigation()
        policy.onShown(nowMillis = 1_000_000L)

        // Many navigations, but well inside the five minute interval.
        repeat(10) { policy.onNavigation() }
        assertFalse(policy.canShow(nowMillis = 1_100_000L))
    }

    @Test
    fun timeAloneIsNotEnough() {
        val policy = InterstitialPolicy()
        policy.onNavigation()
        policy.onShown(nowMillis = 1_000_000L)

        // A long time has passed, but the user has not moved between screens.
        assertFalse(policy.canShow(nowMillis = 100_000_000L))
    }

    @Test
    fun resetClearsTheHistory() {
        val policy = InterstitialPolicy()
        policy.onNavigation()
        policy.onShown(nowMillis = 1_000_000L)
        assertFalse(policy.canShow(nowMillis = 1_001_000L))

        // A new consent should not inherit the old rate limit.
        policy.reset()
        assertTrue(policy.canShow(nowMillis = 1_001_000L))
    }
}
