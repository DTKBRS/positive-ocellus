# Plugin Hub submission

The [official Plugin Hub instructions](https://github.com/runelite/plugin-hub/blob/master/README.md) use two repositories:

1. `DTKBRS/positive-ocellus` contains the plugin source, tests, assets, and build files.
2. A fork of `runelite/plugin-hub` contains a manifest pointing to a reviewed commit of that source repository.

This is a text-only plugin. It changes Ocellus's overhead dialogue through `OverheadTextChanged` and `Actor.setOverheadText`. It does not change or consume sound events, automate actions, or provide information before the original dialogue appears.

## Before submission

- [ ] Run a live encounter covering all phases and confirm replacement text appears above Ocellus.
- [ ] Confirm disabling one line preserves its original text and disabling the plugin stops replacements.
- [ ] Confirm other NPCs and players remain unchanged.
- [ ] Check readability and recognition of combat cues with the replacement wording.
- [ ] Confirm settings persist after restart and save/reload works during the encounter.
- [ ] Run `bash gradlew clean test build` against the current RuneLite release and verify GitHub CI.

## Submit

Fork `runelite/plugin-hub`, create a branch, and add `plugins/positive-ocellus`:

```properties
repository=https://github.com/DTKBRS/positive-ocellus.git
commit=FULL_VERIFIED_COMMIT_SHA
```

Open a PR with that manifest change after live verification. RuneLite maintainers control acceptance. Later updates change the manifest commit SHA through another PR. The source repository is not a fork of Plugin Hub.

The plugin uses the standard build, RuneLite's transitive Gson dependency, classpath-loaded resources, and a 48×48 icon. No voice clips or speech-generation dependencies are included.
