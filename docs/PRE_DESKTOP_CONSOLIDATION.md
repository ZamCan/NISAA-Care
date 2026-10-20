# Pre-desktop consolidation record

## Integration branch

- Branch: `engineering/consolidation-pre-desktop`
- Foundation: `engineering/premium-native-pairing-knowledge`
- Protected production baseline: `main` (not modified by this consolidation)
- Validation PR: https://github.com/ZamCan/NISAA-Care/pull/7

This branch is a consolidation candidate, not a release-certified snapshot. Keep the source branches until the Android verification workflow passes and the remaining integration decisions are reviewed.

## Changes already integrated

The four current hero-vector drawables were copied from the latest `main` version to replace older vector geometry that used empty `<circle>` elements:

- `app/src/main/res/drawable/nisaa_hero_faith.xml`
- `app/src/main/res/drawable/nisaa_hero_family.xml`
- `app/src/main/res/drawable/nisaa_hero_marriage.xml`
- `app/src/main/res/drawable/nisaa_hero_wellness.xml`

## Branch reconciliation decisions

### Biological health

Reviewed `engineering/biological-health-core` against this integration branch. The biological engine, cycle-care assessment, models, and core documentation are already present. The integration branch's health-source catalog contains more reviewed source entries than the older branch version, so it was retained rather than replaced with the smaller catalog. Overlapping screen builders and localized strings were not copied wholesale because doing so could discard newer UX or translations.

### QR pairing

The QR branch has a different `PairingPayload` contract from the current `PairingExchange` / `PairingScanner` protocol. The QR implementation also uses ZXing, which is not currently declared in the app's Gradle dependencies. These pieces must be reconciled into one canonical, expiring, revocable pairing format before the QR code is added; do not copy the duplicate payload class as-is.

### Graphical UX, themes, and source foundation

These branches overlap with the native navigation, screen builders, UI kit, theme resources, and source documentation in the integration base. They must be compared by behavior and individual file, not merged blindly.

## Required verification before desktop handoff

The draft PR runs the repository workflow:

```sh
./gradlew test assembleDebug bundleRelease
```

Do not call this branch ready until the workflow succeeds and the generated debug APK and release AAB artifacts are available. If it fails, fix the reported issue on this branch and rerun the workflow. Do not merge the draft PR into `main` while it is marked validation-only.

## Desktop clone

After the verification gate passes, clone this branch (not `main` or an older feature branch):

```sh
git clone --branch engineering/consolidation-pre-desktop https://github.com/ZamCan/NISAA-Care.git
cd NISAA-Care
git status
```


## Personal cycle wheel implementation

The native `CycleRingView` now draws a full circular cycle timeline with distinct
menstruation, follicular, estimated ovulation-window, and luteal segments. A marker
indicates the current estimated cycle-day position; the center keeps the day number
readable. The Cycle screen adds a localized colour key using the existing phase strings.

The wheel is intentionally an explanatory estimate based on the configured/derived
cycle length. It does not establish that ovulation occurred, diagnose a condition,
or identify safe days for contraception. Predictions must remain qualified by the
cycle data quality and confidence shown on the screen.

Validation note: the previously recorded workflow run (39461045069) succeeded for
the earlier consolidation commit. The new wheel commits require a fresh CI run before
the integration branch can be certified; the earlier success is not evidence that
these latest changes have compiled.
