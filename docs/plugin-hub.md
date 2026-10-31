# Plugin Hub release checklist

This is a development preview. Do not submit the initial unmapped pack as a working replacement plugin.

## Before submission

- [ ] Capture every supported combat voice line in a live encounter.
- [ ] Fill verified `soundId` and `channel` fields in the bundled pack; record game revision and evidence below.
- [ ] Confirm whether any voice lines use unsupported streamed/music playback.
- [ ] Confirm replacement timing, including sound-event delay and any intrinsic silence in original samples.
- [ ] Check all phases, wipe/re-entry, mute, volume, reload, and plugin shutdown.
- [ ] Listen to all replacement clips; preserve recognition of combat cues.
- [ ] Check invalid/missing WAV fallback and unrelated sounds near other bosses.
- [ ] Review final artwork and audio for public release.
- [ ] Run `bash gradlew clean test build` against the current RuneLite release.
- [ ] Publish the repository publicly and verify CI.

## Submit and update

Follow the [official Plugin Hub instructions](https://github.com/runelite/plugin-hub/blob/master/README.md).
Create a manifest named `plugins/positive-ocellus` in a branch of your plugin-hub fork:

```properties
repository=https://github.com/DTKBRS/positive-ocellus.git
commit=REPLACE_WITH_VERIFIED_FULL_COMMIT_SHA
```

Only use that URL after the public repository exists. Open a PR to `runelite/plugin-hub` when the above checks are complete. Review/acceptance is controlled by RuneLite maintainers. Later releases update the manifest's full commit SHA through another PR; the plugin never fetches executable updates itself.

`icon.png` is 48×48 pixels. Resources are loaded as classpath streams, so they work in distributed JARs. There are no new runtime dependencies beyond RuneLite's transitive dependencies. The speech generator is developer tooling only.

## Mapping evidence

No IDs verified yet.

| Cue key | Sound ID | Channel | Game revision | Evidence / repeat test |
|---|---|---|---|---|
