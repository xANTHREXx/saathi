import { GoogleGenAI } from "@google/genai";

// Tried in order. A busy, retired or unavailable model falls through to the next; Gemma keeps us
// on Google models if Gemini quota runs out (see the fallback-stack note).
const MODELS = [process.env.GEMINI_MODEL, "gemini-3.8-flash", "gemini-flash-latest", "gemini-3.5-flash-lite", "gemma-4-31b-it"].filter(Boolean);

let client;
const ai = () => (client ??= new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY }));
export const configured = () => Boolean(process.env.GEMINI_API_KEY);

/** Real Gemini call: returns { text, sources }. Injected in tests. */
export async function gemini(contents, { json = false, search = false, system } = {}) {
  let last;
  for (const model of MODELS) {
    const gemma = model.startsWith("gemma");
    try {
      const config = {};
      if (json && !gemma) config.responseMimeType = "application/json";
      if (search && !gemma) config.tools = [{ googleSearch: {} }];
      if (system && !gemma) config.systemInstruction = system;
      const parts = gemma && system ? [{ text: system }, ...contents] : contents;
      const r = await ai().models.generateContent({ model, contents: [{ role: "user", parts }], config });
      const chunks = r.candidates?.[0]?.groundingMetadata?.groundingChunks || [];
      return { text: r.text || "", sources: chunks.filter((c) => c.web?.uri).map((c) => ({ title: c.web.title, uri: c.web.uri })) };
    } catch (e) {
      last = e;
    }
  }
  throw last || new Error("No model available");
}

const parseJson = (t) => JSON.parse(String(t).replace(/^```(json)?\s*|\s*```$/g, ""));

// Same plate rule as the phone, so a missing model still gives a plate.
const PLATE = /\b([A-Z]{2})[\s-]?(\d{1,2})[\s-]?([A-Z]{0,3})[\s-]?(\d{4})\b/;
const normPlate = (p) => (p ? String(p).toUpperCase().replace(/[^A-Z0-9]/g, "") : null);
const findPlate = (t) => { const m = PLATE.exec(String(t || "").toUpperCase()); return m ? m.slice(1).join("") : null; };

const UNKNOWN = { source: "unknown", app: null, plate: null, vehicle_type: "unknown", driver_name: null, status: null, pickup: null, drop: null, confidence: 0 };

const TRIP_PROMPT = `You read one ride-app notification (Uber, Ola or Rapido) or one photo of a vehicle number plate, in India.
Return JSON only: {"source": "ride_app"|"plate_photo"|"unknown", "app": string|null, "plate": string|null,
"vehicle_type": "auto"|"cab"|"bike"|"unknown", "driver_name": string|null, "status": "assigned"|"arrived"|"started"|"ended"|null,
"pickup": string|null, "drop": string|null, "confidence": 0-100}.
Copy the plate exactly as shown. If something is not shown, use null. Never guess.`;

export async function readTrip({ text, image, mime }, gen = gemini) {
  try {
    const parts = [{ text: TRIP_PROMPT }];
    if (text) parts.push({ text: `Notification: ${text}` });
    if (image) parts.push({ inlineData: { data: image.toString("base64"), mimeType: mime || "image/jpeg" } });
    const out = { ...UNKNOWN, ...parseJson((await gen(parts, { json: true })).text) };
    out.plate = normPlate(out.plate) || findPlate(text);
    return out;
  } catch {
    return { ...UNKNOWN, plate: findPlate(text) };
  }
}

const SAFE = /\byou('| a)re (now )?safe\b/gi;

const COMPANION_SYSTEM = (lang) => `You are Saathi, on a phone call with a woman travelling alone in India who feels uneasy.
Speak like a calm elder sister, in the language ${lang}. Short sentences, out loud.
Say one or two nearby landmarks from the list you get, by name, so anyone near her hears that someone knows where she is.
Give one cautious next step at a time, such as walking to an open, busy place from the list.
Set raise_alert to true when she asks for help, says she is scared, or describes being followed, touched or threatened.
When you raise it, tell her her contacts have been told.
Never say she is safe. Never promise that help is coming. Never ask what happened to her.
Return JSON only: {"reply": string, "raise_alert": boolean}.`;

export async function companionTurn({ text, lang = "en-IN", lat, lng, history = [] }, gen = gemini, nearby = async () => []) {
  try {
    const places = lat != null ? await nearby(lat, lng) : [];
    const ctx = places.length ? `Open places nearby: ${places.map((p) => `${p.name} (${p.distance_m} m)`).join("; ")}.` : "No place list available.";
    const convo = history.slice(-6).map((h) => `${h.role}: ${h.text}`).join("\n");
    const r = parseJson((await gen([{ text: `${ctx}\n${convo}\nShe says: ${text}` }], { json: true, system: COMPANION_SYSTEM(lang) })).text);
    return { reply: String(r.reply || "").replace(SAFE, "I'm here with you"), raise_alert: Boolean(r.raise_alert), places };
  } catch {
    // No model: keep her talking and treat the call as a request for help.
    return { reply: "I'm here with you. I've told your contacts. Keep the phone with you and move towards people.", raise_alert: true, places: [] };
  }
}

export const DISCLAIMER = "This is information, not legal advice.";

const GUIDE_PROMPT = (rec, kind, country, state) => `You are the Reporting guide in Saathi. Use Google Search to check current procedures in ${state || ""} ${country || "India"}.
She chose: "${kind}". The trip record is below. Write Markdown, addressed to her, calm and short:
## Your options
For each, one line on what it does, what to bring and what to say: tell someone close; report in the ride app (only if it was a ride-app trip);
the police, including asking for a Zero FIR at any police station if in India; a women's helpline. Push none of them.
## Draft complaint
A first-person police complaint from the record: date, times, place, vehicle plate, what the record shows. Leave [brackets] for her name,
phone and anything not in the record. Include each evidence item's SHA-256 fingerprint. Never invent events.
## Sources
The pages you relied on.
TRIP RECORD
${JSON.stringify(rec).slice(0, 4000)}`;

export async function guide({ record, kind = "something happened", country = "India", state }, gen = gemini) {
  const r = await gen([{ text: GUIDE_PROMPT(record, kind, country, state) }], { search: true });
  const body = String(r.text || "").trim();
  return { markdown: body.startsWith(DISCLAIMER) ? body : `${DISCLAIMER}\n\n${body}`, sources: r.sources || [] };
}
