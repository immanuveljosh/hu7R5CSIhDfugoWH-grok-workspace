import { Shield } from "lucide-react";
import { useDialer } from "@/lib/dialer/store";
import { Button } from "@/components/ui/button";

export function PrivacySheet() {
  const show = useDialer((s) => s.showPrivacy);
  const acceptPrivacy = useDialer((s) => s.acceptPrivacy);
  if (!show) return null;
  return (
    <div className="absolute inset-0 z-40 flex items-end bg-bg/80 p-4 backdrop-blur-sm">
      <div className="w-full rounded-xl bg-surface p-5">
        <Shield className="size-6 text-call" />
        <h2 className="mt-3 text-xl font-semibold tracking-tight">Private by design</h2>
        <p className="mt-2 text-sm leading-6 text-muted">
          Pulse is a local dialer. Call recordings — if you enable them — are stored only in this app’s private files on the device. They are never uploaded.
        </p>
        <ul className="mt-3 space-y-1.5 text-sm leading-6 text-muted">
          <li>Auto-record is off until you turn it on.</li>
          <li>Two-way call audio is often blocked by Android. Pulse will say so instead of bypassing security.</li>
          <li>Recording laws vary. You are responsible for consent in your region.</li>
        </ul>
        <Button className="mt-5 w-full" onClick={acceptPrivacy}>Continue</Button>
      </div>
    </div>
  );
}

export function PermissionSheet() {
  const show = useDialer((s) => s.showPermissions);
  const grant = useDialer((s) => s.grantPermissions);
  if (!show) return null;
  return (
    <div className="absolute inset-0 z-40 flex items-end bg-bg/80 p-4 backdrop-blur-sm">
      <div className="w-full rounded-xl bg-surface p-5">
        <h2 className="text-xl font-semibold tracking-tight">Permissions Pulse needs</h2>
        <ul className="mt-3 space-y-2 text-sm leading-6 text-muted">
          <li><span className="font-medium text-fg">Phone</span> — place, answer, and show calls.</li>
          <li><span className="font-medium text-fg">Contacts</span> — match names to numbers.</li>
          <li><span className="font-medium text-fg">Microphone</span> — only if you enable call recording.</li>
          <li><span className="font-medium text-fg">Notifications</span> — incoming call and recording status.</li>
        </ul>
        <Button className="mt-5 w-full" onClick={grant}>Allow</Button>
      </div>
    </div>
  );
}

export function DefaultDialerSheet() {
  const show = useDialer((s) => s.showDefaultDialer);
  const isDefault = useDialer((s) => s.settings.isDefaultDialer);
  const setDefaultDialer = useDialer((s) => s.setDefaultDialer);
  const setShow = useDialer((s) => s.setShowDefaultDialer);
  if (!show) return null;
  return (
    <div
      className="absolute inset-0 z-40 flex items-end bg-bg/80 p-4 backdrop-blur-sm"
      onClick={() => setShow(false)}
    >
      <div className="w-full rounded-xl bg-surface p-5" onClick={(e) => e.stopPropagation()}>
        <h2 className="text-xl font-semibold tracking-tight">Default phone app</h2>
        <p className="mt-2 text-sm leading-6 text-muted">
          Android will ask you to set Pulse as the default dialer. That is required for in-call UI on a real phone.
        </p>
        <div className="mt-5 flex flex-col gap-2">
          <Button onClick={() => setDefaultDialer(!isDefault)}>{isDefault ? "Release default role" : "Use Pulse as default"}</Button>
          <Button variant="ghost" onClick={() => setShow(false)}>Not now</Button>
        </div>
      </div>
    </div>
  );
}

export function DeleteAllSheet() {
  const show = useDialer((s) => s.confirmDeleteAll);
  const setShow = useDialer((s) => s.setConfirmDeleteAll);
  const deleteAll = useDialer((s) => s.deleteAllRecordings);
  if (!show) return null;
  return (
    <div className="absolute inset-0 z-40 flex items-center justify-center bg-bg/80 p-6 backdrop-blur-sm">
      <div className="w-full rounded-xl bg-surface p-5">
        <h2 className="text-lg font-semibold">Delete all recordings?</h2>
        <p className="mt-2 text-sm leading-6 text-muted">This removes every local recording. It cannot be undone.</p>
        <div className="mt-5 flex gap-2">
          <Button variant="subtle" className="flex-1" onClick={() => setShow(false)}>Cancel</Button>
          <Button variant="hangup" className="flex-1" onClick={deleteAll}>Delete all</Button>
        </div>
      </div>
    </div>
  );
}

export function Toast() {
  const toast = useDialer((s) => s.toast);
  const setToast = useDialer((s) => s.setToast);
  if (!toast) return null;
  return (
    <button type="button" onClick={() => setToast(null)} className="absolute inset-x-4 bottom-24 z-30 rounded-lg bg-elevated px-3 py-2.5 text-left text-sm text-fg shadow-lg">
      {toast}
    </button>
  );
}
