import { createFileRoute } from "@tanstack/react-router";
import type { ReactNode } from "react";
import { DialerApp } from "@/components/dialer-app";
import { HardDrive, Mic, Phone, Shield } from "lucide-react";

export const Route = createFileRoute("/")({ component: Home });

function Home() {
  return (
    <main className="min-h-dvh bg-bg text-fg">
      <div className="mx-auto flex max-w-6xl flex-col items-center gap-8 px-4 py-4 md:flex-row md:items-center md:justify-center md:gap-16 md:py-6">
        <aside className="hidden w-full max-w-md md:block">
          <p className="text-sm font-medium tracking-[0.18em] text-muted uppercase">Android dialer</p>
          <h1 className="mt-3 text-5xl font-semibold tracking-tight">Pulse</h1>
          <p className="mt-4 max-w-sm text-base leading-7 text-muted">
            A lightweight Kotlin dialer with local call recording. Recordings never leave the phone.
          </p>
          <ul className="mt-8 space-y-4">
            <Feature icon={<Phone className="size-4" />} title="Default phone app" body="InCallService, incoming and outgoing screens, recents and contacts." />
            <Feature icon={<Mic className="size-4" />} title="Honest recording" body="Probes VOICE_CALL, then communication, then mic. Never bypasses Android." />
            <Feature icon={<HardDrive className="size-4" />} title="App-private files" body="Call_2026-09-07_11-30-45.m4a plus Room metadata. Play, seek, delete." />
            <Feature icon={<Shield className="size-4" />} title="Privacy notice" body="Auto-record is off until you enable it. Consent is your responsibility." />
          </ul>
          <p className="mt-8 text-sm text-subtle">Open Settings, tap Auto call recording, then place a call.</p>
        </aside>
        <DialerApp />
      </div>
    </main>
  );
}

function Feature({ icon, title, body }: { icon: ReactNode; title: string; body: string }) {
  return (
    <li className="flex gap-3">
      <span className="mt-0.5 flex size-8 shrink-0 items-center justify-center rounded-md bg-elevated text-call">{icon}</span>
      <span>
        <span className="block text-sm font-medium">{title}</span>
        <span className="mt-0.5 block text-sm leading-6 text-muted">{body}</span>
      </span>
    </li>
  );
}
