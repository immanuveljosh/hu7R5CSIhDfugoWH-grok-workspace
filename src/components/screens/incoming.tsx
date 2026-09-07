import { Phone, PhoneOff } from "lucide-react";
import { formatPhoneDisplay, initials } from "@/lib/dialer/format";
import { useDialer } from "@/lib/dialer/store";

export function IncomingCallScreen() {
  const call = useDialer((s) => s.call);
  const answerCall = useDialer((s) => s.answerCall);
  const declineCall = useDialer((s) => s.declineCall);
  const autoRecord = useDialer((s) => s.settings.autoRecord);
  if (!call) return null;

  return (
    <div className="flex h-full flex-col bg-bg px-6 pb-12 pt-16">
      <p className="text-center text-sm font-medium uppercase tracking-[0.18em] text-muted">Incoming call</p>
      <div className="mt-10 flex flex-col items-center">
        <div className="ring-soft flex size-28 items-center justify-center rounded-full bg-elevated text-3xl font-medium">
          {call.name ? initials(call.name) : "#"}
        </div>
        <h1 className="mt-6 text-center text-3xl font-semibold tracking-tight">{call.name ?? formatPhoneDisplay(call.number)}</h1>
        {call.name ? <p className="mt-1 font-mono text-sm text-muted">{formatPhoneDisplay(call.number)}</p> : null}
        {autoRecord ? (
          <p className="mt-5 max-w-64 text-center text-xs leading-5 text-muted">
            Auto-record is on. This call will be recorded on this device if audio capture is allowed.
          </p>
        ) : null}
      </div>
      <div className="mt-auto flex items-center justify-around px-4">
        <button type="button" onClick={declineCall} className="flex flex-col items-center gap-2">
          <span className="flex size-16 items-center justify-center rounded-full bg-hangup text-hangup-fg">
            <PhoneOff className="size-7" />
          </span>
          <span className="text-xs text-muted">Decline</span>
        </button>
        <button type="button" onClick={answerCall} className="flex flex-col items-center gap-2">
          <span className="flex size-16 items-center justify-center rounded-full bg-call text-call-fg">
            <Phone className="size-7" fill="currentColor" />
          </span>
          <span className="text-xs text-muted">Answer</span>
        </button>
      </div>
    </div>
  );
}
