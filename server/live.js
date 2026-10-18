import { randomBytes } from "node:crypto";

const TTL_MS = 60 * 60 * 1000;

/** Live-location pages. In memory: Cloud Run runs one instance. */
export class Live {
  #shares = new Map();

  create({ name, plate }, now = Date.now()) {
    const token = randomBytes(16).toString("hex");
    this.#shares.set(token, { name: String(name || "She").slice(0, 40), plate: plate || null, last: null, events: [], touched: now });
    return { token };
  }

  update(token, { lat, lng, t, events }, now = Date.now()) {
    const s = this.#live(token, now);
    if (!s) return false;
    s.last = { lat: +lat, lng: +lng, t: +t };
    if (Array.isArray(events)) s.events = events.slice(-10).map(String);
    s.touched = now;
    return true;
  }

  state(token, now = Date.now()) {
    const s = this.#live(token, now);
    return s ? { name: s.name, plate: s.plate, last: s.last, events: s.events } : null;
  }

  #live(token, now) {
    const s = this.#shares.get(token);
    if (!s) return null;
    if (now - s.touched > TTL_MS) {
      this.#shares.delete(token);
      return null;
    }
    return s;
  }
}

export const livePage = (token) => `<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Saathi live location</title>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.css">
<style>
 body{margin:0;font:16px system-ui,sans-serif;background:#1a1d21;color:#ecedea}
 header{padding:14px 16px;display:grid;gap:4px} h1{margin:0;font-size:20px}
 #map{height:70vh} .muted{color:#a3a9af;font-size:14px} .ev{color:#f2b705}
</style></head><body>
<header><h1 id="who">Saathi alert</h1><div id="when" class="muted">Waiting for her location…</div><div id="ev" class="ev"></div>
<div class="muted">Call her. If you can't reach her and think she's in danger, call 112.</div></header>
<div id="map"></div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.js"></script>
<script>
const map = L.map('map').setView([20.6, 78.9], 4);
L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 19, attribution: '© OpenStreetMap'}).addTo(map);
let marker;
async function tick() {
  const r = await fetch('/live/${token}/state');
  if (r.status === 410) { document.getElementById('when').textContent = 'This link has expired.'; return; }
  const s = await r.json();
  document.getElementById('who').textContent = s.name + (s.plate ? ' · vehicle ' + s.plate : '');
  document.getElementById('ev').textContent = s.events.join(' · ');
  if (s.last) {
    const p = [s.last.lat, s.last.lng];
    marker ? marker.setLatLng(p) : (marker = L.marker(p).addTo(map), map.setView(p, 16));
    document.getElementById('when').textContent = 'Location at ' + new Date(s.last.t * 1000).toLocaleTimeString();
  }
  setTimeout(tick, 10000);
}
tick();
</script></body></html>`;
