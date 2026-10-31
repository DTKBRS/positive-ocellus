# Validation — text-only plugin

- Java 11 / RuneLite 1.13.4 compilation and JAR build.
- Tests cover case/markup/ellipsis normalization, word boundaries, phrase matching, the exact enrage cue, disabled and unknown text, persistence, invalid settings, and the 19 default entries.
- Event-handler tests use NPC/player proxies to verify Ocellus replacement, recursion protection, other NPC/player exclusion, unknown dialogue preservation, and inactive behavior.
- No audio events, playback classes, recordings, or speech generator remain in this version.

A live Ocellus encounter and Plugin Hub review remain pending. Unit tests do not establish in-game readability or final compatibility with every callout variant.
