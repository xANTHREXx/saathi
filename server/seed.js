// Synthetic community data for the demo. Every screen that shows it says "Sample data".
// Fictional places along transit corridors; no real report, person or vehicle.

function rng(seed) {
  let s = seed;
  return () => ((s = (s * 1664525 + 1013904223) % 4294967296) / 4294967296);
}

const corridors = [
  { name: "Bengaluru, Silk Board to Koramangala", from: [12.9172, 77.6229], to: [12.9352, 77.6245] },
  { name: "Bengaluru, Majestic bus stand area", from: [12.9767, 77.5713], to: [12.9779, 77.5728] },
  { name: "Bengaluru, Hosur Road", from: [12.9070, 77.6235], to: [12.8900, 77.6400] },
  { name: "Pune, Swargate to Shivajinagar", from: [18.5018, 73.8636], to: [18.5308, 73.8475] },
  { name: "Pune, Hadapsar bus route", from: [18.5089, 73.9260], to: [18.4966, 73.9416] },
];

const flagged = [
  { plate: "KA05XY9876", accounts: 4 },
  { plate: "KA01AB4321", accounts: 3 },
  { plate: "MH12CD5678", accounts: 5 },
];

export function seedPoints(now = Math.floor(Date.now() / 1000)) {
  const r = rng(42);
  const pts = [];
  let acct = 0;
  for (const c of corridors) {
    for (let i = 0; i < 32; i++) {
      const f = r();
      const evening = r() < 0.7; // most reports after 9 pm
      const hour = evening ? 21 + Math.floor(r() * 3) : 7 + Math.floor(r() * 12);
      const daysAgo = Math.floor(r() * 80);
      const dayStart = now - daysAgo * 86400 - ((now + 19800) % 86400);
      pts.push({
        lat: c.from[0] + (c.to[0] - c.from[0]) * f + (r() - 0.5) * 0.002,
        lng: c.from[1] + (c.to[1] - c.from[1]) * f + (r() - 0.5) * 0.002,
        t: dayStart + hour * 3600 + Math.floor(r() * 3600),
        kind: r() < 0.6 ? "felt_off" : "alert",
        plate: null,
        account: `seed-${acct++}`,
      });
    }
  }
  for (const v of flagged) {
    for (let i = 0; i < v.accounts; i++) {
      pts.push({ lat: 12.93 + r() * 0.02, lng: 77.6 + r() * 0.02, t: now - Math.floor(r() * 60) * 86400, kind: "alert", plate: v.plate, account: `seed-v-${v.plate}-${i}` });
    }
  }
  return pts;
}
