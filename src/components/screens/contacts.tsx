import { Phone, Search } from "lucide-react";
import { useMemo, useState } from "react";
import { CONTACTS } from "@/lib/dialer/seed";
import { formatPhoneDisplay, initials } from "@/lib/dialer/format";
import { useDialer } from "@/lib/dialer/store";

export function ContactsScreen() {
  const placeCall = useDialer((s) => s.placeCall);
  const [q, setQ] = useState("");
  const list = useMemo(() => {
    const query = q.trim().toLowerCase();
    const src = [...CONTACTS].sort((a, b) => a.name.localeCompare(b.name));
    if (!query) return src;
    return src.filter(
      (c) =>
        c.name.toLowerCase().includes(query) ||
        c.number.includes(query) ||
        (c.company ?? "").toLowerCase().includes(query),
    );
  }, [q]);

  return (
    <div className="flex h-full flex-col">
      <div className="px-5 pt-2 pb-3">
        <p className="text-sm font-medium text-muted">Contacts</p>
        <h1 className="mt-1 text-2xl font-semibold tracking-tight">People</h1>
        <label className="mt-3 flex items-center gap-2 rounded-md bg-elevated px-3 py-2">
          <Search className="size-4 text-muted" />
          <input
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Search name or number"
            className="w-full bg-transparent text-sm text-fg outline-none placeholder:text-subtle"
          />
        </label>
      </div>
      <div className="min-h-0 flex-1 overflow-y-auto px-2 pb-2">
        {list.map((c) => (
          <div key={c.id} className="flex items-center gap-3 rounded-lg px-3 py-2.5">
            <span className="flex size-11 shrink-0 items-center justify-center rounded-full bg-key text-sm font-medium">{initials(c.name)}</span>
            <span className="min-w-0 flex-1">
              <span className="block truncate text-sm font-medium text-fg">{c.name}</span>
              <span className="block truncate text-xs text-muted">
                {formatPhoneDisplay(c.number)}
                {c.company ? ` · ${c.company}` : ""}
              </span>
            </span>
            <button type="button" aria-label={`Call ${c.name}`} onClick={() => placeCall(c.number)} className="flex size-10 items-center justify-center rounded-full text-call hover:bg-call/10">
              <Phone className="size-4" />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
