export type CallDirection = "incoming" | "outgoing";
export type CallState = "idle" | "ringing" | "connecting" | "active" | "ended";
export type TabId = "keypad" | "recents" | "contacts" | "recordings";
export type RecordingQuality = "standard" | "high";
export type AudioSourceId = "VOICE_CALL" | "VOICE_COMMUNICATION" | "MIC";

export type RecordingCapability =
  | { supported: true; source: AudioSourceId; label: string; nearEndOnly: boolean }
  | { supported: false; reason: string };

export interface Contact {
  id: string;
  name: string;
  number: string;
  company?: string;
}

export interface RecentCall {
  id: string;
  number: string;
  name?: string;
  direction: CallDirection;
  at: number;
  durationMs: number;
  missed: boolean;
}

export interface Recording {
  id: string;
  filename: string;
  number: string;
  name?: string;
  direction: CallDirection;
  startedAt: number;
  durationMs: number;
  fileSize: number;
  audioDataUrl: string;
  source: AudioSourceId;
}

export interface ActiveCall {
  id: string;
  number: string;
  name?: string;
  direction: CallDirection;
  state: Exclude<CallState, "idle">;
  startedAt: number;
  connectedAt?: number;
  muted: boolean;
  speaker: boolean;
  onHold: boolean;
  recording: boolean;
  recordingNotice?: string;
  recordingError?: string;
}

export interface DialerSettings {
  autoRecord: boolean;
  quality: RecordingQuality;
  privacyAccepted: boolean;
  isDefaultDialer: boolean;
  simulateUnsupportedDevice: boolean;
  permissions: {
    phone: boolean;
    contacts: boolean;
    microphone: boolean;
    notifications: boolean;
  };
}
