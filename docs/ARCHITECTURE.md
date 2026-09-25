# Architecture and safety decisions

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
