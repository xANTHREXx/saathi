# Saathi

A trip companion for women commuting in India. It watches the trip on its own, checks in loudly when
something goes wrong, texts the right people from her own SIM (even with no data), takes help from the
power button, the lock screen and a home-screen widget, speaks for her when she can't, talks her through
an uneasy moment, and leaves a sealed record with reporting guidance. Built for the Google Cloud AI
Builder Cup 2026.

```
android/   Kotlin + Jetpack Compose app (minSdk 29)
server/    Node 20 + Express backend for Cloud Run
tools/     demo track generator
```

## What runs where

| On the phone, no AI, works offline | On the server (Cloud Run) |
|---|---|
| Watcher: long stop, off route, late arrival | Gemini: reads ride notifications and plate photos |
| Check-in: loud alarm, full screen over the lock screen, 10 s | Gemini: companion replies |
| Alert SMS from her SIM to contacts in her current city | Gemini + Google Search: reporting guide and draft |
| Power-button presses, lock-screen notification, widget, hold | Seal (HMAC) over evidence fingerprints |
| Speak for me in the local language, Hindi, English | Live-location page for contacts |
| Ride pickup from Uber, Ola, Rapido notifications (regex) | Community points, heatmap, vehicle flags |

Whether someone is alerted never depends on a model. Without Gemini the server still answers: plates
from a regex, a fixed companion reply that raises the alert only on words like "help" or "bachao", and a
static reporting guide on the phone.

## Run the backend

```bash
cd server
npm install
npm test            # 16 tests
GEMINI_API_KEY=... SAATHI_SECRET=... node index.js   # http://localhost:8080/health
```

Env: `GEMINI_API_KEY` (AI Studio), `GEMINI_MODEL` (optional, tried first), `MAPS_API_KEY` (optional:
Routes, Places, Text-to-Speech), `SAATHI_SECRET` (seal key; set it so seals survive restarts),
`SEED_DEMO=false` to turn off sample community data, `PUBLIC_URL` (base for live links).

Deploy from Google Cloud Shell:

```bash
gcloud run deploy saathi --source server --region asia-south1 --allow-unauthenticated \
  --max-instances 1 --set-env-vars GEMINI_API_KEY=...,SAATHI_SECRET=...
```

`--max-instances 1` matters: live links and community data are kept in memory.

## Run the app

Needs JDK 17 and the Android SDK. `android/local.properties` (not committed):

```
sdk.dir=/path/to/Android/Sdk
BASE_URL=https://<your-cloud-run-url>
MAPS_API_KEY=
```

```bash
cd android
./gradlew :app:testDebugUnitTest   # 43 tests
./gradlew :app:assembleDebug       # app/build/outputs/apk/debug/app-debug.apk
```

Without `BASE_URL` the app talks to `http://10.0.2.2:8080`, the host machine from the emulator.

### Demo

Long-press the "Saathi" title on the home screen for the demo panel (sample data):

- **Replay: Bengaluru Uber auto, 30×**: the auto stops 12 minutes at a fuel station; the check-in fires
  after 5 minutes of track time, and with no answer the SMS goes out.
- **Fake ride-app notifications**: Booked, Arrived (asks for the plate), Flagged auto (KA05XY9876 has
  sample reports), Ended (asks "Anything felt off?").
- **Pretend I'm in (city)**: Bengaluru or Pune changes who is alerted and the speak-for-me language.

Grant notification access for the ride watcher: Settings › Notifications › Device & app notifications,
or `adb shell cmd notification allow_listener app.saathi/app.saathi.trigger.RideNotificationListener`.

## Verified so far

On the Android 16 emulator: replay stop → check-in over the lock screen → SMS to Riya (Bengaluru) and
Amma (anywhere), not Aditi (Pune); hold-for-help alert with a live link that updates; Uber notification
starts a watched trip with no action; flagged-vehicle warning; companion raises the alert on "help";
community map with sample heatmap.

Not yet verified: the power-button trigger (the emulator merges quick presses; test on a real phone), the
widget on a home screen, the plate-check screen, SMS delivery to a real phone, and anything on a Cloud Run
URL.

## Limits

- Speak-for-me translations are drafts and need a native speaker's check.
- The reporting guide and companion advice are generated and not reviewed by a qualified person.
- Community data in the demo is synthetic and labelled "Sample data".
- The emergency step is simulated.
