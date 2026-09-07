# Pulse

Lightweight Kotlin dialer for Android 10 (API 29) through the current SDK. Recordings stay in app-private storage. Nothing is uploaded.

Open the `android/` folder in Android Studio (Ladybug / Koala or newer). Sync Gradle, then run the `app` configuration on a device or emulator with a telephony stack.

## What it includes

- Number keypad, recents, contacts, in-call UI, incoming UI
- Default-dialer role via `RoleManager.ROLE_DIALER` and `InCallService`
- Auto call recording with a capability probe and a hard stop when the device blocks it
- Room metadata + M4A files named `Call_YYYY-MM-DD_HH-mm-ss.m4a`
- Playback with Media3 ExoPlayer, delete, and delete-all
- Privacy notice and runtime permissions requested only when needed

`ConnectionService` is not registered. That API is for apps that *provide* calls (VoIP). Pulse is a default *dialer*: it controls SIM calls through `InCallService`.

## Call recording (official APIs only)

Pulse never uses accessibility, hidden APIs, or root.

On enable, `AudioSourceProbe` tries, in order:

1. `MediaRecorder.AudioSource.VOICE_CALL`
2. `MediaRecorder.AudioSource.VOICE_COMMUNICATION`
3. `MediaRecorder.AudioSource.MIC`

If none of those can start a recorder, Settings shows:

**Call recording is not supported on this device.**

On most phones from Android 10 onward, `VOICE_CALL` is reserved for the system/OEM dialer. The microphone fallback captures near-end audio only. Pulse labels that honestly. Auto-record stays off until you turn it on, and it never records after you turn it off.

You are responsible for two-party consent where the law requires it.

## Permissions

| Permission | Why |
|---|---|
| `CALL_PHONE`, `ANSWER_PHONE_CALLS`, `READ_PHONE_STATE`, `READ_PHONE_NUMBERS` | Place and observe calls |
| `READ_CALL_LOG`, `WRITE_CALL_LOG` | Recents (default dialer) |
| `READ_CONTACTS` | Names on the keypad and recents |
| `RECORD_AUDIO` | Call recording, only when you enable it |
| `FOREGROUND_SERVICE` / `_MICROPHONE` | Recording notification while a call is active |
| `POST_NOTIFICATIONS` | Android 13+ recording / incoming status |
| `USE_FULL_SCREEN_INTENT` | Incoming call over the lock screen |

No storage, location, SMS, or accessibility permissions.

## Tests

From this directory:

```
./gradlew test
```

If the wrapper JAR is missing (this tree is source-only), Android Studio will generate it on first sync, or run `gradle wrapper` with Gradle 8.9.
