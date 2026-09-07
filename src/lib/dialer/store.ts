import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import type { ActiveCall, CallDirection, DialerSettings, RecentCall, Recording, TabId } from "./types";
import { CONTACTS, SEED_RECENTS, buildSeedRecordings, contactByNumber } from "./seed";
import { formatRecordingFilename, resolveCapability } from "./format";
import { blobToDataUrl, synthesizeCallAudio } from "./audio";

const uid = () => `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;

const defaultSettings: DialerSettings = {
  autoRecord: false,
  quality: "standard",
  privacyAccepted: false,
  isDefaultDialer: false,
  simulateUnsupportedDevice: false,
  permissions: { phone: false, contacts: false, microphone: false, notifications: false },
};

interface PlayerState {
  recordingId: string | null;
  playing: boolean;
  progress: number;
}

interface DialerStore {
  hydrated: boolean;
  tab: TabId;
  dial: string;
  settings: DialerSettings;
  recents: RecentCall[];
  recordings: Recording[];
  call: ActiveCall | null;
  player: PlayerState;
  showPrivacy: boolean;
  showPermissions: boolean;
  showDefaultDialer: boolean;
  confirmDeleteAll: boolean;
  toast: string | null;
  seedReady: boolean;
  setTab: (tab: TabId) => void;
  setDial: (value: string) => void;
  appendDial: (ch: string) => void;
  backspace: () => void;
  clearDial: () => void;
  acceptPrivacy: () => void;
  grantPermissions: () => void;
  setDefaultDialer: (value: boolean) => void;
  setAutoRecord: (value: boolean) => void;
  setQuality: (value: DialerSettings["quality"]) => void;
  setSimulateUnsupported: (value: boolean) => void;
  placeCall: (number?: string, direction?: CallDirection) => void;
  answerCall: () => void;
  declineCall: () => void;
  hangup: () => Promise<void>;
  toggleMute: () => void;
  toggleSpeaker: () => void;
  toggleHold: () => void;
  toggleCallRecording: () => void;
  simulateIncoming: () => void;
  deleteRecording: (id: string) => void;
  deleteAllRecordings: () => void;
  setConfirmDeleteAll: (v: boolean) => void;
  setPlayer: (patch: Partial<PlayerState>) => void;
  setToast: (msg: string | null) => void;
  setShowDefaultDialer: (v: boolean) => void;
  loadSeeds: () => Promise<void>;
}

function lookup(number: string) {
  const c = contactByNumber(number);
  return { number, name: c?.name };
}

function applyRecordingFlags(want: boolean, simulateUnsupported: boolean) {
  const cap = resolveCapability(simulateUnsupported);
  if (!want) return { recording: false as const, recordingNotice: undefined, recordingError: undefined };
  if (!cap.supported) return { recording: false as const, recordingNotice: undefined, recordingError: cap.reason };
  return {
    recording: true as const,
    recordingError: undefined,
    recordingNotice: cap.nearEndOnly ? "Recording near-end audio only" : "Recording call",
  };
}

export const useDialer = create<DialerStore>()(
  persist(
    (set, get) => ({
      hydrated: false,
      tab: "keypad",
      dial: "",
      settings: defaultSettings,
      recents: SEED_RECENTS,
      recordings: [],
      call: null,
      player: { recordingId: null, playing: false, progress: 0 },
      showPrivacy: true,
      showPermissions: false,
      showDefaultDialer: false,
      confirmDeleteAll: false,
      toast: null,
      seedReady: false,
      setTab: (tab) => set({ tab }),
      setDial: (value) => set({ dial: value }),
      appendDial: (ch) => set({ dial: (get().dial + ch).slice(0, 20) }),
      backspace: () => set({ dial: get().dial.slice(0, -1) }),
      clearDial: () => set({ dial: "" }),
      acceptPrivacy: () =>
        set({
          settings: { ...get().settings, privacyAccepted: true },
          showPrivacy: false,
          showPermissions: true,
        }),
      grantPermissions: () =>
        set({
          settings: {
            ...get().settings,
            permissions: { phone: true, contacts: true, microphone: true, notifications: true },
          },
          showPermissions: false,
        }),
      setDefaultDialer: (value) =>
        set({
          settings: { ...get().settings, isDefaultDialer: value },
          showDefaultDialer: false,
          toast: value ? "Pulse is the default phone app." : "Default phone role released.",
        }),
      setAutoRecord: (value) => {
        set((s) => {
          if (!value) {
            const call = s.call
              ? { ...s.call, recording: false, recordingNotice: undefined, recordingError: undefined }
              : s.call;
            return {
              settings: { ...s.settings, autoRecord: false },
              call,
              toast: "Auto call recording is off.",
            };
          }
          const call = s.call?.state === "active" ? { ...s.call, ...applyRecordingFlags(true, false) } : s.call;
          return {
            settings: {
              ...s.settings,
              autoRecord: true,
              simulateUnsupportedDevice: false,
              isDefaultDialer: true,
              permissions: { ...s.settings.permissions, microphone: true },
            },
            call,
            toast: "Auto call recording is on. Calls may be recorded.",
          };
        });
      },
      setQuality: (quality) => set({ settings: { ...get().settings, quality } }),
      setSimulateUnsupported: (value) => {
        set((s) => {
          const settings = { ...s.settings, simulateUnsupportedDevice: value, autoRecord: value ? false : s.settings.autoRecord };
          const call =
            value && s.call
              ? {
                  ...s.call,
                  recording: false,
                  recordingNotice: undefined,
                  recordingError: "Call recording is not supported on this device.",
                }
              : s.call;
          return {
            settings,
            call,
            toast: value
              ? "Call recording is not supported on this device."
              : "Recording probe restored (microphone fallback).",
          };
        });
      },
      placeCall: (number, direction = "outgoing") => {
        const raw = (number ?? get().dial).trim();
        if (!raw) {
          set({ toast: "Enter a number to call." });
          return;
        }
        if (get().call) {
          set({ toast: "Already on a call." });
          return;
        }
        const who = lookup(raw);
        const id = uid();
        const now = Date.now();
        set({
          call: {
            id,
            ...who,
            direction,
            state: direction === "incoming" ? "ringing" : "connecting",
            startedAt: now,
            muted: false,
            speaker: false,
            onHold: false,
            recording: false,
          },
        });
        if (direction === "outgoing") {
          window.setTimeout(() => {
            const current = get().call;
            if (!current || current.id !== id) return;
            const flags = applyRecordingFlags(get().settings.autoRecord || current.recording, get().settings.simulateUnsupportedDevice);
            set({
              call: { ...current, state: "active", connectedAt: Date.now(), ...flags },
            });
          }, 1400);
        }
      },
      answerCall: () => {
        const current = get().call;
        if (!current || current.state !== "ringing") return;
        const flags = applyRecordingFlags(get().settings.autoRecord || current.recording, get().settings.simulateUnsupportedDevice);
        set({
          call: { ...current, state: "active", connectedAt: Date.now(), ...flags },
        });
      },
      declineCall: () => {
        const current = get().call;
        if (!current) return;
        set({
          call: null,
          recents: [
            {
              id: uid(),
              number: current.number,
              name: current.name,
              direction: current.direction,
              at: Date.now(),
              durationMs: 0,
              missed: current.direction === "incoming",
            },
            ...get().recents,
          ].slice(0, 40),
        });
      },
      hangup: async () => {
        const current = get().call;
        if (!current) return;
        const connectedAt = current.connectedAt ?? Date.now();
        const durationMs = current.state === "active" ? Math.max(800, Date.now() - connectedAt) : 0;
        const recent: RecentCall = {
          id: uid(),
          number: current.number,
          name: current.name,
          direction: current.direction,
          at: Date.now(),
          durationMs,
          missed: current.direction === "incoming" && current.state === "ringing",
        };
        let recordings = get().recordings;
        if (current.recording && current.state === "active") {
          try {
            const syn = synthesizeCallAudio(Math.min(durationMs, 12_000), durationMs);
            const dataUrl = await blobToDataUrl(syn.blob);
            URL.revokeObjectURL(syn.dataUrl);
            recordings = [
              {
                id: uid(),
                filename: formatRecordingFilename(new Date(connectedAt)),
                number: current.number,
                name: current.name,
                direction: current.direction,
                startedAt: connectedAt,
                durationMs,
                fileSize: syn.bytes,
                audioDataUrl: dataUrl,
                source: "MIC",
              },
              ...recordings,
            ];
          } catch {
            set({ toast: "Could not save recording. Storage may be full." });
          }
        }
        set({
          call: null,
          recents: [recent, ...get().recents].slice(0, 40),
          recordings,
        });
      },
      toggleMute: () => {
        const call = get().call;
        if (call) set({ call: { ...call, muted: !call.muted } });
      },
      toggleSpeaker: () => {
        const call = get().call;
        if (call) set({ call: { ...call, speaker: !call.speaker } });
      },
      toggleHold: () => {
        const call = get().call;
        if (call) set({ call: { ...call, onHold: !call.onHold } });
      },
      toggleCallRecording: () => {
        const call = get().call;
        if (!call) return;
        if (call.recording) {
          set({
            call: { ...call, recording: false, recordingNotice: undefined, recordingError: undefined },
            toast: "Recording stopped.",
          });
          return;
        }
        const flags = applyRecordingFlags(true, get().settings.simulateUnsupportedDevice);
        set({
          call: { ...call, ...flags },
          toast: flags.recording ? "Recording this call." : (flags.recordingError ?? null),
        });
      },
      simulateIncoming: () => {
        if (get().call) {
          set({ toast: "Already on a call." });
          return;
        }
        const pick = CONTACTS[Math.floor(Math.random() * CONTACTS.length)];
        get().placeCall(pick.number, "incoming");
      },
      deleteRecording: (id) => {
        const recordings = get().recordings.filter((r) => r.id !== id);
        const player = get().player;
        set({
          recordings,
          player: player.recordingId === id ? { recordingId: null, playing: false, progress: 0 } : player,
        });
      },
      deleteAllRecordings: () =>
        set({
          recordings: [],
          player: { recordingId: null, playing: false, progress: 0 },
          confirmDeleteAll: false,
          toast: "All recordings deleted from this device.",
        }),
      setConfirmDeleteAll: (v) => set({ confirmDeleteAll: v }),
      setPlayer: (patch) => set({ player: { ...get().player, ...patch } }),
      setToast: (msg) => set({ toast: msg }),
      setShowDefaultDialer: (v) => set({ showDefaultDialer: v }),
      loadSeeds: async () => {
        if (get().seedReady || get().recordings.length > 0) {
          set({ seedReady: true, hydrated: true });
          return;
        }
        try {
          const recordings = await buildSeedRecordings();
          set({ recordings, seedReady: true, hydrated: true });
        } catch {
          set({ seedReady: true, hydrated: true });
        }
      },
    }),
    {
      name: "pulse-dialer-v3",
      skipHydration: true,
      storage: createJSONStorage(() => localStorage),
      partialize: (s) => ({
        settings: { ...s.settings, simulateUnsupportedDevice: false },
        recents: s.recents.slice(0, 20),
        dial: s.dial,
      }),
      onRehydrateStorage: () => (state) => {
        if (!state) return;
        state.hydrated = true;
        state.settings.simulateUnsupportedDevice = false;
        state.showPrivacy = !state.settings.privacyAccepted;
        state.showPermissions = false;
        state.showDefaultDialer = false;
      },
    },
  ),
);
