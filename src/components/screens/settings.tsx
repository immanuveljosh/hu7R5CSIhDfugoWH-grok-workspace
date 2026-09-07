import { HardDrive, Info, Mic, Phone, Shield } from "lucide-react";
import { formatBytes, resolveCapability } from "@/lib/dialer/format";
import { useDialer } from "@/lib/dialer/store";
import { cn } from "@/lib/utils";

function SwitchTrack({ checked }: { checked: boolean }) {
  return (
    <span
      aria-hidden
      className={cn(
        "relative h-11 w-16 shrink-0 rounded-full transition-colors duration-150",
        checked ? "bg-call" : "bg-key",
      )}
    >
      <span
        className={cn(
          "absolute top-1 left-1 size-9 rounded-full bg-fg transition-transform duration-150",
          checked && "translate-x-5",
        )}
      />
    </span>
  );
}

function Toggle({
  checked,
  onChange,
  label,
}: {
  checked: boolean;
  onChange: (v: boolean) => void;
  label: string;
}) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={label}
      onClick={() => onChange(!checked)}
      className="shrink-0"
    >
      <SwitchTrack checked={checked} />
    </button>
  );
}

export function SettingsScreen() {
  const autoRecord = useDialer((s) => s.settings.autoRecord);
  const quality = useDialer((s) => s.settings.quality);
  const isDefaultDialer = useDialer((s) => s.settings.isDefaultDialer);
  const simulateUnsupported = useDialer((s) => s.settings.simulateUnsupportedDevice);
  const recordings = useDialer((s) => s.recordings);
  const setAutoRecord = useDialer((s) => s.setAutoRecord);
  const setQuality = useDialer((s) => s.setQuality);
  const setSimulateUnsupported = useDialer((s) => s.setSimulateUnsupported);
  const setShowDefaultDialer = useDialer((s) => s.setShowDefaultDialer);
  const simulateIncoming = useDialer((s) => s.simulateIncoming);
  const cap = resolveCapability(simulateUnsupported);
  const used = recordings.reduce((n, r) => n + r.fileSize, 0);

  return (
    <div className="flex h-full flex-col overflow-y-auto px-5 pb-6 pt-2">
      <p className="text-sm font-medium text-muted">Settings</p>
      <h1 className="mt-1 text-2xl font-semibold tracking-tight">Pulse</h1>

      <section className="mt-6 rounded-xl bg-surface p-2">
        <button
          type="button"
          role="switch"
          aria-checked={autoRecord}
          aria-label="Auto call recording"
          onClick={() => setAutoRecord(!autoRecord)}
          className="flex w-full items-start gap-3 rounded-lg p-3 text-left hover:bg-elevated/60 active:bg-elevated"
        >
          <Mic className={cn("mt-0.5 size-5 shrink-0", autoRecord ? "text-call" : "text-muted")} />
          <div className="min-w-0 flex-1">
            <div className="flex items-center justify-between gap-3">
              <p className="text-sm font-medium">Auto call recording</p>
              <SwitchTrack checked={autoRecord} />
            </div>
            <p className="mt-1 text-xs leading-5 text-muted">
              When on, Pulse records eligible calls and stops when the call ends. Never records while this is off.
            </p>
            {autoRecord ? (
              <p className="mt-2 rounded-md bg-hangup/10 px-2.5 py-2 text-xs text-hangup">
                Recording is enabled. Tell the other party if your local law requires consent.
              </p>
            ) : null}
            {!cap.supported ? (
              <p className="mt-2 rounded-md bg-elevated px-2.5 py-2 text-xs text-fg">{cap.reason}</p>
            ) : (
              <p className="mt-2 text-xs text-subtle">{cap.label}</p>
            )}
          </div>
        </button>
      </section>

      <section className="mt-3 rounded-xl bg-surface p-4">
        <p className="text-sm font-medium">Recording quality</p>
        <div className="mt-3 grid grid-cols-2 gap-2">
          {(["standard", "high"] as const).map((q) => (
            <button
              key={q}
              type="button"
              onClick={() => setQuality(q)}
              className={cn("rounded-md px-3 py-2 text-sm capitalize", quality === q ? "bg-call text-call-fg" : "bg-elevated text-fg")}
            >
              {q}
              <span className="mt-0.5 block text-[11px] opacity-70">{q === "high" ? "AAC 48 kHz" : "AAC 16 kHz"}</span>
            </button>
          ))}
        </div>
      </section>

      <section className="mt-3 rounded-xl bg-surface p-4">
        <div className="flex items-start gap-3">
          <HardDrive className="mt-0.5 size-5 text-muted" />
          <div>
            <p className="text-sm font-medium">Local storage</p>
            <p className="mt-1 text-xs leading-5 text-muted">
              {recordings.length} file{recordings.length === 1 ? "" : "s"} · {formatBytes(used)} in app-private storage. Pulse never uploads recordings.
            </p>
          </div>
        </div>
      </section>

      <section className="mt-3 rounded-xl bg-surface p-4">
        <div className="flex items-start gap-3">
          <Phone className="mt-0.5 size-5 text-muted" />
          <div className="min-w-0 flex-1">
            <div className="flex items-center justify-between gap-3">
              <p className="text-sm font-medium">Default phone app</p>
              <span className={cn("text-xs font-medium", isDefaultDialer ? "text-call" : "text-muted")}>
                {isDefaultDialer ? "Active" : "Off"}
              </span>
            </div>
            <p className="mt-1 text-xs leading-5 text-muted">Android requires the default dialer role for in-call UI and eligible recording.</p>
            <button type="button" onClick={() => setShowDefaultDialer(true)} className="mt-3 h-11 rounded-md bg-elevated px-3 text-sm font-medium">
              {isDefaultDialer ? "Manage role" : "Set as default dialer"}
            </button>
          </div>
        </div>
      </section>

      <section className="mt-3 rounded-xl bg-surface p-4">
        <div className="flex items-start gap-3">
          <Shield className="mt-0.5 size-5 text-muted" />
          <div>
            <p className="text-sm font-medium">Privacy</p>
            <p className="mt-1 text-xs leading-5 text-muted">
              Recordings are written only to this app’s private files. No cloud, no analytics of call audio, no accessibility or hidden APIs.
            </p>
          </div>
        </div>
      </section>

      <section className="mt-3 rounded-xl bg-surface p-4">
        <div className="flex items-start gap-3">
          <Info className="mt-0.5 size-5 text-muted" />
          <div className="min-w-0 flex-1">
            <p className="text-sm font-medium">Companion lab</p>
            <p className="mt-1 text-xs leading-5 text-muted">Try recording here. On a phone, open the Kotlin project in Android Studio.</p>
            <div className="mt-3 flex flex-col gap-2">
              <button type="button" onClick={simulateIncoming} className="h-11 rounded-md bg-elevated px-3 text-sm font-medium">
                Simulate incoming call
              </button>
              <div className="flex items-center justify-between gap-3 pt-1">
                <p className="text-xs text-muted">Pretend this device blocks call audio</p>
                <Toggle
                  checked={simulateUnsupported}
                  onChange={setSimulateUnsupported}
                  label="Simulate unsupported device"
                />
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
