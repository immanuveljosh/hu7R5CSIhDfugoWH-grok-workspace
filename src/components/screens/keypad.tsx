import { Delete, Phone, UserPlus } from "lucide-react";
import { playDtmf } from "@/lib/dialer/audio";
import { formatPhoneDisplay } from "@/lib/dialer/format";
import { useDialer } from "@/lib/dialer/store";
import { cn } from "@/lib/utils";

const KEYS: Array<{ digit: string; letters: string }> = [
  { digit: "1", letters: "" }, { digit: "2", letters: "ABC" }, { digit: "3", letters: "DEF" },
  { digit: "4", letters: "GHI" }, { digit: "5", letters: "JKL" }, { digit: "6", letters: "MNO" },
  { digit: "7", letters: "PQRS" }, { digit: "8", letters: "TUV" }, { digit: "9", letters: "WXYZ" },
  { digit: "*", letters: "" }, { digit: "0", letters: "+" }, { digit: "#", letters: "" },
];

export function KeypadScreen() {
  const dial = useDialer((s) => s.dial);
  const appendDial = useDialer((s) => s.appendDial);
  const backspace = useDialer((s) => s.backspace);
  const placeCall = useDialer((s) => s.placeCall);
  const recordingOn = useDialer((s) => s.settings.autoRecord);

  const onKey = (digit: string) => {
    playDtmf(digit);
    appendDial(digit);
  };

  return (
    <div className="flex h-full flex-col px-5 pb-3 pt-2">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-muted">Keypad</p>
        {recordingOn ? (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-hangup/15 px-2.5 py-1 text-[11px] font-medium text-hangup">
            <span className="rec-dot size-1.5 rounded-full bg-rec" />
            Auto-record
          </span>
        ) : null}
      </div>
      <div className="flex min-h-20 flex-1 flex-col items-center justify-end pb-3">
        <p className={cn("min-h-12 w-full text-center font-mono font-medium tracking-wide text-fg", dial.length > 12 ? "text-2xl" : "text-3xl")}>
          {formatPhoneDisplay(dial) || <span className="text-subtle">Enter number</span>}
        </p>
      </div>
      <div className="mx-auto grid w-full max-w-xs grid-cols-3 gap-x-4 gap-y-3">
        {KEYS.map((k) => (
          <button
            key={k.digit}
            type="button"
            aria-label={k.digit === "*" ? "star" : k.digit === "#" ? "pound" : `digit ${k.digit}`}
            onClick={() => onKey(k.digit)}
            onContextMenu={(e) => {
              if (k.digit === "0") {
                e.preventDefault();
                playDtmf("0");
                appendDial("+");
              }
            }}
            className="flex size-[4.5rem] flex-col items-center justify-center rounded-full bg-key text-fg transition-colors duration-150 hover:bg-key-press active:scale-95"
          >
            <span className="font-mono text-[1.7rem] leading-none">{k.digit}</span>
            <span className="mt-1 h-3 text-[10px] font-medium tracking-[0.18em] text-muted">{k.letters || "\u00a0"}</span>
          </button>
        ))}
      </div>
      <div className="mx-auto mt-5 flex w-full max-w-xs items-center justify-between px-4">
        <button type="button" aria-label="Add to contacts" className="flex size-12 items-center justify-center rounded-full text-muted hover:bg-elevated hover:text-fg">
          <UserPlus className="size-5" />
        </button>
        <button
          type="button"
          aria-label="Call"
          onClick={() => placeCall()}
          className="flex size-16 items-center justify-center rounded-full bg-call text-call-fg shadow-[0_8px_24px_-8px_rgba(61,207,138,0.7)] transition-transform duration-150 active:scale-95"
        >
          <Phone className="size-7" fill="currentColor" />
        </button>
        <button
          type="button"
          aria-label="Delete digit"
          onClick={backspace}
          className="flex size-12 items-center justify-center rounded-full text-muted hover:bg-elevated hover:text-fg disabled:opacity-30"
          disabled={!dial}
        >
          <Delete className="size-5" />
        </button>
      </div>
    </div>
  );
}
