# NISAA CARE engineering status

This file records the engineering baseline so future work continues from the
existing implementation instead of rebuilding the project from scratch.

## Baseline verified on 2026-09-25

The main branch contains a working native Android foundation with:

- Kotlin/native Android views and Gradle build configuration.
- Local SQLite persistence and repository boundaries.
- Deterministic biological calculation foundation with versioned estimates.
- Health and Islamic content governance states.
- Relationship permissions and isolation rules.
- Short-lived pairing invitations.
- Offline-first notification/content/sync foundations.
- Swahili, English, and Arabic resource foundations with RTL support.
- Premium design tokens, vector icon system, adaptive launcher resources, and
  splash resources.
- Unit tests for biological, content, relationship, and sync contracts.
- Android verification workflow.

## Baseline verified on 2026-10-20

Branch `engineering/premium-native-pairing-knowledge` adds:

- Glassmorphic surface system: layered translucent fills, gradient hairlines,
  specular sheen, an aurora backdrop, and real `RenderEffect` blur on API 31+.
- Adaptive layout driven by window width: floating glass bottom bar on compact
  windows, glass side rail and a centred 720dp column on expanded ones.
- Consent-gated advertising layer, off by default. The SDK is only linked when
  the build enables it, only non-personalised ads are used, the advertising
  identifier is never requested, and no ad appears on a reading screen.
- Banner and interstitial formats with an interstitial rate limit of three
  navigations and five minutes.
- 37 unit tests covering the advertising policy and interstitial rate limit
  alongside the existing domain, content, relationship and sync contracts.

See `docs/ADVERTISING_AND_CONSENT.md`, `docs/DESIGN_SYSTEM.md` and
`docs/UPSTREAM_SYNC.md`.

## Active engineering sequence

### Stage A — product foundation hardening
1. Keep the current architecture and database boundaries.
2. Expand domain boundary tests before adding network behaviour.
3. Harden accessibility, state handling, localization, and navigation.
4. Refine the premium visual system without introducing bitmap dependencies.

### Stage B — biological engine
1. Expand deterministic fixtures and edge-case tests.
2. Preserve calculation version with every stored estimate.
3. Keep estimates educational and uncertainty-aware.
4. Never convert biological predictions into automatic fiqh rulings.

### Stage C — governed content
1. Add structured source/reference/reviewer metadata.
2. Keep medical and Islamic records review-gated.
3. Add signed package verification and transactional install.
4. Add rollback and installed-version tracking.
5. Populate only verified, attributed content.

### Stage D2 — monetisation and distribution
1. Keep advertising off by default and consent-gated.
2. Supply a live AdMob account only after the Play Console declarations in
   `docs/ADVERTISING_AND_CONSENT.md` are accurate.
3. Never request the advertising identifier or personalise ads.
4. Keep ads out of reading surfaces.
5. Review ad performance against retention before adding any format.

### Stage D — quotations
1. Add translation groups and provenance.
2. Add audience and seasonal eligibility.
3. Add deterministic one-year rotation.
4. Test repeat avoidance and missing-translation fallback.
5. Never manufacture religious quotations to fill calendar gaps.

### Stage E — relationships
1. Complete wife-controlled granular sharing.
2. Keep each relationship independently isolated.
3. Support multiple independent wife relationships for a husband where the
   account model permits it.
4. Add support-request lifecycle and privacy-safe notifications.
5. Keep relationship-setting sync separate from health-record sync.

### Stage F — real synchronization
No real backend is to be invented. Implement only against a documented,
threat-modelled service with explicit authentication, authorization,
revocation, conflict handling, encryption, and audit requirements.

## Release gate

Before production release:

- physical-device testing
- offline/online transition testing
- Arabic RTL testing
- accessibility review
- database migration testing
- privacy/security review
- notification privacy review
- APK installation verification
- AAB verification
- real release signing and Play App Signing configuration

The repository must not claim production readiness merely because the project
compiles.
