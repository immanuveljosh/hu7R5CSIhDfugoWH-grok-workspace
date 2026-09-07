import { Circle, Mic, MicOff, Pause, PhoneOff, Square, Volume2 } from "lucide-react";
import { useEffect, useState, type ReactNode } from "react";
import { formatDuration, formatPhoneDisplay, initials } from "@/lib/dialer/format";
import { useDialer } from "@/lib/dialer/store";
import { cn } from "@/lib/utils";

export function InCallScreen() {
  const call = useDialer((s) => s.call);
  const hangup = useDialer((s) => s.hangup);
  const toggleMute = useDialer((s) => s.toggleMute);
  const toggleSpeaker = useDialer((s) => s.toggleSpeaker);
  const toggleHold = useDialer((s) => s.toggleHold);
  const toggleCallRecording = useDialer((s) => s.toggleCallRecording);
  const [now, setNow] = useState(Date.now());

  useEffect(() => {
    const id = window.setInterval(() => setNow(Date.now()), 250);
    return () => window.clearInterval(id);
  }, []);

  if (!call) return null;
  const elapsed = call.connectedAt ? now - call.connectedAt : 0;
  const status =
    call.state === "connecting" ? "Calling…" : call.onHold ? "On hold" : call.state === "active" ? formatDuration(elapsed) : "Call";

  return (
    <div className="flex h-full flex-col bg-bg px-6 pb-10 pt-8">
      <div className="flex flex-col items-center pt-8">
        <div className="flex size-28 items-center justify-center rounded-full bg-elevated text-3xl font-medium tracking-tight">
          {call.name ? initials(call.name) : "#"}
        </div>
        <h1 className="mt-6 text-center text-2xl font-semibold tracking-tight">{call.name ?? formatPhoneDisplay(call.number)}</h1>
        {call.name ? <p className="mt-1 font-mono text-sm text-muted">{formatPhoneDisplay(call.number)}</p> : null}
        <p className="mt-3 font-mono text-sm tabular-nums text-muted">{status}</p>
        {call.recording ? (
          <span className="mt-4 inline-flex items-center gap-2 rounded-full bg-hangup/15 px-3 py-1.5 text-xs font-medium text-hangup">
            <span className="rec-dot size-1.5 rounded-full bg-rec" />
            {call.recordingNotice ?? "Recording"}
          </span>
        ) : call.recordingError ? (
          <span className="mt-4 max-w-64 text-center text-xs text-muted">{call.recordingError}</span>
        ) : null}
      </div>
      <div className="mt-auto grid grid-cols-3 gap-5 px-4">
        <CallAction label={call.muted ? "Unmute" : "Mute"} active={call.muted} onClick={toggleMute} icon={call.muted ? <MicOff className="size-5" /> : <Mic className="size-5" />} />
        <CallAction label={call.speaker ? "Speaker" : "Audio"} active={call.speaker} onClick={toggleSpeaker} icon={<Volume2 className="size-5" />} />
        <CallAction label={call.onHold ? "Resume" : "Hold"} active={call.onHold} onClick={toggleHold} icon={<Pause className="size-5" />} />
      </div>
      <button
        type="button"
        aria-label={call.recording ? "Stop recording" : "Record this call"}
        onClick={toggleCallRecording}
        className={cn(
          "mt-8 flex h-12 w-full items-center justify-center gap-2 rounded-full text-sm font-medium transition-colors duration-150 active:scale-95",
          call.recording ? "bg-hangup text-hangup-fg" : "bg-elevated text-fg",
        )}
      >
        {call.recording ? <Square className="size-3.5 fill-current" /> : <Circle className="size-4 fill-hangup text-hangup" />}
        {call.recording ? "Stop recording" : "Record this call"}
      </button>
      <div className="mt-6 flex justify-center">
        <button
          type="button"
          aria-label="Hang up"
          onClick={() => void hangup()}
          className="flex size-16 items-center justify-center rounded-full bg-hangup text-hangup-fg shadow-[0_8px_24px_-8px_rgba(226,75,75,0.7)] active:scale-95"
        >
          <PhoneOff className="size-7" />
        </button>
      </div>
    </div>
  );
}

function CallAction({ label, icon, onClick, active }: { label: string; icon: ReactNode; onClick: () => void; active?: boolean }) {
  return (
    <button type="button" onClick={onClick} className="flex flex-col items-center gap-2">
      <span className={cn("flex size-14 items-center justify-center rounded-full", active ? "bg-fg text-bg" : "bg-elevated text-fg")}>{icon}</span>
      <span className="text-[11px] text-muted">{label}</span>
    </button>
  );
}
