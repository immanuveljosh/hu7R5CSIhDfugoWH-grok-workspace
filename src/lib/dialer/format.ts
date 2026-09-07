const FILENAME_RE = /^Call_\d{4}-\d{2}-\d{2}_\d{2}-\d{2}-\d{2}\.m4a$/;

export function pad2(n: number): string {
  return String(n).padStart(2, "0");
}

export function formatRecordingFilename(date: Date): string {
  const y = date.getFullYear();
  const m = pad2(date.getMonth() + 1);
  const d = pad2(date.getDate());
  const h = pad2(date.getHours());
  const min = pad2(date.getMinutes());
  const s = pad2(date.getSeconds());
  return `Call_${y}-${m}-${d}_${h}-${min}-${s}.m4a`;
}

export function isValidRecordingFilename(name: string): boolean {
  return FILENAME_RE.test(name);
}

export function formatDuration(ms: number): string {
  if (!Number.isFinite(ms) || ms < 0) return "0:00";
  const total = Math.floor(ms / 1000);
  const h = Math.floor(total / 3600);
  const m = Math.floor((total % 3600) / 60);
  const s = total % 60;
  if (h > 0) return `${h}:${pad2(m)}:${pad2(s)}`;
  return `${m}:${pad2(s)}`;
}

export function formatClock(date: Date): string {
  return `${pad2(date.getHours())}:${pad2(date.getMinutes())}`;
}

export function formatDayTime(epoch: number): string {
  const d = new Date(epoch);
  const now = new Date();
  const sameDay =
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate();
  const time = d.toLocaleTimeString(undefined, { hour: "numeric", minute: "2-digit" });
  if (sameDay) return time;
  const yesterday = new Date(now);
  yesterday.setDate(now.getDate() - 1);
  const isYest =
    d.getFullYear() === yesterday.getFullYear() &&
    d.getMonth() === yesterday.getMonth() &&
    d.getDate() === yesterday.getDate();
  if (isYest) return `Yesterday · ${time}`;
  return `${d.toLocaleDateString(undefined, { month: "short", day: "numeric" })} · ${time}`;
}

export function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function digitsOnly(value: string): string {
  return value.replace(/[^\d*+#]/g, "");
}

export function formatPhoneDisplay(raw: string): string {
  const v = raw.trim();
  if (!v) return "";
  const hasPlus = v.startsWith("+");
  const rest = v.replace(/[^\d*#]/g, "");
  if (rest.includes("*") || rest.includes("#")) return v;
  const digits = rest;
  if (hasPlus) {
    if (digits.startsWith("91") && digits.length === 12) {
      return `+91 ${digits.slice(2, 7)} ${digits.slice(7)}`;
    }
    if (digits.startsWith("1") && digits.length === 11) {
      return `+1 ${digits.slice(1, 4)} ${digits.slice(4, 7)} ${digits.slice(7)}`;
    }
    return `+${digits}`;
  }
  if (digits.length === 10) {
    return `${digits.slice(0, 3)} ${digits.slice(3, 6)} ${digits.slice(6)}`;
  }
  return v;
}

export function initials(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "?";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

export function resolveCapability(simulateUnsupported: boolean): import("./types").RecordingCapability {
  if (simulateUnsupported) {
    return { supported: false, reason: "Call recording is not supported on this device." };
  }
  return {
    supported: true,
    source: "MIC",
    label: "Microphone (near-end). Two-way call audio is blocked on this device.",
    nearEndOnly: true,
  };
}
