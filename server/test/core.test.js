import { test } from "node:test";
import assert from "node:assert/strict";
import { seal, sealOk } from "../seal.js";
import { Live } from "../live.js";
import { Community } from "../community.js";
import { seedPoints } from "../seed.js";

const caseA = { case_id: "SA-1", created_at: 1, evidence: [{ name: "audio", sha256: "ab", received_at: 2 }] };

test("sealDetectsEdit", () => {
  const c = { ...caseA, seal: seal(caseA) };
  assert.equal(sealOk(c), true);
  const edited = { ...c, evidence: [{ ...c.evidence[0], sha256: "cd" }] };
  assert.equal(sealOk(edited), false);
});

test("liveTokenIs32Hex", () => {
  const live = new Live();
  const { token } = live.create({ name: "Priya", plate: "KA05AB1234" }, 0);
  assert.match(token, /^[0-9a-f]{32}$/);
});

test("liveExpires", () => {
  const live = new Live();
  const { token } = live.create({ name: "Priya" }, 0);
  assert.equal(live.update(token, { lat: 1, lng: 2, t: 3, events: [] }, 60 * 1000), true);
  assert.equal(live.state(token, 60 * 1000 + 61 * 60 * 1000), null);
});

test("pointRoundsLocation", () => {
  const c = new Community();
  c.add({ lat: 12.971234, lng: 77.594987, t: 1760800000, kind: "felt_off", plate: null }, "acc1");
  const p = c.points()[0];
  assert.equal(p.lat, 12.971);
  assert.equal(p.lng, 77.595);
});

test("plateHashedNotStored", () => {
  const c = new Community();
  c.add({ lat: 1, lng: 1, t: 1, kind: "alert", plate: "KA 05 AB 1234" }, "acc1");
  assert.equal(JSON.stringify(c.raw()).includes("KA05AB1234"), false);
  assert.equal(JSON.stringify(c.raw()).includes("KA 05 AB 1234"), false);
});

test("vehicleFlaggedAt3DistinctAccounts", () => {
  const c = new Community();
  for (let i = 0; i < 3; i++) c.add({ lat: 1, lng: 1, t: 1000 + i, kind: "alert", plate: "KA05XY9876" }, "same");
  assert.deepEqual(c.vehicle("KA05XY9876", 2000), { reports: 1, flagged: false });
  c.add({ lat: 1, lng: 1, t: 1500, kind: "alert", plate: "ka-05-xy-9876" }, "b");
  c.add({ lat: 1, lng: 1, t: 1600, kind: "alert", plate: "KA05XY9876" }, "c");
  assert.deepEqual(c.vehicle("KA05XY9876", 2000), { reports: 3, flagged: true });
});

test("oldReportsDontCount", () => {
  const c = new Community();
  const day = 86400;
  ["a", "b", "c"].forEach((acc) => c.add({ lat: 1, lng: 1, t: 0, kind: "alert", plate: "KA01AA0001" }, acc));
  assert.equal(c.vehicle("KA01AA0001", 91 * day).flagged, false);
});

test("seedMarkedSample", () => {
  const pts = seedPoints();
  assert.ok(pts.length > 100);
  const c = new Community({ sample: true });
  assert.equal(c.isSample, true);
});
