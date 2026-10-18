import { test } from "node:test";
import assert from "node:assert/strict";
import { readTrip, companionTurn, guide, DISCLAIMER } from "../ai.js";

const fakeJson = (obj) => async () => ({ text: JSON.stringify(obj) });
const failing = async () => { throw new Error("quota"); };

test("rideNotificationParsed", async () => {
  const r = await readTrip({ text: "Ravi is arriving · Auto KA 05 AB 1234" },
    fakeJson({ source: "ride_app", app: "Uber", plate: "KA 05 AB 1234", vehicle_type: "auto", driver_name: "Ravi", status: "arrived", confidence: 90 }));
  assert.equal(r.plate, "KA05AB1234");
  assert.equal(r.status, "arrived");
});

test("unreadableReturnsUnknown", async () => {
  const r = await readTrip({ text: "Your order is on the way" }, fakeJson({ source: "unknown", plate: null, confidence: 5 }));
  assert.equal(r.source, "unknown");
  assert.equal(r.plate, null);
});

test("modelErrorFallsBackToOnPhoneRegex", async () => {
  const r = await readTrip({ text: "Uber: Driver arriving in Auto KA05AB1234" }, failing);
  assert.equal(r.plate, "KA05AB1234");
  assert.equal(r.vehicle_type, "unknown");
});

test("replyNeverSaysSafe", async () => {
  const r = await companionTurn({ text: "hello", lang: "en-IN" },
    fakeJson({ reply: "You're safe now, I'm with you.", raise_alert: false }), async () => []);
  assert.equal(/you('| a)re safe/i.test(r.reply), false);
});

test("raiseAlertFlagPassesThrough", async () => {
  const r = await companionTurn({ text: "a man is following me, help", lang: "en-IN" },
    fakeJson({ reply: "I've told Riya and Amma.", raise_alert: true }), async () => []);
  assert.equal(r.raise_alert, true);
});

test("companionFailsGracefully", async () => {
  const r = await companionTurn({ text: "help", lang: "en-IN" }, failing, async () => []);
  assert.equal(r.raise_alert, true);
  assert.ok(r.reply.length > 0);
});

test("guideHasDisclaimer", async () => {
  const r = await guide({ record: { plate: "KA05AB1234" }, kind: "felt_off" }, async () => ({ text: "## Your options\n- Tell someone close", sources: [] }));
  assert.ok(r.markdown.startsWith(DISCLAIMER));
});

test("noModelOnlyRaisesForHelpWords", async () => {
  const calm = await companionTurn({ text: "hi, just checking in", lang: "en-IN" }, failing, async () => []);
  assert.equal(calm.raise_alert, false);
  const hindi = await companionTurn({ text: "bachao, koi peecha kar raha hai", lang: "hi-IN" }, failing, async () => []);
  assert.equal(hindi.raise_alert, true);
});
