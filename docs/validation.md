# Validation — 2026-10-31

- Java 11 / RuneLite 1.13.4: clean compilation and JAR build passed.
- 10 JUnit tests passed: pack persistence, duplicate mapping rejection, path traversal rejection, schema/channel validation, unmapped entries, audio fallback, shutdown behavior, all 19 bundled WAV formats/durations, and icon dimensions.
- Development client startup succeeded; RuneLite reported PositiveOcellusPlugin running and registered both sound handlers, overhead text, game tick and game-state listeners. No login or combat actions were performed.
- The real editor was rendered offscreen at the 225-pixel sidebar width and visually inspected; layout/contrast fixes were applied. Reproduce using `bash gradlew renderPreview`; output is `build/reports/voice-editor.png`. This is an offscreen Swing layout check, not an in-game screenshot.
- All 19 generated clips are mono, 16-bit PCM and approximately 1.2–3.3 seconds long. Format/duration validation does not replace a listening review.

Not verified: live Ocellus sound IDs, live suppression/timing, actual voice coverage, or Plugin Hub acceptance. Default IDs remain -1, so no in-game replacements are active yet. See `plugin-hub.md` for the release gates.
