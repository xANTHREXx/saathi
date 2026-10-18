import { hmac } from "./seal.js";
import { seedPoints } from "./seed.js";

const DAY = 86400;
const FLAG_ACCOUNTS = 3;
const FLAG_WINDOW = 90 * DAY;

// Same rule as the phone: case, spaces and dashes don't matter; O/0 and I/1 are one character.
const plateKey = (p) =>
  p ? hmac(String(p).toUpperCase().replace(/[^A-Z0-9]/g, "").replace(/O/g, "0").replace(/I/g, "1")) : null;

const istBand = (t) => Math.floor((((t + 19800) % DAY) / 3600) / 3); // 0..7, 3-hour bands in IST

/** Anonymous community data: rounded place, time band, kind, keyed plate. No names, no numbers. */
export class Community {
  #points = [];
  constructor({ sample = false } = {}) {
    this.isSample = sample;
    if (sample) for (const p of seedPoints()) this.add(p, p.account);
  }

  add({ lat, lng, t, kind, plate }, account) {
    this.#points.push({
      lat: Math.round(+lat * 1000) / 1000,
      lng: Math.round(+lng * 1000) / 1000,
      t: +t,
      band: istBand(+t),
      kind: String(kind || "report"),
      plateKey: plateKey(plate),
      account: hmac("acct:" + account),
    });
  }

  points() {
    return this.#points.map(({ lat, lng, band, kind }) => ({ lat, lng, band, kind }));
  }

  raw() {
    return this.#points;
  }

  vehicle(plate, now = Date.now() / 1000) {
    const key = plateKey(plate);
    const accounts = new Set(
      this.#points.filter((p) => p.plateKey === key && now - p.t <= FLAG_WINDOW).map((p) => p.account),
    );
    return { reports: accounts.size, flagged: accounts.size >= FLAG_ACCOUNTS };
  }
}
