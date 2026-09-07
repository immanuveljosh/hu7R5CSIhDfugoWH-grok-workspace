const DTMF: Record<string, [number, number]> = {
  "1": [697, 1209], "2": [697, 1336], "3": [697, 1477],
  "4": [770, 1209], "5": [770, 1336], "6": [770, 1477],
  "7": [852, 1209], "8": [852, 1336], "9": [852, 1477],
  "*": [941, 1209], "0": [941, 1336], "#": [941, 1477],
};

let audioCtx: AudioContext | null = null;

function ctx(): AudioContext | null {
  if (typeof window === "undefined") return null;
  if (!audioCtx) {
    const Ctor = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
    audioCtx = new Ctor();
  }
  return audioCtx;
}

export function playDtmf(key: string, durationMs = 110): void {
  const pair = DTMF[key];
  const ac = ctx();
  if (!pair || !ac) return;
  void ac.resume();
  const now = ac.currentTime;
  const gain = ac.createGain();
  gain.gain.setValueAtTime(0.0001, now);
  gain.gain.exponentialRampToValueAtTime(0.08, now + 0.01);
  gain.gain.exponentialRampToValueAtTime(0.0001, now + durationMs / 1000);
  gain.connect(ac.destination);
  for (const freq of pair) {
    const osc = ac.createOscillator();
    osc.type = "sine";
    osc.frequency.value = freq;
    osc.connect(gain);
    osc.start(now);
    osc.stop(now + durationMs / 1000 + 0.02);
  }
}

function writeWav(samples: Float32Array, sampleRate: number): Blob {
  const bytesPerSample = 2;
  const buffer = new ArrayBuffer(44 + samples.length * bytesPerSample);
  const view = new DataView(buffer);
  const ascii = (offset: number, text: string) => {
    for (let i = 0; i < text.length; i++) view.setUint8(offset + i, text.charCodeAt(i));
  };
  ascii(0, "RIFF");
  view.setUint32(4, 36 + samples.length * bytesPerSample, true);
  ascii(8, "WAVE");
  ascii(12, "fmt ");
  view.setUint32(16, 16, true);
  view.setUint16(20, 1, true);
  view.setUint16(22, 1, true);
  view.setUint32(24, sampleRate, true);
  view.setUint32(28, sampleRate * bytesPerSample, true);
  view.setUint16(32, bytesPerSample, true);
  view.setUint16(34, 16, true);
  ascii(36, "data");
  view.setUint32(40, samples.length * bytesPerSample, true);
  let offset = 44;
  for (let i = 0; i < samples.length; i++) {
    const s = Math.max(-1, Math.min(1, samples[i]));
    view.setInt16(offset, s < 0 ? s * 0x8000 : s * 0x7fff, true);
    offset += 2;
  }
  return new Blob([buffer], { type: "audio/wav" });
}

export function synthesizeCallAudio(durationMs: number, seed = 1): { blob: Blob; dataUrl: string; bytes: number } {
  const sampleRate = 16000;
  const n = Math.max(1, Math.floor((durationMs / 1000) * sampleRate));
  const samples = new Float32Array(n);
  let rand = seed * 997 + 13;
  const next = () => {
    rand = (rand * 16807) % 2147483647;
    return rand / 2147483647;
  };
  for (let i = 0; i < n; i++) {
    const t = i / sampleRate;
    const env = Math.min(1, t * 8) * Math.min(1, (n - i) / (sampleRate * 0.12));
    const near = Math.sin(2 * Math.PI * 220 * t) * 0.18;
    const talk = Math.sin(2 * Math.PI * (180 + 40 * Math.sin(2 * Math.PI * 2.2 * t)) * t) * 0.12;
    const noise = (next() * 2 - 1) * 0.02;
    samples[i] = (near + talk + noise) * env;
  }
  const blob = writeWav(samples, sampleRate);
  return { blob, dataUrl: URL.createObjectURL(blob), bytes: blob.size };
}

export async function blobToDataUrl(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = () => reject(reader.error);
    reader.readAsDataURL(blob);
  });
}
