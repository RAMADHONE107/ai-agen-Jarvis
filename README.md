# PROJECT JARVIS — Mobile AI Agent

Android-first personal assistant project, developed from an Android phone using Acode.

## Current milestone: v0.2.0 — local allowlisted app launcher

- Native Android app written in Kotlin.
- Local command parser supports opening Chrome, YouTube, WhatsApp, Google Maps, Gmail, and Android Accessibility Settings.
- Accessibility Service is registered but intentionally does not inspect screen content or perform clicks, gestures, or text entry.
- Unknown commands are rejected; no arbitrary shell commands or actions are executed.
- Unit tests cover basic parser behavior.
- GitHub Actions workflow runs unit tests and builds a debug APK.

## Build

The GitHub Actions workflow uses JDK 17, Android SDK 35, and Gradle 8.9. Run it from the Actions tab with Build JARVIS Debug APK or push to main.

Artifact name: jarvis-mobile-agent-debug.

## Open in Acode

Clone or download this repository, extract it, and open the project folder in Acode. Acode is used to edit source; APK compilation is handled by GitHub Actions.

## Safety boundaries

This milestone only launches a small allowlist of installed apps. It does not read screen contents, tap UI elements, type text, send messages, make payments, or delete data. Accessibility permissions are not enabled automatically. Any future high-impact action must require explicit user approval and post-action verification.

## Next milestones

1. Verify CI build and download/install the debug APK on the target phone.
2. Add a pause/stop control and visible action log.
3. Add explicitly approved Accessibility actions with target matching and result verification.
4. Add voice input/output.
5. Add a replaceable AI planner endpoint only after approval and secret-handling design.
