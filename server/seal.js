import { createHmac, randomBytes } from "node:crypto";

// The seal key lives only on the server. Set SAATHI_SECRET in production so seals survive restarts.
export const SECRET = process.env.SAATHI_SECRET || randomBytes(32).toString("hex");

const canonical = (c) =>
  JSON.stringify({
    case_id: c.case_id,
    created_at: c.created_at,
    evidence: (c.evidence || []).map((e) => ({ name: e.name, received_at: e.received_at, sha256: e.sha256 })),
  });

export const seal = (c) => createHmac("sha256", SECRET).update(canonical(c)).digest("hex");
export const sealOk = (c) => typeof c.seal === "string" && c.seal === seal(c);
export const hmac = (s) => createHmac("sha256", SECRET).update(s).digest("hex");
