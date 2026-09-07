import { ArrowDownLeft, ArrowUpRight, Phone, PhoneMissed } from "lucide-react";
import { formatDayTime, formatDuration, formatPhoneDisplay, initials } from "@/lib/dialer/format";
import { useDialer } from "@/lib/dialer/store";
import { cn } from "@/lib/utils";

export function RecentsScreen() {
  const recents = useDialer((s) => s.recents);
  const placeCall = useDialer((s) => s.placeCall);
  const setDial = useDialer((s) => s.setDial);
  const setTab = useDialer((s) => s.setTab);

  return (
    <div className="flex h-full flex-col">
      <div className="px-5 pt-2 pb-3">
        <p className="text-sm font-medium text-muted">Recents</p>
        <h1 className="mt-1 text-2xl font-semibold tracking-tight">Calls</h1>
      </div>
      <div className="min-h-0 flex-1 overflow-y-auto px-2 pb-2">
        {recents.length === 0 ? (
          <p className="px-3 py-12 text-center text-sm text-muted">No calls yet.</p>
        ) : (
          recents.map((c) => (
            <button
              key={c.id}
              type="button"
              onClick={() => {
                setDial(c.number);
                setTab("keypad");
              }}
              className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-left hover:bg-elevated"
            >
              <span className="flex size-11 shrink-0 items-center justify-center rounded-full bg-key text-sm font-medium text-fg">
                {c.name ? initials(c.name) : "#"}
              </span>
              <span className="min-w-0 flex-1">
                <span className={cn("block truncate text-sm font-medium", c.missed ? "text-hangup" : "text-fg")}>
                  {c.name ?? formatPhoneDisplay(c.number)}
                </span>
                <span className="mt-0.5 flex items-center gap-1 text-xs text-muted">
                  {c.missed ? <PhoneMissed className="size-3" /> : c.direction === "incoming" ? <ArrowDownLeft className="size-3" /> : <ArrowUpRight className="size-3" />}
                  {c.missed ? "Missed" : c.direction === "incoming" ? "Incoming" : "Outgoing"}
                  {c.durationMs > 0 ? ` · ${formatDuration(c.durationMs)}` : null}
                  {` · ${formatDayTime(c.at)}`}
                </span>
              </span>
              <span
                role="button"
                tabIndex={0}
                aria-label={`Call ${c.name ?? c.number}`}
                onClick={(e) => {
                  e.stopPropagation();
                  placeCall(c.number);
                }}
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    e.stopPropagation();
                    placeCall(c.number);
                  }
                }}
                className="flex size-10 items-center justify-center rounded-full text-call hover:bg-call/10"
              >
                <Phone className="size-4" />
              </span>
            </button>
          ))
        )}
      </div>
    </div>
  );
}
