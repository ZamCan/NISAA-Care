# NISAA CARE External Sources of Truth

Last reviewed: 2026-09-25

NISAA CARE uses external sources as evidence inputs, not as an uncontrolled runtime authority. User health records and the deterministic calculation engine remain local-first.

## Source classes

### Menstrual health and general health
- World Health Organization (WHO) — Menstrual health:
  https://www.who.int/news-room/fact-sheets/detail/menstrual-health
- American College of Obstetricians and Gynecologists (ACOG) — Abnormal uterine bleeding:
  https://www.acog.org/womens-health/faqs/abnormal-uterine-bleeding
- ACOG — Amenorrhea:
  https://www.acog.org/womens-health/faqs/amenorrhea-absence-of-periods
- WHO — Endometriosis:
  https://www.who.int/news-room/fact-sheets/detail/endometriosis

These sources should inform reviewed educational content and safety/referral rules. They must not be scraped into executable medical rules without a human review step.

## Content ingestion rule

External content is handled as:

1. Fetch a source through a controlled repository/sync adapter.
2. Parse only the fields needed by NISAA CARE.
3. Normalize into an internal content DTO.
4. Attach source URL, publisher, publication/review date, retrieval date, language, and content version.
5. Mark imported material REVIEW_REQUIRED.
6. Human/content reviewer verifies the material.
7. Only VERIFIED or PUBLISHED material may be shown as authoritative in the app.
8. Preserve old published versions when an update changes a claim.
9. Never silently replace a user's health record or historical calculation.

## Medical calculation boundary

The biological engine is deterministic and versioned. External sources can update educational thresholds, warning explanations, or algorithm configuration only through a reviewed release. They do not directly execute code or alter calculations at runtime.

NISAA CARE must not diagnose disease, promise pregnancy, or present calendar estimates as guaranteed contraception.

## Islamic/religious content boundary

Qur'an, Hadith, fiqh and scholarly material require provenance and review. The app must never manufacture quotations or citations. Where scholarly positions differ, the content model should identify the methodology/school or present the disagreement rather than flattening it into one universal rule.

Religious content should be ingested from verified/licensed sources with explicit attribution and checked translations. Do not scrape arbitrary websites and publish their material automatically.

## Runtime networking

Network access is optional. Offline operation remains the baseline. Future sync should use signed/versioned content packages with integrity validation, transactional application, and rollback on failure.

Private health or relationship data must never be placed in external-source URLs, query strings, logs, or public content packages.
