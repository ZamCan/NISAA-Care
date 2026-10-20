package com.zamcan.nisaacare.core.ads

import android.content.Context
import android.view.View

/**
 * Chooses the advertising implementation for this build.
 *
 * The Google Mobile Ads SDK is only on the classpath when the build enables
 * advertising, so the SDK-backed implementation lives in a separate source set
 * (`src/ads`) that Gradle includes only for enabled builds.
 *
 * The factory finds it with a single reflective lookup rather than a direct
 * reference, because a direct reference would not compile in the default build
 * where the SDK is absent. When the lookup fails the app falls back to
 * [NoOpAdProvider], which means a default build behaves exactly like a build
 * that has advertising switched off.
 */
object AdProviderFactory {

    private const val ADMOB_PROVIDER_CLASS =
        "com.zamcan.nisaacare.core.ads.AdmobAdProvider"

    /**
     * The provider for this build.
     *
     * Returns [NoOpAdProvider] when advertising is disabled, unconfigured, or
     * the SDK-backed implementation is not present.
     */
    fun create(context: Context): AdProvider {
        if (!AdsConfiguration.enabled) return NoOpAdProvider
        if (!AdsConfiguration.isConfigured) return NoOpAdProvider
        if (!AdsConfiguration.isSdkPresent()) return NoOpAdProvider
        return runCatching {
            val type = Class.forName(ADMOB_PROVIDER_CLASS)
            val instance = type.getDeclaredConstructor(Context::class.java).newInstance(context)
            instance as? AdProvider ?: NoOpAdProvider
        }.getOrDefault(NoOpAdProvider)
    }
}
