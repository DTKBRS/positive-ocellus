# Positive Ocellus

A RuneLite plugin that changes **the text above Ocellus's head** in The Fractured Archive into encouraging dialogue. His audio and combat mechanics stay unchanged.

18 combat callouts have fixed positive replacements. Insolence keeps its original callout. Examples:

- Worth taunt → **You are worthy.**
- Failure taunt → **You are getting stronger.**
- Enrage callout → **Let's finish strong!**

**Development version — not yet on the Plugin Hub.** Matching and event behavior are tested; a live encounter check is still needed before submission.

## Run

Install a Java 11 JDK (a runtime alone cannot build the plugin), then:

```sh
bash gradlew test build
bash gradlew run
```

On Windows, use `.\gradlew.bat test build` and `.\gradlew.bat run` in PowerShell.

Enable **Positive Ocellus** in the plugin list. Its fixed replacements apply automatically, with no sidebar or setup. Disable the plugin to restore original dialogue. Insolence always keeps its original text. Audio stays unchanged.

Case, punctuation, and ellipses are ignored when matching. Keywords match whole words; the short enrage cue uses exact matching. Only NPCs named Ocellus are affected. Unknown lines remain unchanged.

## Settings and updates

Wording and matching rules are bundled with the plugin and update with it. No separate settings file is read or written. Files saved by older versions are ignored.

[Replacement texts](docs/text-lines.md)

## Validation and Plugin Hub

Tests cover text matching, bundled dialogue validation, unknown cues, plugin enable/disable behavior, and Ocellus-only event handling. A live encounter check is still needed to verify every phase and readable attack cues.

Before Plugin Hub submission, run `bash gradlew clean test build` against the current RuneLite release and verify CI. Follow the [official Plugin Hub instructions](https://github.com/runelite/plugin-hub/blob/master/README.md) to submit a manifest pointing to a verified commit of this repository.

## Credits

Unofficial fan project; not affiliated with Jagex or RuneLite. Ocellus and Old School RuneScape belong to Jagex. Combat cue research: [OSRS Wiki transcript](https://oldschool.runescape.wiki/w/Transcript:Ocellus). Descriptions in the catalog are paraphrases.

Code is BSD-2-Clause; third-party characters and marks remain their owners' property. Gradle wrapper files originate from [RuneLite's example plugin](https://github.com/runelite/example-plugin); Gradle is Apache-2.0 licensed.
