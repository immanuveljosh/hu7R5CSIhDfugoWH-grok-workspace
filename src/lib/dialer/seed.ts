import type { Contact, RecentCall, Recording } from "./types";
import { formatRecordingFilename } from "./format";
import { blobToDataUrl, synthesizeCallAudio } from "./audio";

export const CONTACTS: Contact[] = [
  { id: "c1", name: "Priya Natarajan", number: "+919840011223", company: "Natarajan Mills" },
  { id: "c2", name: "Amara Chen", number: "+14155550192", company: "North Bay Studio" },
  { id: "c3", name: "Rajesh Iyer", number: "+919876543210", company: "Iyer & Co" },
  { id: "c4", name: "Elena Voss", number: "+49305551200", company: "Voss Atelier" },
  { id: "c5", name: "Jordan Hale", number: "+12125550144", company: "Hale Transit" },
  { id: "c6", name: "Meena Krishnan", number: "+914222334455", company: "Coimbatore Clinic" },
  { id: "c7", name: "Tomasz Lewandowski", number: "+48225550199", company: "Lewandowski Lab" },
  { id: "c8", name: "Sofia Alvarez", number: "+34915550121", company: "Alvarez Ediciones" },
];

export function contactByNumber(number: string): Contact | undefined {
  const n = number.replace(/\s/g, "");
  return CONTACTS.find((c) => c.number.replace(/\s/g, "") === n);
}

function hoursAgo(h: number): number {
  return Date.now() - h * 3600_000;
}

export const SEED_RECENTS: RecentCall[] = [
  { id: "r1", number: "+919840011223", name: "Priya Natarajan", direction: "outgoing", at: hoursAgo(0.4), durationMs: 8 * 60_000 + 12_000, missed: false },
  { id: "r2", number: "+14155550192", name: "Amara Chen", direction: "incoming", at: hoursAgo(3), durationMs: 0, missed: true },
  { id: "r3", number: "+914222334455", name: "Meena Krishnan", direction: "incoming", at: hoursAgo(7), durationMs: 4 * 60_000 + 41_000, missed: false },
  { id: "r4", number: "+12125550144", name: "Jordan Hale", direction: "outgoing", at: hoursAgo(22), durationMs: 62_000, missed: false },
  { id: "r5", number: "+49305551200", name: "Elena Voss", direction: "incoming", at: hoursAgo(28), durationMs: 12 * 60_000, missed: false },
  { id: "r6", number: "+919876543210", name: "Rajesh Iyer", direction: "outgoing", at: hoursAgo(50), durationMs: 0, missed: true },
];

export async function buildSeedRecordings(): Promise<Recording[]> {
  const specs = [
    { id: "rec1", number: "+919840011223", name: "Priya Natarajan", direction: "outgoing" as const, dur: 5200, when: hoursAgo(0.4), seed: 11 },
    { id: "rec2", number: "+914222334455", name: "Meena Krishnan", direction: "incoming" as const, dur: 3800, when: hoursAgo(7), seed: 29 },
    { id: "rec3", number: "+49305551200", name: "Elena Voss", direction: "incoming" as const, dur: 6100, when: hoursAgo(28), seed: 47 },
  ];
  const out: Recording[] = [];
  for (const spec of specs) {
    const syn = synthesizeCallAudio(spec.dur, spec.seed);
    const dataUrl = await blobToDataUrl(syn.blob);
    URL.revokeObjectURL(syn.dataUrl);
    const started = new Date(spec.when);
    out.push({
      id: spec.id,
      filename: formatRecordingFilename(started),
      number: spec.number,
      name: spec.name,
      direction: spec.direction,
      startedAt: spec.when,
      durationMs: spec.dur,
      fileSize: syn.bytes,
      audioDataUrl: dataUrl,
      source: "MIC",
    });
  }
  return out;
}
