# Pre-desktop consolidation record

## Consolidated branch

- Branch: `main`
- Integration history: `engineering/premium-native-pairing-knowledge`, which
  absorbed `engineering/consolidation-pre-desktop`
- The engineering branches are retained as history, not as separate products.

`main` is the single branch to clone. Every other branch is an ancestor of it
or an ancestor of a branch that was merged into it, so no work is stranded on a
scattered branch.

## Consolidation outcome

Every source branch was compared file by file against the integration branch
before merging. The result:

| Source branch | Outcome |
|---|---|
| `engineering/premium-native-pairing-knowledge` | the integration base |
| `engineering/consolidation-pre-desktop` | merged: engine composition root, phase-segmented cycle wheel, consolidation record |
| `engineering/pairing-qr-whatsapp` | QR generation taken, in a reconciled form; see below |
| `engineering/theme-and-source-foundation` | already present, nothing to merge |
| `engineering/premium-graphical-ux` | already present, nothing to merge |
| `engineering/biological-health-core` | already present, nothing to merge |
| `engineering/premium-foundation` | already present, nothing to merge |
| `engineering/premium-native-polish-v2` | already an ancestor, nothing to merge |

### What the QR branch needed reconciled

The QR branch declared a second `PairingPayload` class in the same package as
the existing `PairingExchange` payload, so copying it as written would not
compile, and it used a different URI format.

Rather than pick one, the existing canonical format is kept for encoding and
the parser was extended to also accept the legacy format. A pairing code may
have been produced by a different build than the one receiving it, so accepting
both is the correct behaviour rather than a compromise.

The branch's camera-scanning dependency was deliberately not taken. It would
require the CAMERA permission in a privacy-first health application for a
convenience feature. QR *generation* needs no permission and was taken, using
the plain Java ZXing core encoder so the app gains no AndroidX surface.

The branch's parser also used `android.net.Uri`, which throws outside an
instrumented test. The integrated parser is framework-free and unit tested.

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

Clone `main`. It is the complete engine:

```sh
git clone https://github.com/ZamCan/NISAA-Care.git
cd NISAA-Care
git status
git log --oneline -5
```

To verify locally:

```sh
./gradlew test
./gradlew assembleDebug
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
