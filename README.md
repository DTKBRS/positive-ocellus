# Positive Ocellus

![Positive Ocellus](assets/positive-ocellus.png)

A RuneLite plugin that changes **the text above Ocellus's head** in The Fractured Archive into encouraging dialogue. His audio and combat mechanics stay unchanged.

19 combat callouts have built-in positive replacements, enabled immediately. Examples:

- Worth taunt → **You are worthy.**
- Failure taunt → **You are getting stronger.**
- Enrage callout → **Finish strong.**

**Development version — not yet on the Plugin Hub.** Matching and event behavior are tested; a live encounter check is still needed before submission.

## Run

Install Java 11, then:

```sh
bash gradlew test build
bash gradlew run
```

Enable **Positive Ocellus** and open its sidebar tab. Each dialogue line has editable positive text, an enable switch, and matching words. Click **Save changes**; the next matching overhead line uses your text. **Add dialogue line** supports new callouts. No sound IDs, recordings, or audio setup are needed.

Case, punctuation, and ellipses are ignored when matching. Keywords match whole words; the short enrage cue uses exact matching. Only NPCs named Ocellus are affected. Unknown lines and disabled replacements remain unchanged.

## Settings and updates

Your text lives in `~/.runelite/positive-ocellus/text-pack.json`. Changes persist across client restarts and plugin updates. **Reload saved text** reloads that file. Invalid files leave the last working pack active. Replacement text is limited to 80 plain-text characters.

The bundled JSON is the first-run default. New default entries can be added manually with **Add dialogue line**, or merged into the local JSON. User customizations are never overwritten automatically. The plugin does not download settings or code at runtime.

[Dialogue catalog](docs/text-lines.md) · [Plugin Hub submission guide](docs/plugin-hub.md) · [Validation](docs/validation.md)

## Credits

Unofficial fan project; not affiliated with Jagex or RuneLite. Ocellus and Old School RuneScape belong to Jagex. Combat cue research: [OSRS Wiki transcript](https://oldschool.runescape.wiki/w/Transcript:Ocellus). Descriptions in the catalog are paraphrases.

Artwork was generated with OpenAI ImageGen using the Ocellus game render as an identity reference. See [asset provenance](docs/assets.md). Code is BSD-2-Clause; third-party characters and marks remain their owners' property.
