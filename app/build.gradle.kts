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
}
