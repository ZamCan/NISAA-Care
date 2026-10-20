plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val releaseStorePath = providers.gradleProperty("NISAA_RELEASE_STORE_FILE").orNull
    ?.let { file(it) }
val releaseStorePasswordValue = providers.gradleProperty("NISAA_RELEASE_STORE_PASSWORD").orNull
val releaseKeyAliasValue = providers.gradleProperty("NISAA_RELEASE_KEY_ALIAS").orNull
val releaseKeyPasswordValue = providers.gradleProperty("NISAA_RELEASE_KEY_PASSWORD").orNull
val releaseSigningConfigured = releaseStorePath != null &&
    releaseStorePasswordValue != null && releaseKeyAliasValue != null &&
    releaseKeyPasswordValue != null

// Advertising configuration.
//
// Both properties are intentionally absent from the repository. A fresh
// checkout builds with advertising fully inert: the SDK is not linked, no AdMob
// account identifier exists anywhere in the source tree, and the app cannot
// request an ad.
//
// To enable, supply both at build time:
//   gradle -Pnisaa.ads.enabled=true -Pnisaa.admob.appId=ca-app-pub-... assembleRelease
//
// See docs/ADVERTISING_AND_CONSENT.md for the consent flow and the Play
// Console declarations that enabling it requires.
val adsEnabled = providers.gradleProperty("nisaa.ads.enabled")
    .map { it.toBoolean() }
    .orElse(false)
    .get()

// Google's published test identifiers. They serve test ads only and earn
// nothing. They are the fallback so the ad slots can be seen working before a
// live AdMob account exists.
val admobTestAppId = "ca-app-pub-3940256099942544/6300978111"
val admobTestBannerUnitId = "ca-app-pub-3940256099942544/6300978111"
val admobTestInterstitialUnitId = "ca-app-pub-3940256099942544/1033173712"

val admobAppId = providers.gradleProperty("nisaa.admob.appId")
    .orElse("")
    .get()

val admobSafeUnitId = providers.gradleProperty("nisaa.admob.safeUnitId")
    .orElse("")
    .get()

val admobInterstitialUnitId = providers.gradleProperty("nisaa.admob.interstitialUnitId")
    .orElse("")
    .get()

// The identifier the SDK actually initialises with. When advertising is on and
// no live account was supplied, the test identifier is used, so the SDK has
// something valid to start with and the slots can be seen working.
val effectiveAdmobAppId = if (admobAppId.isNotBlank()) admobAppId else admobTestAppId
val effectiveBannerUnitId = if (admobSafeUnitId.isNotBlank()) admobSafeUnitId else admobTestBannerUnitId
val effectiveInterstitialUnitId =
    if (admobInterstitialUnitId.isNotBlank()) admobInterstitialUnitId else admobTestInterstitialUnitId

android {
    namespace = "com.zamcan.nisaacare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zamcan.nisaacare"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "android.test.InstrumentationTestRunner"

        buildConfigField("boolean", "ADS_ENABLED", adsEnabled.toString())
        buildConfigField("String", "ADMOB_APP_ID", "\"$admobAppId\"")
        buildConfigField("String", "ADMOB_SAFE_UNIT_ID", "\"$admobSafeUnitId\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"$admobInterstitialUnitId\"")
        buildConfigField("String", "ADMOB_TEST_APP_ID", "\"$admobTestAppId\"")
        buildConfigField("String", "ADMOB_TEST_BANNER_UNIT_ID", "\"$admobTestBannerUnitId\"")
        buildConfigField("String", "ADMOB_TEST_INTERSTITIAL_UNIT_ID", "\"$admobTestInterstitialUnitId\"")

        // The AdMob application ID is a manifest meta-data entry. It must match
        // the identifier the SDK is initialised with, or the SDK refuses to
        // start and the slots stay silently empty. A default build resolves it
        // to an empty string, which the SDK treats as "not configured" and will
        // not initialise. That keeps any account identifier out of the
        // committed manifest.
        manifestPlaceholders["admobAppId"] = if (adsEnabled) effectiveAdmobAppId else ""
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    signingConfigs {
        create("release") {
            if (releaseSigningConfigured) {
                storeFile = releaseStorePath
                storePassword = releaseStorePasswordValue
                keyAlias = releaseKeyAliasValue
                keyPassword = releaseKeyPasswordValue
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        buildConfig = true
    }

    // The AdMob-backed provider lives in its own source set, compiled only when
    // advertising is enabled. This keeps the SDK reference out of the default
    // build entirely, so a default checkout cannot link the SDK by accident.
    sourceSets {
        getByName("main") {
            if (adsEnabled) {
                java.srcDir("src/ads/java")
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    // The Google Mobile Ads SDK is only linked when advertising is explicitly
    // enabled. A default build stays dependency-free, which keeps the app's
    // offline-first and privacy-first guarantees true by construction rather
    // than by policy.
    if (adsEnabled) {
        implementation("com.google.android.gms:play-services-ads:24.5.0")
        implementation("com.google.android.ump:user-messaging-platform:3.1.0")
    }
}
