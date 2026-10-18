// Synthetic demo tracks. Fictional routes; no real person's home.
import { writeFileSync } from "node:fs";
const out = process.argv[2];
const mdeg = 1 / 111_320;
function track(start, legs) {
  let { lat, lng } = start, t = 0; const pts = [];
  for (const leg of legs) {
    for (let i = 0; i < leg.steps; i++) {
      const jitter = leg.stop ? ((i % 2) - 0.5) * 8 * mdeg : 0;
      lat += (leg.dn ?? 0) * mdeg + jitter; lng += (leg.de ?? 0) * mdeg / Math.cos(lat * Math.PI / 180);
      pts.push({ lat: +lat.toFixed(6), lng: +lng.toFixed(6), t }); t += 10;
      if (leg.stop) lat -= jitter;
    }
  }
  return pts;
}
// Bengaluru auto: 13 min moving, then a 12 min stop off route at a fuel station, then 8 min home.
writeFileSync(`${out}/blr-auto.json`, JSON.stringify(track({ lat: 12.9166, lng: 77.6101 }, [
  { steps: 78, dn: 45, de: 30 }, { steps: 6, de: 100 }, { steps: 72, stop: true }, { steps: 6, de: -100 }, { steps: 48, dn: 50, de: 20 },
])));
// Pune bus: moving with short stops at bus stops, about 20 min.
const legs = []; for (let i = 0; i < 8; i++) legs.push({ steps: 12, dn: 40, de: 25 }, { steps: 4, stop: true });
writeFileSync(`${out}/pune-bus.json`, JSON.stringify(track({ lat: 18.5204, lng: 73.8567 }, legs)));
console.log("ok");
