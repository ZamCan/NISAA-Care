# Upstream sync audit (2026-10-20)

All remote branches were fetched and compared against
`engineering/premium-native-pairing-knowledge` (the working branch).

## Branches

| Branch | Tip | Relation to working branch | Action |
|---|---|---|---|
| `main` | `cc9f3ef` | ancestor | nothing to merge |
| `engineering/premium-native-pairing-knowledge` | `4b44d2e` | **the working branch** | current |
| `engineering/premium-native-polish-v2` | `219a990` | ancestor of working branch | nothing to merge |
| `engineering/theme-and-source-foundation` | `a0d5b5e` | 7 commits behind | already integrated |
| `engineering/premium-graphical-ux` | `b902c11` | 12 commits behind | already integrated |
| `engineering/premium-foundation` | `ee1ef7a` | 8 commits behind | already integrated |
| `engineering/biological-health-core` | `fadad34` | 13 commits behind | already integrated |
| `engineering/pairing-qr-whatsapp` | `b0b5fc4` | 13 commits behind | already integrated |

## Why the other branches need no merge

Their content is present in the working branch, reached through different
history. Verified by direct diff, not by assumption:

- **Theme polish** (`edc721e`, `d0185b8`, `418c405`, `04f1528`): the four
  `styles.xml` variants are byte-identical between
  `theme-and-source-foundation` and the working branch.
- **Edge-to-edge hardening** (`0bda6b9`): `prepareEdgeToEdgeRoot` and
  `configureSystemBars` are present in `MainActivity`.
- **Semantic icon family** (`a0d5b5e`): `ic_fertility`, `ic_shield`,
  `ic_health` and `ic_book` are all wired into `ScreenBuilders.kt`.
- **Graphical hero flows** (`b902c11` and siblings): `nisaa_hero_family`,
  `nisaa_hero_marriage` and `nisaa_hero_faith` are referenced from
  `ScreenBuilders.kt` and `RelationshipScreens.kt`. `docs/PREMIUM_UX_FLOWS.md`
  and `docs/BRAND_ASSET_SOURCES.md` are identical.

The one intentional difference is the four `nisaa_hero_*.xml` vector assets.
`premium-graphical-ux` still carries the original `<circle>` elements without
`cx`/`cy`/`r`, which AAPT2 rejects. The working branch has the corrected
`<path>` arc equivalents, so the working branch is strictly better here.

## No separate "responsive" repository exists

The GitHub account `ZamCan` holds `NISAA-Care`, `Madrassa-`,
`AndroidBuildTest-`, `AsphaltIdleRacer`, `BrainBounceSuite`, `SpeedEmpire`,
`UniShelf-App` and `ZamCan-Software-Chemistry-`. None of them is a
responsive-layout reference project, and `SpeedEmpire` / `AsphaltIdleRacer`
are Unity projects whose AdMob work is not reusable Android code.

The actual responsive gap is inside this repository:
`docs/PREMIUM_NATIVE_UI_SYSTEM.md` requires that the app not be locked to
portrait and that content reflow on wider windows, but
`NisaaDesign.MAX_CONTENT_WIDTH_DP` was declared and never applied. That gap
is closed in the "Responsive layout" commit on the working branch.
