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

## Layout

- Minimum interactive target: 48dp.
- Comfortable content width on larger screens: approximately 720dp.
- Prefer generous vertical rhythm and short information groups.
- Use cards for grouped information, not every individual label.
- Primary actions use one strong visual treatment per screen.
- Empty, loading, error, offline, pending-review, and success states are first
class UI states.

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
