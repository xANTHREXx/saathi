---
name: Saathi
description: A night-commute companion for Android. Her trip is drawn as a thread of stamped ajrakh stars on indigo cloth.
colors:
  night-indigo-ground: "#0D1222"
  night-surface-1: "#141A2E"
  night-surface-2: "#1B2340"
  night-surface-3: "#252F52"
  night-ink: "#EEF0F7"
  night-ink-2: "#AAB2CC"
  night-ink-3: "#6F7899"
  night-indigo-brand: "#8FA2E8"
  night-indigo-container: "#25336B"
  night-on-indigo-container: "#DCE2FF"
  night-madder: "#C7372F"
  night-madder-container: "#5A1714"
  night-on-madder-container: "#FFDAD5"
  night-alum: "#E7B94A"
  night-ok: "#7BD3A4"
  night-hair: "#FFFFFF14"
  night-star-off: "#FFFFFF24"
  day-resist-ground: "#F5F6FA"
  day-surface-1: "#FFFFFF"
  day-surface-2: "#EEF0F7"
  day-surface-3: "#E2E6F2"
  day-ink: "#11162A"
  day-ink-2: "#4D5675"
  day-ink-3: "#8790AD"
  day-indigo-brand: "#2E3F86"
  day-indigo-container: "#DCE2FF"
  day-on-indigo-container: "#18245A"
  day-madder: "#B42A23"
  day-madder-container: "#FFDAD5"
  day-on-madder-container: "#5A1714"
  day-alum: "#8A5F00"
  day-ok: "#1E7A4C"
  day-hair: "#0D12221A"
  day-star-off: "#2E3F8626"
  plate-white: "#FAFAF7"
  plate-black: "#121212"
typography:
  display-large:
    fontFamily: "Figtree, Noto Sans (system fallback for Indic scripts)"
    fontSize: "46sp"
    fontWeight: 600
    lineHeight: "48sp"
    letterSpacing: "-0.03em"
  display-small:
    fontFamily: "Figtree"
    fontSize: "34sp"
    fontWeight: 600
    lineHeight: "38sp"
    letterSpacing: "-0.025em"
  headline-medium:
    fontFamily: "Figtree"
    fontSize: "28sp"
    fontWeight: 600
    lineHeight: "32sp"
    letterSpacing: "-0.02em"
  headline-small:
    fontFamily: "Figtree"
    fontSize: "24sp"
    fontWeight: 600
    lineHeight: "28sp"
    letterSpacing: "-0.015em"
  title-large:
    fontFamily: "Figtree"
    fontSize: "22sp"
    fontWeight: 600
    lineHeight: "28sp"
    letterSpacing: "-0.015em"
  title-medium:
    fontFamily: "Figtree"
    fontSize: "17sp"
    fontWeight: 600
    lineHeight: "22sp"
    letterSpacing: "-0.005em"
  title-small:
    fontFamily: "Figtree"
    fontSize: "15sp"
    fontWeight: 600
    lineHeight: "20sp"
  body-large:
    fontFamily: "Figtree"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "23sp"
  body-medium:
    fontFamily: "Figtree"
    fontSize: "15sp"
    fontWeight: 400
    lineHeight: "21sp"
  body-small:
    fontFamily: "Figtree"
    fontSize: "13sp"
    fontWeight: 400
    lineHeight: "18sp"
  label-large:
    fontFamily: "Figtree"
    fontSize: "15sp"
    fontWeight: 600
    lineHeight: "20sp"
  label-medium:
    fontFamily: "Figtree"
    fontSize: "13sp"
    fontWeight: 500
    lineHeight: "18sp"
  label-small:
    fontFamily: "Figtree"
    fontSize: "12sp"
    fontWeight: 500
    lineHeight: "16sp"
  plate:
    fontFamily: "Figtree"
    fontSize: "13sp"
    fontWeight: 700
    letterSpacing: "0.06em"
rounded:
  extra-small: "8dp"
  small: "12dp"
  chip: "18dp"
  medium: "20dp"
  large: "28dp"
  plate: "32dp"
spacing:
  hair: "1dp"
  xs: "4dp"
  sm: "8dp"
  md: "10dp"
  sheet-gap: "14dp"
  gutter: "16dp"
  card: "18dp"
components:
  help-plate:
    backgroundColor: "{colors.night-madder}"
    textColor: "{colors.night-ink}"
    typography: "{typography.headline-medium}"
    rounded: "{rounded.plate}"
    padding: "18dp"
    height: "140dp min"
  help-plate-quiet:
    backgroundColor: "{colors.night-madder-container}"
    textColor: "{colors.night-on-madder-container}"
    typography: "{typography.title-large}"
    rounded: "{rounded.plate}"
    padding: "18dp"
  okay-plate:
    backgroundColor: "{colors.night-ink}"
    textColor: "{colors.night-indigo-ground}"
    typography: "{typography.headline-medium}"
    rounded: "{rounded.plate}"
    height: "88dp"
  okay-plate-checkin:
    backgroundColor: "{colors.night-ink}"
    textColor: "{colors.night-indigo-ground}"
    typography: "{typography.headline-medium}"
    rounded: "{rounded.plate}"
    height: "108dp"
  tile:
    backgroundColor: "{colors.night-surface-2}"
    textColor: "{colors.night-ink}"
    typography: "{typography.title-small}"
    rounded: "{rounded.medium}"
    padding: "0 14dp"
  mode-chip:
    backgroundColor: "{colors.night-surface-2}"
    textColor: "{colors.night-ink}"
    typography: "{typography.label-medium}"
    rounded: "{rounded.medium}"
    height: "72dp"
  mode-chip-selected:
    backgroundColor: "{colors.night-indigo-container}"
    textColor: "{colors.night-on-indigo-container}"
  map-chip:
    backgroundColor: "{colors.night-surface-1}"
    textColor: "{colors.night-ink}"
    typography: "{typography.label-medium}"
    rounded: "{rounded.chip}"
    padding: "0 10dp"
    height: "36dp"
  told-banner:
    backgroundColor: "{colors.night-madder-container}"
    textColor: "{colors.night-on-madder-container}"
    typography: "{typography.title-small}"
    rounded: "{rounded.medium}"
    padding: "14dp"
  trip-card:
    backgroundColor: "{colors.night-surface-1}"
    textColor: "{colors.night-ink}"
    rounded: "{rounded.large}"
    padding: "18dp"
  trip-sheet:
    backgroundColor: "{colors.night-indigo-ground}"
    rounded: "{rounded.large}"
  plate-text:
    backgroundColor: "{colors.plate-white}"
    textColor: "{colors.plate-black}"
    typography: "{typography.plate}"
    rounded: "6dp"
    padding: "1dp 8dp"
  saathi-mark:
    backgroundColor: "{colors.night-indigo-container}"
    textColor: "#FFFFFF"
    size: "24dp"
---

# Design System: Saathi

## Overview

**Creative North Star: "Indigo Cloth, Stamped Thread"**

Saathi is dyed like Ajrakh cloth. Night is indigo ground and day is resist white. Madder red is the costly dye, so it goes only on help and alerts. Alum gold is the mordant, the attention colour that shows before anything is sent. The one motif is the eight-point ajrakh star, two squares with one turned 45 degrees. It appears as the app mark, and it is also the trip thread: one star is stamped onto her route for each minute Saathi watches. When the trip stalls, the newest star turns alum and the stamping stops.

Screens follow a map-and-sheet trip layout like Pixel and Maps. A calm, luminance-only map sits at the top, and a 28dp sheet carries the line diagram. Help is pinned below the scroll so it never leaves her thumb. Density is low and controls are large: every target is at least 48dp and the help row is at least 140dp. Each screen has exactly one full-contrast "reversed plate", the thing to press. Depth comes from tonal steps and hairlines, never from shadows.

Native Android: Jetpack Compose with Material 3 roles. Every colour above maps onto an M3 slot or onto the `SaathiColors` extension in `ui/theme/SaathiTheme.kt`, which is the normative source.

**Key Characteristics:**
- Indigo night ground with three tonal surface steps, and a resist-white day ground with white surfaces.
- Madder reserved for help and alerts. Alum for stalls, check-ins and pre-send attention.
- Eight-point ajrakh star as both the mark and the trip thread, stamped one per watched minute.
- One reversed plate per screen: Hold for help normally, I'm okay during an alert or check-in.
- Figtree in one family at weights 400 to 700, with system Noto for Kannada and Devanagari.
- Flat and tonal throughout, with 1dp hairlines instead of boxes or shadows.

## Colors

The palette is three dyes on two grounds: indigo, madder and alum on night indigo or resist white. Every role is defined for both night and day.

### Primary
- **Indigo Brand Ink** (night-indigo-brand / day-indigo-brand): M3 `primary`. Used for icons on tiles, her own station on the line diagram, her position and live route on the map, and the speaking-language highlight. It is the ink of "being watched over".
- **Indigo Container** (night-indigo-container / day-indigo-container): `primaryContainer`. Fills the in-app mark tile and the selected mode chip.

### Secondary
- **Alum Gold** (night-alum / day-alum): M3 `secondary`, plus `SaathiColors.alum`. Marks attention before anything is sent: stall stations on the line diagram, the newest star when the trip stalls, the check-in countdown ring, and the reason pill. The day value is darkened to a bronze so it stays legible on white.

### Tertiary
- **Madder Red** (night-madder / day-madder): mapped to M3 `error` and to `SaathiColors.help`. Used for the Hold for help plate, the "Something happened" and "Don't get in" actions, the told-banner's check disc, the plate mismatch ring, and alert lines in trip history.
- **Madder Container** (night-madder-container / day-madder-container): `errorContainer` and `helpContainer`. Used for the told banner and for the quiet help plate during an alert.

### Neutral
- **Indigo Ground / Resist Ground** (night-indigo-ground / day-resist-ground): `background` and `surface`. Used for the sheet, full screens and the pinned help row.
- **Surface 1 to 3** (night-surface-1..3 / day-surface-1..3): `surfaceContainerLow`, `surfaceContainerHigh` and `surfaceContainerHighest`. Low is for chips, cards and the navigation bar. High is for tiles, mode chips and the map placeholder. Highest is for the countdown track.
- **Ink / Ink 2** (night-ink, night-ink-2 / day pair): primary and secondary text (`onBackground`, `onSurfaceVariant`, `SaathiColors.fg2`).
- **Ink 3** (night-ink-3 / day-ink-3): `outline` and `SaathiColors.fg3`. Used only for marks: the sheet handle (at 50%), the start station ring, the walked route on the map, and small fact icons.
- **Hair** (night-hair / day-hair): 1dp edges on chips, cards and dividers, and the rule above the pinned help row.
- **Star Off** (night-star-off / day-star-off): the rail under the stars and the faint "thread runs further back" stars.
- **Ok Green** (night-ok / day-ok): the "watching" status dot, setup checks that are done, and the plate-match disc. It is never used as a fill for a control.
- **Plate White / Plate Black**: used only inside `PlateText`, which imitates a real Indian number plate in both themes.

### Named Rules
**The Madder Rule.** Madder red is spent only on help and alerts: the help plate, alert confirmations, a plate mismatch, and the "something happened / don't get in" escalations. Nothing decorative is red, and nothing turns red before an alert is actually sent.

**The Alum-Before-Send Rule.** Attention before anything is sent is alum: stalls, the check-in pill, and the draining countdown ring. Alum turns to madder only once contacts have been told.

**The Ink-3-Is-Not-Text Rule.** fg3 colours marks and never text. Secondary text uses fg2.

## Typography

**Display Font:** Figtree, a variable font with weights 400, 500, 600 and 700 instantiated through `FontVariation`. It falls back to Android's system Noto for Kannada, Devanagari and other Indic scripts.
**Body Font:** Figtree.
**Label/Mono Font:** Figtree for labels. The platform monospace face is used only for SHA-256 fingerprint strings in trip records.

Font provenance: `res/font/figtree.ttf` is the Figtree variable font from the Google Fonts repository (github.com/google/fonts, `ofl/figtree`), licensed under the SIL Open Font License 1.1. No raster or other font asset ships with the app.

**Character:** Figtree is a friendly geometric grotesque, set tight at large sizes and heavy at semibold. It reads as calm and plain-spoken, like the elder-sister voice, rather than technical.

### Hierarchy
- **Display** (600; 46/48sp at -0.03em, 40/44sp, and 34/38sp at -0.025em): screen statements such as "Are you okay?", "Set up Saathi", "My trips", and "Your contacts were told". The check-in countdown number overrides this to 72/76sp.
- **Headline** (600; 30/34sp, 28/32sp and 24/28sp at -0.02em to -0.015em): reversed-plate labels ("Hold for help", "I'm okay"), plate-check verdicts, and the first spoken phrase.
- **Title** (600; 22/28sp, 17/22sp and 15/20sp): sheet heads, the minutes-watched figure, station names, tile labels, and the quiet help-plate label.
- **Body** (400; 16/23sp, 15/21sp and 13/18sp): explanations in fg2, and station subtitles in bodySmall.
- **Label** (600 or 500; 15/20sp, 13/18sp and 12/16sp): chips, the pill, nav labels, and "watched".
- **Plate** (700, 13sp or 22sp when big, +0.06em): the spaced capitals of a number plate. This is the only positive tracking in the system.

### Named Rules
**The Tight-At-Size Rule.** Negative tracking applies only from titleMedium up. Body and label styles stay at 0.

**The Grow-Don't-Clip Rule.** Layouts that hold text grow with the system font scale (`heightIn(min)` with `IntrinsicSize.Min`) instead of clipping. The help row was verified at 130% font scale.

## Layout

The trip tab stacks a 340dp map behind a scrolling column. The column holds a top chip row (status-bar padded, 16dp gutters, 8dp gaps), then a 236dp spacer, then the sheet. The sheet has 28dp top corners and a 36x4dp handle 10dp from the top, and stacks its sections 14dp apart with 16dp horizontal gutters. A 92%-opaque ground scrim sits behind the status bar so the clock stays readable when the sheet scrolls under it. Below the scroll region, a 1dp hair rule and the help row are pinned above the Material navigation bar (Trip, My trips, Map, Settings).

The help row splits 1.15 : 1. Hold for help sits on the left. Speak for me and Call Saathi stack on the right as two tiles. Gaps are 10dp, and the row is at least 140dp high and grows with font scale.

Full-screen surfaces (check-in, speak for me, plate check) use 14 to 16dp gutters. They put the explanation at the top, the decisive element in the optical middle, flanked by weighted spacers, and the reversed plate or primary action at the bottom within thumb reach. Cards pad at 18dp, banners at 14dp, and list gaps are 6 to 12dp.

Check-in always renders in night. Speak for me always renders in day, so it is a bright sheet a stranger can read. Every other surface follows the system theme.

### Named Rules
**The Pinned Help Rule.** Help never scrolls away. The help row lives outside the scroll container, under a hair rule, on every trip state.

**The One Plate Chip Rule.** On a trip the vehicle plate outranks the city. While a plate chip shows in the map chip row, the city chip is hidden.

## Elevation & Depth

Saathi is flat. There are no shadows or elevation tints anywhere. Depth comes from three tonal surface steps over the ground and from 1dp hair borders on chips, cards and the help-row rule. The map dissolves into the sheet through a vertical gradient: 55% ground at the top, clear between 18% and 70%, and full ground at the bottom. That gradient is the only soft transition in the system. The hold-to-help fill is an inner 22% black wash that grows left to right, not a lift.

### Named Rules
**The Tonal-Only Rule.** Separate layers by surface step or hairline. If something needs to read as "above", move it one surface step up rather than adding a shadow.

## Shapes

Corners are generously round and come from the M3 shape scale: extra-small 8dp, small 12dp, medium 20dp, large 28dp, extra-large 32dp. Chips are fully rounded at 18dp. Tiles, mode chips, banners and full-screen secondary buttons use 20dp. Cards and the sheet top use 28dp. Reversed plates use 32dp. Stations are circles with a 3dp ring, 14dp across, or 18dp for her own station. The ajrakh star is drawn as two squares with half-side equal to 0.72 of its radius, and placed stars get a small fixed tilt (-4 to +4 degrees) so a row looks hand-stamped. The in-app mark is a rounded indigo tile with a corner of 0.3 of its size, holding a white star at 0.6 of its size. Icons are Material Rounded.

## Components

### Buttons
- **Shape:** fully rounded M3 buttons by default. Full-screen actions use 20dp (`medium`). The plate-check primary uses 24dp and its mismatch escalation uses 28dp.
- **Primary:** M3 `Button` in indigo brand with ground-coloured text, 52 to 64dp tall on full screens.
- **Tonal:** `FilledTonalButton` (surface-3 container) for secondary actions such as End trip, Check plate, Close and It's fine. It is never used for help.
- **Escalation:** a madder container with white text, used only for "Something happened" and "Don't get in".

### Reversed Plate (signature)
There is exactly one full-contrast plate per screen.
- **Hold for help:** madder ground, white headlineMedium label, and a bodySmall line naming who gets texted. It has 32dp corners and 18dp padding with content anchored to the bottom. Holding for 2s (linear) fills the plate with a dark wash and raises the alert. Releasing early drains the fill over 250ms.
- **Quiet help (during an alert):** the plate steps back to madder container with an onHelpContainer "Hold to alert again" label in titleLarge, so that I'm okay owns the screen.
- **I'm okay:** an inverse-surface plate (light on night, dark on day) with a check icon and a headlineMedium label, 88dp tall on Home and 96 to 108dp on check-in. It sits directly under the told banner during an alert.

### Chips
- **Map chip:** 36dp tall with 18dp corners, surface 1 with a hair border and 6dp internal gaps. The variants are the mark plus "Saathi", the city with a NearMe icon, and a `PlateText`.
- **Mode chip:** 72dp tall with 20dp corners, surface 2, an indigo icon over a labelMedium label. When selected it switches to indigo container.
- **Pill:** the check-in reason, shown as an alum label on 16% alum with fully round corners.

### Cards / Containers
- **Corner Style:** 28dp (large). Tiles and banners use 20dp.
- **Background:** surface 1 for cards (trip card, felt-off, plate comparison) and surface 2 for tiles.
- **Shadow Strategy:** none (see Elevation & Depth).
- **Border:** 1dp hair on cards. Tiles have no border.
- **Internal Padding:** 18dp for cards, 14dp for banners and tiles.

### Inputs / Fields
- **Style:** stock M3 `OutlinedTextField` with the theme's outline and primary roles. Plate entry uppercases as she types.

### Navigation
- **Style:** M3 `NavigationBar` on surface 1 with Rounded icons and labelMedium labels. The active item shows the indigo-container indicator.

### Line Diagram (signature)
A vertical trip line. It shows a dim start station (ink-3 ring, fg2 title), then a 2dp star-off rail carrying 14dp stars, one per watched minute. The newest five are shown, with two faint off-stars when the thread runs further back. Next come alum-filled event stations, then her own 18dp indigo station. Each new star stamps in: it lands at 1.6x scale and 35% ink, then settles to 1x over 200ms and full ink over 260ms, with an emphasized-decelerate curve. When a stall begins, the newest star turns alum and only three stars show. **During an alert the history steps aside:** only the stop and her own station remain. With reduce-motion on, stars appear without the stamp.

### Trip Map
osmdroid tiles pass through a luminance-only colour matrix. At night it is inverted onto the indigo ramp (10,14,30 to 52,64,110). By day it maps onto a pale indigo ramp (190,196,214 to 250,251,253). Streets read as quiet structure. Her route draws as a 60%-alpha ink-3 hairline, with ajrakh stars stamped every 34px (in indigo while on a trip, in ink-3 afterwards). Her position is an indigo dot with a white ring inside a 20% indigo halo. With no fix yet, the map shows an 18%-opacity star on surface 2.

### Told Banner
A madder-container row with 20dp corners and 14dp padding. It holds a 36dp madder disc with a white check, a titleSmall "X and Y were told", and a bodySmall line at 80% stating what was sent and that the emergency step is simulated.

### Check-in Countdown
A 200dp ring with a 12dp stroke on surface 3, draining clockwise in alum from 12 o'clock with a round cap. Seconds sit in the centre at 72sp. The ring never turns red.

### Plate Text
A real-plate imitation: plate-white field, 1.5dp plate-black border, 6dp corners, bold spaced capitals formatted "KA 05 AB 1234". Big plates reserve a 2.5dp ring slot so plates compared in a column line up. A mismatch fills that ring with madder.

### Marks
- **In-app mark:** an indigo-container tile with a white ajrakh star, at 20 to 28dp.
- **Launcher icon:** an adaptive vector with a white ajrakh star on a madder (#C7372F) background and a monochrome layer for themed icons. The launcher keeps madder because the user picked the Ajrakh mark, which was madder. Inside the app the mark is indigo, so madder stays reserved for help.

### Home-screen Widget (Glance)
Night ground with 28dp corners. One madder Help tile (22dp corners, 30sp bold white "Help", madder-mist sub-line) opens Speak for me. "Slide" swaps to Walk, Bus and Train tiles on surface 3. Glance cannot load Figtree or theme roles, so the widget uses literal palette hexes and the system face.

## Do's and Don'ts

### Do:
- **Do** read every colour through `MaterialTheme.colorScheme` or `Saathi.colors`, so both night and day resolve.
- **Do** keep exactly one reversed plate per screen: Hold for help normally, I'm okay during an alert or check-in.
- **Do** step the help plate back to helpContainer with "Hold to alert again" while an alert is active.
- **Do** pin help outside the scroll, under a 1dp hair rule.
- **Do** stamp one ajrakh star per watched minute, and turn the newest one alum when the trip stalls.
- **Do** use alum for anything that asks for attention before a message is sent.
- **Do** let containers grow with font scale (`heightIn(min)` with intrinsic height) rather than clip names.
- **Do** use the indigo tile for the in-app mark, and keep the madder launcher icon.

### Don't:
- **Don't** use madder for anything other than help and alerts. That includes validation errors and decoration.
- **Don't** turn a countdown or stall red before an alert is actually sent.
- **Don't** set text in fg3. It colours marks only.
- **Don't** show the city chip while a plate chip is showing.
- **Don't** add shadows. Separate layers with a surface step or a hairline.
- **Don't** put a second full-contrast plate on a screen that already has one.
