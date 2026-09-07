import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

export function PhoneFrame({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <div className="w-full md:h-[726px] md:w-[335px] md:shrink-0">
      <div
        className={cn(
          "relative mx-auto w-full max-w-md overflow-hidden bg-bg text-fg",
          "min-h-dvh md:min-h-0 md:h-[844px] md:w-[390px] md:origin-top-left md:scale-[0.86] md:rounded-device md:border md:border-border-strong md:shadow-[0_40px_80px_-24px_rgba(0,0,0,0.7)]",
          className,
        )}
      >
        <div className="pointer-events-none absolute inset-x-0 top-0 z-20 hidden h-7 md:block">
          <div className="mx-auto mt-2 h-5 w-24 rounded-full bg-elevated" />
        </div>
        <div className="relative flex h-full min-h-dvh flex-col md:h-full md:min-h-0">{children}</div>
      </div>
    </div>
  );
}
