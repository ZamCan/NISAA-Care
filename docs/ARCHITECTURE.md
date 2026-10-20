# Architecture and safety decisions

## Single application engine

`core/engine/NisaaCareEngine.kt` is the only concrete application composition
root. `NisaaApplication` creates it once, and screens consume its shared
repository and domain services. `AppContainer` is only a source-compatibility
type alias; it does not create a second engine. Keep feature algorithms in
their focused packages and wire them through this one root rather than
constructing duplicate engines in activities or screens.

The app remains a single Gradle Android application module (`:app`) so a clean
clone builds as one app. Focused Kotlin packages are not separate apps or
unlinked engines: they are components assembled by the single root.

## Layers

- `domain/` contains deterministic, UI-independent rules and immutable models.
- `data/local/` owns SQLite migrations, encrypted values, and repository
  adapters.
- `data/repository/` exposes storage-facing contracts to the domain/UI.
- `core/` contains design tokens, locale handling, security, sync, and content
  update contracts.
- `features/` is represented by UI screen builders and view state models in
  this first single-module build so the app remains easy to install while the
  domain boundaries are kept portable to future Gradle modules.

## Data ownership

The local device is the source of truth in Phase 1. Relationship settings can
later synchronize, but no health record is automatically shared. A husband
view receives only records covered by a current, non-revoked relationship
permission. Each relationship is an isolation boundary; husband users may
have multiple independent relationships.

## Safety decisions

- Biological outputs are estimates with an algorithm version and uncertainty.
  They are not contraception advice.
- A predicted biological status never silently becomes a fiqh ruling. Worship
  context is presented as source-governed education requiring review.
- Health and religious content is stored with governance state. Missing
  authoritative material is shown as `REVIEW_REQUIRED`/pending, never invented.
- Lock-screen notifications default to a generic message.
- Pairing tokens are random, short-lived, revocable, and contain no health
  information. Pairing is a local foundation until a real sync gateway exists.
- Plain Android backup is disabled. Keystore-backed AES-GCM protects selected
  sensitive free-text values; production threat modeling and optional encrypted
  backup remain release work.


## Canonical source map

| Responsibility | Canonical location |
| --- | --- |
| App composition root | `app/src/main/java/com/zamcan/nisaacare/core/engine/NisaaCareEngine.kt` |
| App startup | `NisaaApplication.kt` |
| Cycle calculation and versioning | `domain/cycle/BiologicalEngine.kt` |
| Cycle care prompts | `domain/cycle/CycleCareAssessment.kt` |
| Content governance and selection | `domain/content/ContentEngine.kt` |
| Islamic/worship context | `domain/worship/WorshipContextEngine.kt` |
| Relationship and sharing rules | `domain/relationship/` |
| Local persistence | `data/local/` and `data/content/` |
| Screen composition | `ScreenBuilders.kt`, `RelationshipScreens.kt` |
| Reusable native visual components | `ui/` |
| Unit tests | `app/src/test/` |
| Build and CI | root Gradle files and `.github/workflows/android.yml` |

Avoid creating a second implementation of a domain rule in a screen builder.
Screens call the canonical engine and format its result for the user.
