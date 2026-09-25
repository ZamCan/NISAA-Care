# NISAA CARE

**Women's Health • Fertility • Family • Wellbeing**

NISAA CARE is a privacy-first, offline-first Android foundation for cycle
tracking, fertility estimates, wellbeing education, relationship-scoped
sharing, and source-governed Islamic-context content. The app is intentionally
honest about uncertainty and content review: it does not diagnose, does not
promise pregnancy prevention, and does not present unreviewed religious or
medical text as authoritative.

## Current implementation

This repository contains a first production-oriented foundation, not a claim
that every planned product surface is complete. It includes:

- Kotlin + native Android platform views (no paid APIs or cloud dependency).
- Swahili (default), English, and Arabic resources with RTL foundation.
- Premium calm design system with vector icons, accessible touch targets,
  cards, chips, state panels, custom calendar, and light/dark color tokens.
- Progressive onboarding, role-aware home shell, cycle entry/history, custom
  menstrual calendar, fertility estimates, health/faith content states,
  marriage/permissions, husband view, settings, sources, and content-version
  screens.
- Versioned deterministic biological engine and rotation/content governance
  contracts.
- Platform SQLite schema and repositories for profiles, cycles, symptoms,
  fertility estimates, Islamic content, quotations, relationships, pairing
  invitations, permissions, support requests, preferences, and content state.
- Relationship isolation rules, revocable permissions, expiring random pairing
  invitations, privacy-conscious notification settings, and sync interfaces.
- Android Keystore-backed encryption foundation for sensitive free-text values.
- Debug and release build configuration; release signing is opt-in through
  untracked Gradle properties.

## Build

The checked-in wrapper targets Gradle 8.9. The Android toolchain used for the
initial verification is Java 21, Android SDK platform 35, build tools 34.0.0,
and the Termux AAPT2 override in `gradle.properties`.

```bash
export GRADLE_USER_HOME=/data/data/com.termux/files/home/.gradle
./gradlew test
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease
```

`local.properties` is intentionally ignored. Copy the supplied
`local.properties` shape on another machine or set `ANDROID_SDK_ROOT` and run
`./gradlew --version` to verify the local SDK.

### Safe release signing

No private key is included. To sign a release locally, provide these Gradle
properties through `~/.gradle/gradle.properties` or an untracked local file:

```properties
NISAA_RELEASE_STORE_FILE=/absolute/path/to/nisaa-care-upload.jks
NISAA_RELEASE_STORE_PASSWORD=...
NISAA_RELEASE_KEY_ALIAS=...
NISAA_RELEASE_KEY_PASSWORD=...
```

The release build remains available without a key as an unsigned development
artifact; do not publish it until signing and Play App Signing are configured.

## Safety and content governance

Authoritative content must carry a source, reference, language, version, and
review state (`DRAFT`, `REVIEW_REQUIRED`, `VERIFIED`, `PUBLISHED`, or
`RETIRED`). Initial catalog records are explicit review placeholders rather
than fabricated medical, Qur'an, hadith, or fiqh claims. A future signed
content package can be validated and applied transactionally without changing
user health records.

See `docs/IMPLEMENTATION_PLAN.md` and `docs/ARCHITECTURE.md` for the staged
engineering plan and safety decisions.
