# NISAA CARE Premium Native UI System

## Design correction

NISAA CARE is a native Android application. Its visual language should not resemble a responsive marketing website.

The UI uses Android Views and Android-native interaction behavior while retaining the NISAA CARE editorial identity.

## Native hierarchy

Each screen should read as:

Top app bar
-> screen title/context
-> one primary task
-> compact native rows or focused surfaces
-> supporting detail
-> secondary actions

Avoid stacking many large marketing cards one after another.

## Surface language

- 14–16dp corner radius for normal cards and controls.
- Small elevation for interactive surfaces.
- Fine 1dp borders for separation.
- Tonal surfaces instead of heavy gradients.
- One strong primary action per screen.
- 48dp minimum touch target.
- Compact icon-leading rows for settings, knowledge categories and relationship actions.
- Bottom sheets/dialogs for short focused decisions where appropriate.
- Full-screen content for reading-heavy knowledge articles.
- Hero artwork only where it establishes context; it must not dominate task screens.

## Button language

Primary:
- filled rose action
- short verb
- optional leading vector icon

Secondary:
- tonal rose action

Tertiary:
- outline/text action

Do not stretch every action into a giant website-style CTA.

## Knowledge presentation

Health and Islamic knowledge should use a library pattern:

Category -> topic list -> article/detail -> source/provenance -> related topics.

A source badge or compact provenance row should sit close to the content it supports.

## Cycle presentation

Cycle home:
- current phase/status
- cycle day
- next expected event
- confidence/data quality
- primary log action

History:
- calendar/list
- selected record
- symptoms/flow
- calculation explanation

Fertility:
- estimate
- confidence
- explanation
- limitation
- education

## Relationship presentation

Connection:
- pairing method rows
- code display
- QR display/scan
- WhatsApp handoff
- expiry
- identity verification
- permissions

The user should never see health information before permissions are explicitly granted.

## Adaptive behavior

The app must not be locked to portrait. On wider windows, content should reflow into larger layouts rather than simply stretching a phone layout. Android's current adaptive guidance recommends window-size-aware layouts and different navigation patterns for larger windows.

## Accessibility

- semantic content descriptions
- sufficient contrast
- scalable text
- minimum touch targets
- no meaning conveyed by color alone
- RTL verification for Arabic
- focus order checked for keyboard/external input

## Enterprise-readiness rule

A premium appearance is not achieved by adding decoration everywhere.

The enterprise signal comes from:
- predictable interaction
- strong information hierarchy
- trustworthy provenance
- explicit privacy boundaries
- stable states
- consistent components
- adaptive behavior
- tested error paths
- versioned domain logic
- controlled content updates
