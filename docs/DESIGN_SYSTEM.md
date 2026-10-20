# NISAA CARE design system

## Product character

NISAA CARE uses a calm, premium editorial visual language: warm cream surfaces,
walnut/ink typography, muted rose and terracotta for primary actions, sage for
positive states, and restrained antique gold for emphasis.

The interface should feel like a trusted private care journal rather than a
generic medical dashboard or an AI product.

## Iconography

Icons are Android vector drawables so they scale cleanly across densities and
devices. The icon family uses simple geometric silhouettes with a restrained
two-tone treatment. Important actions must remain understandable without
colour alone and must have text/accessibility descriptions.

Core semantic icons now include:

- cycle/calendar
- fertility
- health
- marriage/relationship
- education/book
- privacy/security
- language
- notifications
- settings
- sharing
- navigation/check/add

Do not replace these with emoji, platform-dependent glyphs, or bitmap artwork.

## Glass surface system

NISAA CARE renders with platform views only, so the glass effect is built from
real primitives rather than a library. `app/src/main/java/com/zamcan/nisaacare/ui/Glass.kt`
holds the implementation.

A glass surface has three layers:

1. **Translucent fill.** Genuinely alpha-blended, so the aurora backdrop shows
   through. This is what makes it read as glass rather than as a pale card.
2. **Gradient hairline.** The stroke is brighter along the top edge and darker
   along the base, which is what a bevelled edge does when lit from above.
   `GradientDrawable` cannot express a gradient stroke, hence the custom
   drawable.
3. **Specular sheen.** A short bright band hugging the inside of the top edge.

### Depth levels

Depth is expressed by stacking, not by elevation. Translucent fills do not
receive real shadows, and a drop shadow on glass reads as a grey halo.

| Level | Fill alpha | Use |
|---|---|---|
| `ONE` | 62% | content cards, rows, chips, fields |
| `TWO` | 74% | panels that group other panels, banners, pills |
| `THREE` | 84% | selected states, overlays, sheets |

Each step is roughly 12%, so three nested surfaces remain distinguishable
instead of collapsing into one another.

### The backdrop

`AuroraBackdropView` paints four large radial blobs plus two defined light
streaks. The blobs are deliberately off-centre so a card never sits on a flat
area of colour; the variation between them is what gives each glass panel
something different to refract.

On API 31+ the streak layer carries a real `RenderEffect` blur, so the frosted
quality is genuine rather than painted. Below API 31 the streaks are drawn
wider and dimmer to approximate it. The blob layer is never blurred, because
flattening the colour variation would remove the depth cue the glass relies on.

### Dark theme

`values-night/glass_colors.xml` inverts the recipe rather than reusing it.
White-at-62% over a dark backdrop raises the black level and kills contrast,
so night uses dark translucent ink instead of white, and the hairlines flip so
the top edge is still the bright one. The same "lit from above" read survives
in both themes.

### What stays opaque

Not everything becomes glass. Legibility wins:

- **Primary buttons** stay opaque rose. A saturated call to action must not
  compete with the glass around it, and translucent text on glass is not
  accessible.
- **Text fields** use the lowest glass level with the sheen off. Body text on a
  62% translucent fill over a moving backdrop is genuinely harder to read.

## Layout

- Minimum interactive target: 48dp.
- Comfortable content width on larger screens: 720dp.
- Prefer generous vertical rhythm and short information groups.
- Use cards for grouped information, not every individual label.
- Primary actions use one strong visual treatment per screen.
- Empty, loading, error, offline, pending-review, and success states are first
class UI states.

## Adaptive layout

The app is not locked to portrait and does not stretch a phone layout on a
tablet. `AdaptiveLayout` classifies the window and `AdaptiveNavigationShell`
responds to it.

| Window width | Class | Navigation | Content |
|---|---|---|---|
| under 600dp | `COMPACT` | floating glass bottom bar | single column |
| 600dp to 839dp | `MEDIUM` | floating glass bottom bar | single column, wider gutter |
| 840dp and up | `EXPANDED` | glass side rail | centred column capped at 720dp |

Classification uses the width of the window, not the physical display. That
distinction matters: in split-screen or on a foldable the window can be narrow
even though the device is large, and a layout chosen from the display size
would overflow the window it is actually drawn in.

The destinations and their order are identical in both presentations. Only the
placement changes, so muscle memory carries across form factors.

## Icon treatment

`GlassIcons` places each glyph inside a translucent tinted well, so an icon has
its own depth cue instead of floating on the glass. Small glyphs receive a
slightly stronger ink, because they otherwise dissolve against a pale panel.

## Colour roles

- Cream: primary background.
- Surface white: cards and readable content areas.
- Ink/walnut: primary text.
- Soft ink: secondary text.
- Rose: primary action and women-focused accent.
- Terracotta: health/attention accent.
- Sage: positive/confirmed state.
- Gold: reflection, emphasis, and premium accent.
- Lilac/soft rose: quiet secondary surfaces.

Never use colour as the only indicator of health or relationship status.

## Typography

Use a strong serif display hierarchy for editorial headings and a clean sans
family for controls, labels, body copy, and data. Arabic must use an
appropriate system Arabic fallback and preserve RTL semantics rather than
simulating RTL through manual string reversal.

## Privacy presentation

Health and relationship information should not appear in lock-screen
notifications by default. Sensitive content is disclosed only inside the
appropriate authenticated/relationship-scoped surface.

## Content presentation

Medical, Qur'an, hadith, and fiqh content must show provenance and review state
when relevant. Pending content is visibly marked as awaiting review; it is not
presented as authoritative.

## Engineering rule

Visual polish must never bypass domain policies. Permissions, relationship
isolation, review states, uncertainty, and privacy rules remain enforced below
the UI layer.
