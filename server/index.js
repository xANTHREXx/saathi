import express from "express";
import { readTrip, companionTurn, guide, configured } from "./ai.js";
import { seal } from "./seal.js";
import { Live, livePage } from "./live.js";
import { Community } from "./community.js";

const MAPS = process.env.MAPS_API_KEY;
const app = express();
app.use(express.json({ limit: "12mb" }));

const live = new Live();
const community = new Community({ sample: process.env.SEED_DEMO !== "false" });
const device = (req) => String(req.get("x-saathi-device") || req.ip);
const base = (req) => process.env.PUBLIC_URL || `${req.protocol}://${req.get("host")}`;

app.get("/health", (_req, res) => res.json({ ok: true, gemini: configured(), maps: Boolean(MAPS), sample: community.isSample }));

// --- AI
app.post("/trip/read", async (req, res) => {
  const { text, image_b64, mime } = req.body || {};
  res.json(await readTrip({ text, image: image_b64 ? Buffer.from(image_b64, "base64") : null, mime }));
});
app.post("/companion/turn", async (req, res) => res.json(await companionTurn(req.body || {}, undefined, nearbyOpen)));
app.post("/guide", async (req, res) => {
  try { res.json(await guide(req.body || {})); }
  catch (e) { res.status(503).json({ error: "The reporting guide is unavailable right now. Try again in a minute." }); }
});

// --- record seal: fingerprints only, never the files
app.post("/seal", (req, res) => res.json({ seal: seal(req.body || {}) }));

// --- live location
app.post("/live", (req, res) => {
  const { token } = live.create(req.body || {});
  res.json({ token, url: `${base(req)}/l/${token}` });
});
app.post("/live/:token", (req, res) => (live.update(req.params.token, req.body || {}) ? res.json({ ok: true }) : res.status(410).end()));
app.get("/live/:token/state", (req, res) => { const s = live.state(req.params.token); s ? res.json(s) : res.status(410).end(); });
app.get("/l/:token", (req, res) => res.type("html").send(livePage(req.params.token.replace(/[^0-9a-f]/g, ""))));

// --- community
app.post("/community/point", (req, res) => { community.add(req.body || {}, device(req)); res.json({ ok: true }); });
app.get("/community/points", (_req, res) => res.json({ sample: community.isSample, points: community.points() }));
app.get("/community/vehicle", (req, res) => res.json(community.vehicle(String(req.query.plate || ""))));

// --- maps
app.post("/route", async (req, res) => {
  const { from, to_text, mode } = req.body || {};
  if (!MAPS || !from || !to_text) return res.json({ points: [], duration_s: null });
  try {
    const r = await fetch("https://routes.googleapis.com/directions/v2:computeRoutes", {
      method: "POST",
      headers: { "Content-Type": "application/json", "X-Goog-Api-Key": MAPS, "X-Goog-FieldMask": "routes.duration,routes.polyline.encodedPolyline" },
      body: JSON.stringify({
        origin: { location: { latLng: { latitude: from.lat, longitude: from.lng } } },
        destination: { address: to_text },
        travelMode: { WALK: "WALK", BUS: "TRANSIT", TRAIN: "TRANSIT", CAB: "DRIVE" }[mode] || "TWO_WHEELER",
      }),
    }).then((x) => x.json());
    const route = r.routes?.[0];
    res.json({ points: route ? decode(route.polyline.encodedPolyline) : [], duration_s: route ? parseInt(route.duration) : null });
  } catch { res.json({ points: [], duration_s: null }); }
});

async function nearbyOpen(lat, lng) {
  if (!MAPS) return [];
  try {
    const r = await fetch("https://places.googleapis.com/v1/places:searchNearby", {
      method: "POST",
      headers: { "Content-Type": "application/json", "X-Goog-Api-Key": MAPS,
        "X-Goog-FieldMask": "places.displayName,places.location,places.currentOpeningHours.openNow" },
      body: JSON.stringify({ includedTypes: ["pharmacy", "convenience_store", "cafe", "police", "hospital", "restaurant"],
        maxResultCount: 8, rankPreference: "DISTANCE", locationRestriction: { circle: { center: { latitude: lat, longitude: lng }, radius: 500 } } }),
    }).then((x) => x.json());
    return (r.places || []).filter((p) => p.currentOpeningHours?.openNow !== false).slice(0, 3).map((p) => ({
      name: p.displayName?.text, distance_m: Math.round(dist(lat, lng, p.location.latitude, p.location.longitude)) }));
  } catch { return []; }
}

const ttsCache = new Map();
app.get("/tts", async (req, res) => {
  const text = String(req.query.text || "").slice(0, 400), lang = String(req.query.lang || "en-IN");
  if (!MAPS || !text) return res.status(404).end();
  const key = `${lang}|${text}`;
  try {
    if (!ttsCache.has(key)) {
      const r = await fetch(`https://texttospeech.googleapis.com/v1/text:synthesize?key=${MAPS}`, {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ input: { text }, voice: { languageCode: lang, ssmlGender: "FEMALE" }, audioConfig: { audioEncoding: "MP3" } }),
      }).then((x) => x.json());
      if (!r.audioContent) return res.status(404).end();
      ttsCache.set(key, Buffer.from(r.audioContent, "base64"));
    }
    res.type("audio/mpeg").send(ttsCache.get(key));
  } catch { res.status(404).end(); }
});

function dist(a, b, c, d) {
  const R = 6371000, r = Math.PI / 180, x = (d - b) * r * Math.cos(((a + c) / 2) * r), y = (c - a) * r;
  return Math.sqrt(x * x + y * y) * R;
}
function decode(s) {
  const pts = []; let i = 0, lat = 0, lng = 0;
  while (i < s.length) {
    for (const k of [0, 1]) {
      let b, shift = 0, result = 0;
      do { b = s.charCodeAt(i++) - 63; result |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
      const v = result & 1 ? ~(result >> 1) : result >> 1;
      k === 0 ? (lat += v) : (lng += v);
    }
    pts.push([lat / 1e5, lng / 1e5]);
  }
  return pts;
}

const port = process.env.PORT || 8080;
app.listen(port, () => console.log(`Saathi server on ${port}`));
