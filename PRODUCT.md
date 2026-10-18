# Product

<!-- impeccable:product-schema 1 -->

Source of truth: `../../2026-10-18-saathi-prd.md` (v1.2, confirmed by the user 2026-10-18). This file
holds the durable product record; the PRD holds requirement IDs and acceptance criteria.

## Platform

android

Native Android only (Kotlin, Jetpack Compose, Glance widget), Android 10+ (minSdk 29). No web app,
no iPhone: iOS cannot read other apps' notifications or the power button. The only web surface is
the live-location page contacts open in a browser after an alert.

## Users

**Primary: the commuter.** A woman in India, 18–35, with an Android phone, travelling alone by auto,
cab, bus, train or on foot, often after dark, sometimes in a city she moved to for work or study.
She already shares her location by hand, carries pepper spray or a steel bottle, and calls family
when uneasy. In the moment that matters she may be frozen, unable to speak, unable to unlock her
phone, or offline.

**Secondary: her contacts.** Family and friends in her current city or back home. They never install
anything; they receive SMS and open a live-location link in any browser.

**Indirect: other women.** They benefit from the heatmap and vehicle flags that pooled reports build.

Not users in this version: police, helplines, NGOs, ride platforms.

Demo persona (fictional): Priya, 26, moved from Pune to Bengaluru; metro then an Uber auto home
after a late shift.

## Product Purpose

Saathi keeps her company on a commute. It picks up Uber, Ola and Rapido rides from their
notifications without being asked, watches the trip, checks in loudly when something goes wrong,
and texts the right people from her own SIM, even with no data. Help is one gesture from the lock
screen. It speaks for her when she cannot, talks her through an uneasy moment like an elder sister
on the phone, and afterwards turns the trip into a sealed record with guidance on where and how to
report. Small reports from many women add up into a map of where trouble happens and flags on
vehicles reported again and again.

Success, measured as behaviour: most of her commutes are watched; ride-app trips start with no
action and others in under 10 seconds; few false alarms (watch for more than 2 per 10 trips); every
alert reaches at least one contact, offline included; small "nothing major" reports get filed; she
is still using it after 7 days.

## Positioning

Every safety app has a button, but a woman who freezes cannot press it. Saathi works when she does
nothing: trips start on their own, the watcher decides on rules, and silence after a check-in is
itself the trigger. It works offline over her own SIM, its contacts and language follow her when she
moves cities, and her small unreported incidents become shared evidence other women can use.

## Operating Context

- Used one-handed, in transit, at night, often with the phone in a bag or pocket and the screen off.
- The critical moments happen on the lock screen: the persistent notification, power-button presses,
  the full-screen check-in, the home-screen widget.
- Network is unreliable; SMS over her SIM is the guaranteed channel.
- The trip arc: ride notification or one-tap start → plate check on arrival → watched trip →
  check-in on a long stop, detour or late arrival → alert, speak-for-me, companion → sealed record →
  reporting guidance, draft, optional small report.
- Languages follow her location: the state's language, then Hindi, then English (Kannada, Marathi,
  Hindi, English in the demo).
- Hackathon context: Google Cloud AI Builder Cup 2026, theme Sustainability & Social Impact, judged
  40% technical merit and Gen AI, 25% problem alignment, 25% innovation, 10% UX. Backend on Cloud
  Run and Firebase; APK via Firebase App Distribution.

## Capabilities and Constraints

- **Rules, not AI, decide every alert.** The watcher, triggers, recipients, SMS text, offline alerts,
  live page, speak-for-me, seal, heatmap counts, area note and vehicle-flag threshold are
  deterministic and unit-tested. Gemini only reads ride notifications and plates, runs the companion,
  and grounds the reporting guide.
- **Sign-in never stands between her and help.** Safety works signed out; an expired session never
  blocks an alert.
- **The app never files anything.** Reporting is guidance and a draft she edits and copies.
- **Privacy:** audio and exact trip logs stay on the phone; the server keeps fingerprints (SHA-256)
  and a seal. Live location leaves the phone only while an alert is active. Community data is
  anonymous, rounded, consented, and deletable with the record.
- **Vehicle flags:** 3+ different accounts in 90 days, shown only as a count to a woman about to ride
  in that vehicle. Never a driver's name, never public, never shared with platforms or police.
- **Safety copy:** never "you're safe", never a promise of rescue, never a request to describe what
  happened. Legal content always cites sources and says "This is information, not legal advice."
- **Restricted permissions** (notification access, SMS, background location) mean a direct APK for
  the hackathon; a Play Store release needs a policy review.
- **Terminology:** trip, watcher, check-in, alert, Speak for me, companion, sealed record,
  fingerprint, seal, reporting guidance, community data, heatmap, vehicle flag, area note.
- **Undecided:** the product name (Saathi is the working name, not yet confirmed); whether the loud
  check-in provokes rather than deters (untested, must be tested with women before real use).

## Brand Commitments

- **Name:** "Saathi" is the working name; treat it as provisional until the user confirms it.
- **Voice:** plain words, short sentences, the user addressed and described as "her". The companion
  speaks like an elder sister on the phone: calm, one cautious next step at a time.
- Nothing else is binding yet.

## Evidence on Hand

- Two friendly interviews with women commuters (neither describes freezing). Notes in
  `../../women-safety-idea-insights.md` and `Projects/women-safety/`.
- Seven Reddit posts describing freezing on a commute, plus one second-hand bus account; Reddit and
  web research cited in the PRD.
- Demo assets: synthetic trip tracks (`android/app/src/main/assets/tracks/`), synthetic community
  data, fictional people, routes and vehicles; app screenshots in `.superpowers/`.
- **Absences future work must not fabricate:** no testimonials, no user counts, no real community
  data (anything shown carries a "Sample data" badge), no helpline, NGO or legal reviewer, no
  partnerships with ride platforms or police. The emergency step in the demo is labelled
  "simulated".

## Product Principles

1. **Works when she does nothing.** The default path needs no action from her; any action asked of
   her is one gesture, reachable from the lock screen.
2. **Help before everything.** Nothing — sign-in, setup, a model call, a network request — sits
   between her and an alert. The SOS stays the easiest thing on any surface it appears on.
3. **Rules decide, AI assists.** Whether someone is told, or a vehicle is flagged, never depends on a
   model.
4. **Her decision, her data.** Saathi guides and drafts but never files, never names, never
   publishes; she can delete any record and its community contribution.
5. **Honest about what is real.** Synthetic, simulated and untested parts are labelled as such, in
   the demo now and in testing with real women after.

## Accessibility & Inclusion

- Large controls that work one-handed, in the dark, under stress.
- The check-in uses sound, vibration and the screen together, at alarm volume even on silent.
- Every trigger confirms with a short vibration without lighting the screen.
- Indian languages only in this version; copy must work in Kannada, Marathi, Hindi and English, with
  the state table extending to Tamil, Telugu, Malayalam, Bengali and Gujarati.
