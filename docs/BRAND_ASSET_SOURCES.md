# NISAA CARE Brand & Visual Asset Sources

Reviewed: 2026-09-25

## Design research

Current visual research included:
- contemporary women's wellness interfaces using warm neutrals, restrained editorial typography, soft organic shapes and spacious card composition
- African women's health photography and clinical environments for cultural/contextual reference
- Islamic geometric patterns for subtle faith-context decoration
- WHO's current menstrual-health presentation and educational framing

The research is used for visual direction, not copied product UI.

## Licensing rule

NISAA CARE should not bundle a random internet photograph merely because it is searchable. Before a third-party image enters the APK, the project must record its license, creator, source URL, modification status and redistribution conditions.

For this stage, the bundled graphical assets are original Android vector artwork created for NISAA CARE. This avoids unnecessary copyright, hotlinking, attribution and offline-availability problems while preserving a premium visual identity.

A Wikimedia Commons CC0 sacred-geometry pattern was reviewed as a possible reusable reference. The source states that it is released under CC0 and can be copied, modified and used commercially:
https://commons.wikimedia.org/wiki/File:Repeating_Tile_Sacred_Geometry_Pattern_-_Digital_Arabic_Tile.png

A separate Wikimedia Commons Islamic geometric design from the Walters Art Museum was also reviewed; its page identifies the artwork as public domain:
https://commons.wikimedia.org/wiki/File:Islamic_-_Geometric_Design_-_Walters_W8531467B_-_Full_Page.jpg

These external assets are references in this stage rather than remote runtime dependencies.

## Bundled graphical family

- nisaa_hero_wellness.xml — onboarding/home wellness identity
- nisaa_hero_faith.xml — Qur'an/faith knowledge context
- nisaa_hero_marriage.xml — marriage/support context
- nisaa_hero_family.xml — reserved family/relationship visual
- PatternView — lightweight geometric decorative layer

All bundled artwork is vector-based and scales across Android densities without shipping large photographic files.

## Premium UX composition

Major journeys should follow:
1. Context/identity
2. One clear primary action
3. Supporting information
4. Status/confidence/provenance
5. Secondary actions
6. Privacy or safety boundary where relevant
7. Offline/empty/loading/error state
8. Persistent navigation only where useful

Visual decoration must never compete with health decisions, religious source information, permissions or safety guidance.
