---
version: 1
slug: "c-main-java-app-saathi-ui-mainactivity-kt-49fe2c7d"
primary_target: "android/app/src/main/java/app/saathi/ui/MainActivity.kt"
related_targets: ["android/app/src/main/java/app/saathi/ui/CheckInActivity.kt","android/app/src/main/java/app/saathi/ui/SpeakForMeActivity.kt","android/app/src/main/java/app/saathi/ui/PlateCheckActivity.kt","android/app/src/main/java/app/saathi/widget/SaathiWidget.kt"]
---

# Saathi app shell: trip, check-in, speak for me, plate check, widget

Scope: the demo-critical Android surfaces (Home/trip, Check-in, Speak for me, Plate check, home-screen widget) plus one shared Material 3 theme every other screen inherits. Mode: Operate.

Audience and job: a woman commuting alone at night in an Indian city, phone in hand or bag; she must know at a glance that she is being watched over, and reach help or "I'm okay" in one gesture. Constraints: Material 3 components and navigation, 48 dp targets, light and dark both designed, Kannada/Devanagari text must fit, red only for help and alerts, safety copy rules from PRODUCT.md.

Chosen direction: Ajrakh palette and mark carried inside a map-and-sheet trip layout (user blended the assigned Ajrakh Block with the Midnight Line challenger, refined to Pixel/Maps and Apple Health/Wallet polish). Memorable moment: the line of her trip is stamped in small ajrakh stars, one per watched minute, and it visibly stalls when something is wrong.

Unresolved: real map tiles at night depend on network; offline falls back to the indigo field with the line diagram only.

## Direction contract

THESIS: A trip companion that shows, not tells, that she is being watched over: her route drawn as a line of stamped ajrakh stars under a calm map. Refuses the category default of a white screen with a giant red SOS circle.

OWN-WORLD: Night indigo ground (#0D1222) with tonal surfaces stepping to #252F52; resist-white day ground (#F5F6FA) with white surfaces; indigo brand ink; madder red (#C7372F) spent only on help and alerts; alum gold for attention before anything is sent. Figtree with system Noto for Indic scripts; Material rounded icons; 28 dp sheets, 32 dp help plate, hairline edges instead of boxes. Signature: eight-point ajrakh star as the trip thread and app mark.

STORY: She sees the ride was picked up on its own, sees the stars accumulate along the line, and knows help is one hold away. When the line stalls, the check-in asks once, loudly; she answers or her contacts are told.

FIRST VIEWPORT: Home on a trip, dark. Top ~38%: map (osmdroid) with her position and route, two chips (city, plate). Bottom sheet, 28 dp top corners: "Watching your Uber auto", destination and ETA; vertical line diagram (pickup, her star, drop) whose watched segment is stamped stars; below, a madder "Hold for help" plate (left, larger) beside tonal Speak for me and Call Saathi tiles. Material navigation bar: Trip, My trips, Map, Settings. Primary action: Hold for help, the one reversed plate on screen.

FORM: Ajrakh Block fused with Midnight Line (assigned direction, candidate 6 of the grounded list, blended by the user with challenger wayfinding-cartography-signage-midnight-transit-diagram). seed:c7d817880efbc1c4

Signature interaction and motion: each watched minute stamps the next star with a short scale-and-settle (200 ms, emphasized decelerate); a stall turns the newest star alum and stops stamping; the check-in countdown ring drains in alum, never red until an alert is actually sent. Reduce-motion: stars appear without the stamp.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
