import { useEffect, useState } from "react";
import { Clock3, Grid3x3, Settings, UserRound, Waves } from "lucide-react";
import { PhoneFrame } from "@/components/phone-frame";
import { StatusBar } from "@/components/status-bar";
import { KeypadScreen } from "@/components/screens/keypad";
import { RecentsScreen } from "@/components/screens/recents";
import { ContactsScreen } from "@/components/screens/contacts";
import { RecordingsScreen } from "@/components/screens/recordings";
import { SettingsScreen } from "@/components/screens/settings";
import { InCallScreen } from "@/components/screens/in-call";
import { IncomingCallScreen } from "@/components/screens/incoming";
import { DefaultDialerSheet, DeleteAllSheet, PermissionSheet, PrivacySheet, Toast } from "@/components/overlays";
import { useDialer } from "@/lib/dialer/store";
import type { TabId } from "@/lib/dialer/types";
import { cn } from "@/lib/utils";

const TABS: Array<{ id: TabId; label: string; icon: typeof Grid3x3 }> = [
  { id: "keypad", label: "Keypad", icon: Grid3x3 },
  { id: "recents", label: "Recents", icon: Clock3 },
  { id: "contacts", label: "Contacts", icon: UserRound },
  { id: "recordings", label: "Files", icon: Waves },
];

export function DialerApp() {
  const tab = useDialer((s) => s.tab);
  const setTab = useDialer((s) => s.setTab);
  const call = useDialer((s) => s.call);
  const toast = useDialer((s) => s.toast);
  const setToast = useDialer((s) => s.setToast);
  const loadSeeds = useDialer((s) => s.loadSeeds);
  const autoRecord = useDialer((s) => s.settings.autoRecord);
  const [showSettings, setShowSettings] = useState(false);

  useEffect(() => {
    let cancelled = false;
    void (async () => {
      await useDialer.persist.rehydrate();
      if (cancelled) return;
      useDialer.setState({ hydrated: true });
      await loadSeeds();
    })();
    return () => {
      cancelled = true;
    };
  }, [loadSeeds]);

  useEffect(() => {
    if (!toast) return;
    const id = window.setTimeout(() => setToast(null), 3200);
    return () => window.clearTimeout(id);
  }, [toast, setToast]);

  const incoming = call?.state === "ringing";
  const inCall = Boolean(call && call.state !== "ringing");

  return (
    <PhoneFrame>
      {incoming ? (
        <IncomingCallScreen />
      ) : inCall ? (
        <InCallScreen />
      ) : (
        <>
          <StatusBar />
          <div className="relative flex items-center justify-end px-4">
            <button
              type="button"
              aria-label="Settings"
              onClick={() => setShowSettings((v) => !v)}
              className={cn("flex size-11 items-center justify-center rounded-full hover:bg-elevated", showSettings ? "text-call" : "text-muted hover:text-fg")}
            >
              <Settings className="size-5" />
            </button>
            {autoRecord ? <span className="rec-dot pointer-events-none absolute top-2 right-5 size-1.5 rounded-full bg-rec" /> : null}
          </div>
          <div className="min-h-0 flex-1">
            {showSettings ? <SettingsScreen /> : null}
            {!showSettings && tab === "keypad" ? <KeypadScreen /> : null}
            {!showSettings && tab === "recents" ? <RecentsScreen /> : null}
            {!showSettings && tab === "contacts" ? <ContactsScreen /> : null}
            {!showSettings && tab === "recordings" ? <RecordingsScreen /> : null}
          </div>
          <nav className="grid grid-cols-4 border-t border-border px-1 pb-3 pt-1">
            {TABS.map((t) => {
              const Icon = t.icon;
              const active = !showSettings && tab === t.id;
              return (
                <button
                  key={t.id}
                  type="button"
                  onClick={() => {
                    setShowSettings(false);
                    setTab(t.id);
                  }}
                  className={cn("flex flex-col items-center gap-1 rounded-md py-2 text-[11px] font-medium", active ? "text-call" : "text-muted")}
                >
                  <Icon className="size-5" strokeWidth={active ? 2.2 : 1.8} />
                  {t.label}
                </button>
              );
            })}
          </nav>
        </>
      )}
      <PrivacySheet />
      <PermissionSheet />
      <DefaultDialerSheet />
      <DeleteAllSheet />
      <Toast />
    </PhoneFrame>
  );
}
