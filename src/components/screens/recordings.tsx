import { Pause, Play, Trash2 } from "lucide-react";
import { useEffect, useRef } from "react";
import { formatBytes, formatDayTime, formatDuration, formatPhoneDisplay } from "@/lib/dialer/format";
import { useDialer } from "@/lib/dialer/store";
import { cn } from "@/lib/utils";

export function RecordingsScreen() {
  const recordings = useDialer((s) => s.recordings);
  const player = useDialer((s) => s.player);
  const setPlayer = useDialer((s) => s.setPlayer);
  const deleteRecording = useDialer((s) => s.deleteRecording);
  const setConfirmDeleteAll = useDialer((s) => s.setConfirmDeleteAll);
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const active = recordings.find((r) => r.id === player.recordingId) ?? null;

  useEffect(() => {
    const el = audioRef.current;
    if (!el) return;
    if (!active) {
      el.pause();
      return;
    }
    if (el.src !== active.audioDataUrl) el.src = active.audioDataUrl;
    if (player.playing) void el.play();
    else el.pause();
  }, [active, player.playing, player.recordingId]);

  return (
    <div className="flex h-full flex-col">
      <audio
        ref={audioRef}
        className="hidden"
        onTimeUpdate={(e) => {
          const el = e.currentTarget;
          if (!el.duration) return;
          setPlayer({ progress: el.currentTime / el.duration });
        }}
        onEnded={() => setPlayer({ playing: false, progress: 0 })}
      />
      <div className="flex items-end justify-between px-5 pt-2 pb-3">
        <div>
          <p className="text-sm font-medium text-muted">Recordings</p>
          <h1 className="mt-1 text-2xl font-semibold tracking-tight">On this device</h1>
        </div>
        {recordings.length > 0 ? (
          <button type="button" className="text-xs font-medium text-hangup" onClick={() => setConfirmDeleteAll(true)}>
            Delete all
          </button>
        ) : null}
      </div>
      <div className="min-h-0 flex-1 overflow-y-auto px-2 pb-2">
        {recordings.length === 0 ? (
          <div className="px-4 py-16 text-center">
            <p className="text-sm font-medium text-fg">No recordings yet</p>
            <p className="mt-2 text-sm text-muted">Turn on auto-record in Settings, then place a call. Files stay on this device.</p>
          </div>
        ) : (
          recordings.map((r) => {
            const isActive = player.recordingId === r.id;
            return (
              <div key={r.id} className="rounded-lg px-3 py-3 hover:bg-elevated">
                <div className="flex items-start gap-3">
                  <button
                    type="button"
                    aria-label={isActive && player.playing ? "Pause" : "Play"}
                    onClick={() => {
                      if (isActive) setPlayer({ playing: !player.playing });
                      else setPlayer({ recordingId: r.id, playing: true, progress: 0 });
                    }}
                    className="mt-0.5 flex size-10 shrink-0 items-center justify-center rounded-full bg-key text-fg"
                  >
                    {isActive && player.playing ? <Pause className="size-4" /> : <Play className="size-4" />}
                  </button>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium">{r.name ?? formatPhoneDisplay(r.number)}</p>
                    <p className="mt-0.5 text-xs text-muted">
                      {r.direction === "incoming" ? "Incoming" : "Outgoing"} · {formatDayTime(r.startedAt)} · {formatDuration(r.durationMs)}
                    </p>
                    <p className="mt-0.5 truncate font-mono text-[10px] text-subtle">{r.filename} · {formatBytes(r.fileSize)}</p>
                    {isActive ? (
                      <input
                        type="range"
                        min={0}
                        max={1000}
                        value={Math.round(player.progress * 1000)}
                        onChange={(e) => {
                          const el = audioRef.current;
                          const p = Number(e.target.value) / 1000;
                          if (el && el.duration) el.currentTime = p * el.duration;
                          setPlayer({ progress: p });
                        }}
                        className="mt-2 h-1 w-full cursor-pointer accent-call"
                        aria-label="Seek"
                      />
                    ) : null}
                  </div>
                  <button type="button" aria-label="Delete recording" onClick={() => deleteRecording(r.id)} className="flex size-9 items-center justify-center rounded-full text-muted hover:bg-hangup/10 hover:text-hangup">
                    <Trash2 className="size-4" />
                  </button>
                </div>
              </div>
            );
          })
        )}
      </div>
      <p className={cn("px-5 pb-2 text-[11px] text-subtle", recordings.length === 0 && "hidden")}>Stored locally. Never synced.</p>
    </div>
  );
}
