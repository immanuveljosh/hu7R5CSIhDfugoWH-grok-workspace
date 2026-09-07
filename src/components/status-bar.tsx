import { Signal, Wifi, Battery } from "lucide-react";
import { formatClock } from "@/lib/dialer/format";
import { useEffect, useState } from "react";

export function StatusBar() {
  const [now, setNow] = useState(() => new Date());
  useEffect(() => {
    const id = window.setInterval(() => setNow(new Date()), 15_000);
    return () => window.clearInterval(id);
  }, []);
  return (
    <div className="flex items-center justify-between px-5 pt-3 pb-1 text-xs font-medium tabular-nums text-fg">
      <span>{formatClock(now)}</span>
      <div className="flex items-center gap-1.5 text-fg">
        <Signal className="size-3.5" strokeWidth={2} />
        <Wifi className="size-3.5" strokeWidth={2} />
        <Battery className="size-3.5" strokeWidth={2} />
      </div>
    </div>
  );
}
