# Positive Ocellus

![Positive Ocellus](assets/positive-ocellus.png)

A RuneLite plugin that changes **the text above Ocellus's head** in The Fractured Archive into encouraging dialogue. His audio and combat mechanics stay unchanged.

19 combat callouts have built-in positive replacements; 18 are enabled by default. **Insolence…** stays unchanged so its attack cue remains recognizable. Examples:

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

Enable **Positive Ocellus** and open its sidebar tab. Each dialogue line has editable positive text, an enable switch, and matching words. Click **Save changes**; the next matching overhead line uses your text. **Add dialogue line** supports new callouts. No sound IDs, recordings, or audio setup are needed.

Case, punctuation, and ellipses are ignored when matching. Keywords match whole words; the short enrage cue uses exact matching. Only NPCs named Ocellus are affected. Unknown lines and disabled replacements remain unchanged.

## Settings and updates

Your text lives in `~/.runelite/positive-ocellus/text-pack.json`. Changes persist across client restarts and plugin updates. **Reload saved text** reloads that file. Invalid files leave the last working pack active. Replacement text is limited to 80 plain-text characters.

The bundled JSON is the first-run default. New default entries can be added manually with **Add dialogue line**, or merged into the local JSON. User customizations are never overwritten automatically. The plugin does not download settings or code at runtime.

[Replacement texts](docs/text-lines.md)

## Validation and Plugin Hub

Tests cover text matching, disabled and unknown cues, settings validation and persistence, and Ocellus-only event handling. A live encounter check is still needed to verify every phase, readable attack cues, and save/reload behavior in-game.

Before Plugin Hub submission, run `bash gradlew clean test build` against the current RuneLite release and verify CI. Follow the [official Plugin Hub instructions](https://github.com/runelite/plugin-hub/blob/master/README.md) to submit a manifest pointing to a verified commit of this repository.

## Credits

Unofficial fan project; not affiliated with Jagex or RuneLite. Ocellus and Old School RuneScape belong to Jagex. Combat cue research: [OSRS Wiki transcript](https://oldschool.runescape.wiki/w/Transcript:Ocellus). Descriptions in the catalog are paraphrases.

Fan artwork was generated with OpenAI ImageGen on 2026-10-31 using the [Ocellus game render](https://oldschool.runescape.wiki/w/File:Ocellus.png) as an identity reference; the two icons are resized derivatives. Code is BSD-2-Clause; third-party characters and marks remain their owners' property. Gradle wrapper files originate from [RuneLite's example plugin](https://github.com/runelite/example-plugin); Gradle is Apache-2.0 licensed.
