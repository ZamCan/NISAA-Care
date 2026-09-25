# NISAA CARE implementation plan

## Phase 1 — foundation (implemented in this repository)

- Native Kotlin Android module targeting SDK 35, min SDK 26, Java/Kotlin 21.
- Layered packages for design, localization, security, local persistence,
  domain, repositories, synchronization contracts, and UI.
- Trilingual resource foundation and RTL behavior.
- SQLite schema with explicit migrations and repository boundaries.
- Onboarding, role-aware home shell, navigation, cycle entry, history, custom
  calendar, fertility, content, relationship, settings, and review states.

## Phase 2 — biological engine and cycle tracking

- Expand deterministic calculation fixtures and boundary tests.
- Add optional encrypted backup/export and migration UX.
- Add import/export validation and user-controlled data portability.
- Validate reminders on physical devices and accessibility review.

## Phase 3 — governed health and Islamic content

- Editorial workflow with named reviewers, source references, translation
  review, and publication audit history.
- Signed content package format, manifest verification, rollback, and update
  screen.
- Populate only verified, source-attributed records; keep disagreements
  explicit.

## Phase 4 — quotations and translation rotation

- Editorial quotation catalog, translation-group linking, audience/season
  rules, and 365-day deterministic rotation.
- Duplicate-avoidance and repeat-window tests for each rotation class.

## Phase 5–8 — relationships, husband experience, sync, pairing

- Backend gateway implementation only after a real service and threat model
  are available.
- Relationship-settings sync remains separate from health-data sync.
- Explicit permission grants/revokes, relationship isolation, multi-wife
  support, offline conflict policy, short-lived pairing invitations, and
  user-initiated WhatsApp deep links.
- No automatic messaging and no SMS reminder engine.

## Phase 9–10 — hardening and release

- Threat model, device testing, privacy review, notification-channel review,
  performance/accessibility pass, signing, AAB validation, and Play metadata.
