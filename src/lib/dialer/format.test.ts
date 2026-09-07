import assert from "node:assert/strict";
import { describe, it } from "node:test";
import {
  formatRecordingFilename,
  isValidRecordingFilename,
  formatDuration,
  formatBytes,
  digitsOnly,
  formatPhoneDisplay,
  initials,
  resolveCapability,
} from "./format.ts";

describe("formatRecordingFilename", () => {
  it("matches Call_YYYY-MM-DD_HH-mm-ss.m4a", () => {
    const d = new Date(2026, 8, 7, 11, 30, 45);
    assert.equal(formatRecordingFilename(d), "Call_2026-09-07_11-30-45.m4a");
    assert.equal(isValidRecordingFilename("Call_2026-09-07_11-30-45.m4a"), true);
  });
});

describe("formatDuration", () => {
  it("formats mm:ss", () => {
    assert.equal(formatDuration(0), "0:00");
    assert.equal(formatDuration(65_000), "1:05");
  });
});

describe("phone helpers", () => {
  it("keeps dialable symbols", () => {
    assert.equal(digitsOnly("+91 (984) 001-1223"), "+919840011223");
  });
  it("formats numbers", () => {
    assert.equal(formatPhoneDisplay("+14155550192"), "+1 415 555 0192");
    assert.equal(initials("Priya Natarajan"), "PN");
  });
});

describe("formatBytes", () => {
  it("uses KB", () => {
    assert.equal(formatBytes(2048), "2.0 KB");
  });
});

describe("resolveCapability", () => {
  it("surfaces unsupported copy", () => {
    const cap = resolveCapability(true);
    assert.equal(cap.supported, false);
    if (!cap.supported) assert.equal(cap.reason, "Call recording is not supported on this device.");
  });
});
