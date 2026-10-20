# Advertising and consent

NISAA CARE is offline-first and privacy-first. Advertising is **opt-in,
off by default, and never appears against reading content**. This document
describes exactly what the app does, how to enable it, and what you must
declare if you do.

## Design rules

These are enforced in code, not by convention. The policy lives in
`app/src/main/java/com/zamcan/nisaacare/core/ads/AdsPolicy.kt` and every ad
request passes through it.

| Rule | Enforcement |
|---|---|
| No ad without an explicit opt-in | `AdsConsent.allowsAds` is true only for `GRANTED`. An undecided user behaves exactly like a user who declined. |
| Non-personalised ads only | `AdsPolicy.PERSONALISED_ADS_PERMITTED` is `false` and is not configurable. |
| No advertising identifier | `AdsPolicy.ADVERTISING_ID_PERMITTED` is `false`. The SDK never receives the identifier. |
| No ads on reading screens | `AdSlot.PROHIBITED` covers the topic article and quotation screens. |
| Banners and interstitials only | `AdFormat` contains no rewarded video. |

### Why no rewarded video

Rewarded video requires offering something in exchange for watching an ad.
That changes the character of the product: the app would be nudging people
toward watching advertising in order to unlock features. The chosen formats are
a banner at the foot of a list and an interstitial at a natural break.

### Why the identifier is not requested

The advertising identifier is what allows an ad network to build a profile of
a user across apps. In an app where the activity being profiled is menstrual
and fertility data, that profile is sensitive by construction. Leaving the
identifier alone keeps the SDK's view of the user as coarse as possible.

## Formats and placement

**Banner.** An anchored adaptive banner rendered inside a glass surface at the
foot of the scrolling content. It moves with the list rather than floating
over it. The slot collapses to nothing when no ad is available, so no empty
strip is ever reserved.

Banner placement: home, cycle, fertility, health, faith, settings, marriage,
content version and about screens.

**Interstitial.** Shown only at a navigation break, and only when both rate
limits allow. See `InterstitialPolicy`:

- at least **three navigations** since the last ad, and
- at least **five minutes** since the last ad.

Both must be satisfied. The time limit alone is not enough, because a user
tapping quickly through sections would otherwise see an ad every time; the
navigation limit alone is not enough either, because a user who stays on one
screen for ten minutes and then changes section would otherwise be shown an ad
on a single tap.

Interstitials are never shown on the topic article or quotation screens.

## Enabling advertising

Advertising is off in a default checkout. Three Gradle properties control it:

```
-Pnisaa.ads.enabled=true
-Pnisaa.admob.appId=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
-Pnisaa.admob.safeUnitId=ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZ
-Pnisaa.admob.interstitialUnitId=ca-app-pub-XXXXXXXXXXXXXXXX/WWWWWWWWW
```

Example:

```bash
gradle -Pnisaa.ads.enabled=true \
        -Pnisaa.admob.appId=ca-app-pub-1234567890/1234567890 \
        -Pnisaa.admob.safeUnitId=ca-app-pub-1234567890/1111111111 \
        -Pnisaa.admob.interstitialUnitId=ca-app-pub-1234567890/2222222222 \
        :app:assembleRelease
```

No account identifier is committed to the repository. The manifest
`meta-data` entry resolves to an empty string in a default build, which the
SDK treats as "not configured" and refuses to initialise.

### Test ads during development

If you enable advertising but supply no ad units, the build falls back to
Google's own published test identifiers:

| Purpose | Test identifier |
|---|---|
| Application | `ca-app-pub-3940256099942544/6300978111` |
| Banner | `ca-app-pub-3940256099942544/6300978111` |
| Interstitial | `ca-app-pub-3940256099942544/1033173712` |

These serve test ads only and earn nothing. They exist so the slots can be
seen working before a live account exists. Do not ship a build that uses them.

## Source-set layout

The SDK is referenced from exactly one file, which lives in a source set that
is only compiled for enabled builds:

```
app/src/main/java/.../core/ads/AdProvider.kt          interface, always compiled
app/src/main/java/.../core/ads/NoOpAdProvider.kt      no-op, always compiled
app/src/main/java/.../core/ads/AdProviderFactory.kt   picks the implementation
app/src/ads/java/.../core/ads/AdmobAdProvider.kt      only compiled when enabled
```

`app/build.gradle.kts` adds `src/ads/java` to the main source set only when
`nisaa.ads.enabled` is true, and adds the SDK dependencies under the same
condition. The factory finds the AdMob implementation with one reflective
lookup and falls back to the no-op provider if it is absent.

The result is that a default build cannot link the SDK, cannot initialise it,
and cannot request an ad, regardless of what the consent store says.

## Play Console declarations

If you enable advertising, these are required. Google will reject the
submission otherwise, and the app should not be published while they are
inaccurate.

1. **Data safety.** Declare that the app collects and shares device or
   advertising identifiers, and that ads are personalised. Complete the
   "Ads" declaration and note that the app uses the advertising identifier
   through the Google Mobile Ads SDK.
2. **Ads declaration.** Complete the "Does your app contain ads?" section in
   the app content rating questionnaire.
3. **Privacy policy.** The policy must describe the use of the Google Mobile
   Ads SDK, the consent flow, and how to withdraw consent. The in-app
   Advertising row under Settings lets a user change their choice.
4. **Content rating.** The questionnaire asks about advertising in apps
   aimed at children. NISAA CARE is a general-audience health app; if it is
   later aimed at children, the Families policy rules and the restricted ad
   categories apply.

## Testing notes

- `AdsPolicyTest` covers consent gating, slot prohibition and format limits.
- `InterstitialPolicyTest` covers both rate limits independently, including
  the case where one passes and the other does not.
- The `ads` source set is not compiled by the default build, so it is not
  covered by the default test run. Build once with
  `-Pnisaa.ads.enabled=true` to compile it and confirm it still builds.
