# NISAA CARE — Biological & Health Core

## Scope

This stage advances NISAA CARE from a cycle calculator into a structured, explainable cycle-and-care layer:

- cycle day
- educational phase label
- data-quality state
- deterministic fertility estimates
- non-diagnostic care-review prompts
- governed clinical source catalog
- localized UX for Swahili, English and Arabic

## Clinical boundary

The biological engine is an educational estimation system. It does not diagnose disease, infer pregnancy, prescribe treatment, or promise contraception or pregnancy prevention.

The current bundled calculation uses recorded period starts and preserves its calculation version with historical estimates. A phase label is an educational approximation derived from the recorded cycle day; it must not be presented as a measurement of ovulation.

The care-assessment layer only identifies logged patterns that may merit professional review. It does not attach a disease name to a user.

## Current source boundary

The editorial source catalog records:

- WHO, *Menstrual health*: https://www.who.int/news-room/fact-sheets/detail/menstrual-health
- ACOG, *Abnormal Uterine Bleeding*: https://www.acog.org/womens-health/faqs/abnormal-uterine-bleeding
- ACOG, *Amenorrhea: Absence of Periods*: https://www.acog.org/womens-health/faqs/amenorrhea-absence-of-periods

WHO currently describes the average menstrual cycle as 21–35 days and emphasizes accurate education, dignity, and timely access to care. ACOG similarly describes 21–35 days as typical for adults and identifies prolonged/heavy bleeding and substantially irregular timing as reasons for clinical discussion. These sources inform editorial review; they are not copied into runtime rules without review.

## UX flow

Recorded history → Current cycle view → Cycle day → Educational phase → Pattern/data quality → Estimate → Explain limits → Optional care-review prompt

The cycle screen should remain useful with one record and become more informative as history grows. The app must visibly distinguish:

- recorded observations
- calculated estimates
- educational interpretation
- care-review prompts

## Next hardening gate

Before treating this layer as release-ready:

1. run unit tests
2. build the APK in Termux
3. install on Android 15/API 35
4. verify light/dark layouts
5. verify Arabic RTL
6. verify small-screen scrolling and touch targets
7. verify offline operation
8. verify history survives app restart
9. verify calculation-version persistence
10. perform medical-content editorial review before promoting source-backed prose to VERIFIED/PUBLISHED
