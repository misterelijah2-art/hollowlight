# Hollowlight — Design Document

## 1. Core Concept & Name

**Title:** Hollowlight

**Elevator pitch:** Deep beneath the familiar caves of the Overworld lies the **Understratum**, a
corrupted rock layer that grew wrong around something that was never supposed to be buried in stone:
the **Hollow Star**, an ancient dormant intelligence that perceives rather than hunts. It doesn't chase
you across the map. It *counts* you — every second you spend seen by its servants or standing in its light
erodes your mind a little further. The central fear the mod is built around is **being perceived**:
visibility, not proximity, is the danger. Darkness and stillness are the only real safety, which inverts
the player's usual Minecraft instincts (torches everywhere, sprint through caves) into liabilities.

## 2. Lore & Tone

The Understratum has always been there, just below where diamonds run out — but nobody notices the
transition, because the stone lies about where it starts. Ancient expeditions occasionally broke through by
accident while mining deep, and none of their written accounts agree on how deep it actually was, because
the Hollow Star edits the memory of distance in anyone who has been counted too many times.

**Found-lore delivery:** *The Hollowtext, Vol. III* — a written book placed as loot in Understratum Chapel
structures, in the voice of the third (fictional) expedition's surviving surveyor. It explains the Watcher
("the tall ones... its own eyes, walked out on legs it grew for them"), the Stalker ("the low ones hunt by
ear and by flame-light"), and ends mid-sentence at the altar where the player finds it — implying the writer
did not leave. Full page text lives in `docs/lore/hollowtext_vol_3.json` and is delivered in-game via a
`minecraft:writable_book`-based loot entry in the Chapel loot table.

Tone target: quiet cosmic dread rather than jump-scare gore — closer to *Darkwood*'s "something is deciding
whether to notice you" tension and *SCP*'s procedural-anomaly logic than to visceral horror.

## 3. Progression Arc (Dread Escalation)

1. **Omens** (dread 0–15 → 15–45 threshold crossing): Ambient drone layers thicken, occasional far-off
   whisper stingers, first Resonant Lantern use warns the player their light is "borrowed." No mechanical
   punishment yet beyond a HUD vignette — this phase teaches the fiction.
2. **Stalking** (dread 45–75): Mining Fatigue applied once on threshold crossing (the world resists you
   physically), Stalkers begin actively investigating noise, hunt-event *chance checks* become live. Player
   should feel like something has started paying attention specifically to them.
3. **Confrontation / Escape** (dread 75–100): Blindness flicker, Nausea pulses, hunt events guaranteed to
   trend toward a full Convergence spawn, Watchers blink-step far more punishingly because the player is
   already fragile. The only two outs are surviving an active Convergence encounter or using an Effigy of
   Stillness to force a reset — both deliberately costly.

## 4. Feature List

### 4.1 Hostile Entities

**The Watcher** — a near-motionless line-of-sight predator. States: `IDLE → OBSERVING → BLINKING → DORMANT`.
It inflicts steady dread while seen, and "blink-steps" adjacent (leaving a 3–5 block gap, never landing on
top of the player) the instant sightline breaks within range, then goes dormant for 10 seconds as a mercy
window. This supports horror pacing by making *looking away* — the player's natural instinct — the actual
trigger for danger, forcing tense, deliberate movement instead of panic-fleeing.

**The Stalker** — a fast quadruped drawn by sound/light rather than sight. States:
`PATROL → STALK → LUNGE → RECOVER`. It paths to last-known noise locations rather than tracking the player
directly, circles when it arrives and finds nothing, and commits to a telegraphed wind-up → charge → recovery
lunge rather than a standard melee loop. This supports pacing by rewarding stealth (sneaking, staying dark,
not sprinting) with genuine safety, and by giving skilled players a real dodge window during lunges.

### 4.2 Dread/Sanity System

A 0–100 `dread` value stored via Fabric's **Data Attachment API** (`DreadAttachments`/`DreadManager`),
gained through Watcher sightlines, Stalker proximity, ritual mishaps, and lantern overuse; lost through
natural decay once unperceived, or partial resets on escaping a Convergence. Crossing thresholds
(15/45/75/85) applies real status effects (message cues, Mining Fatigue, Blindness, Nausea) and gates the
hunt-event trigger chance and Watcher/Stalker aggression tuning.

### 4.3 Dimension: The Understratum

A dedicated dimension (`hollowlight:understratum`) — no skylight, capped ambient light, sea level 32,
world height 0–128, deepslate-default terrain. Structures (Chapels, Bonewell Shafts) are spaced widely via
`structure_set` JSON so finding one feels rare. Vanilla hostile/passive spawns are cleared from its biome in
code (`ModWorldGen`) so every threat encountered is authored, not ambient.

### 4.4 Structures

**Understratum Chapel** — a one-way descending corridor structure ending in an altar flanked by three
braziers (see Ritual event). Environmental storytelling: cracked murals implying humanoid silhouettes
counting other silhouettes, a dead-end "false exit" corridor that loops back to the entrance (a vanilla-legal
trick using rotated structure pieces) to disorient explorers, and the Hollowtext book left at the altar.

**Bonewell Shaft** — a vertical shaft lined with Watcher alcoves at deliberate sightline intervals, forcing
players to time descents around when Watchers are likely to be OBSERVING.

### 4.5 Sound Design Plan

- **Ambient loop layers:** `ambient.drone_low` plays continuously at low volume in the Understratum;
  `ambient.drone_high` crossfades in once dread passes the Stalking threshold.
- **Stingers:** `ambient.whisper_stinger` fires probabilistically (2% per threshold-effect check) once dread
  ≥ 45; `ambient.heartbeat_stinger` fires exactly once per Tuning Fork use.
- **Entity cues:** `entity.watcher.detect` on OBSERVING entry; `entity.watcher.blink` twice per blink
  (vanish + reappear); `entity.stalker.snarl` on lunge wind-up and as ambient sound; `entity.stalker.lunge`
  on charge start and again louder on hit; `entity.stalker.footstep` on every step while STALK/LUNGE.
- **Event cues:** `event.hunt_horn` once at Omen phase start; `event.ritual_chant` on first brazier lit and
  again (pitched up) on ritual success; `event.ritual_failure` on ritual failure and Convergence failure.

### 4.6 Custom Items

- **Resonant Lantern** — toggle-lit tool granting Night Vision, but ticking dread every 30s of active light
  because "the light is borrowed." Crafted from a Starshard Fragment + lantern + glowstone.
- **Effigy of Stillness** — single-use panic button forcing nearby Watchers/Stalkers into forced
  recovery/slowness for 15s, at the cost of a 20-point dread spike. Found in Chapel loot.
- **Tuning Fork of the Hollow** — rings to reveal nearby threat count through walls for a window, but the
  ring itself is noise that can alert nearby Stalkers — information versus attention, a genuine gamble.
  Crafted from a Starshard Fragment + iron nuggets.
- **Starshard Fragment** — the common crafting ingredient tying all three together; acquired from rare
  Watcher kill drops (5%) or Chapel/ritual rewards, tying artifact power directly to risk taken.

### 4.7 Dynamic Event Systems

**The Convergence** (`HuntEventManager`) — ambient, world-driven. `DORMANT → OMEN → ACTIVE → RESOLVING`
state machine persisted via `PersistentState`, checked every 5 seconds, escalating probabilistically once a
player is in the Stalking dread range. Warning signal: distant horn. Failure: caught while critical dread.
Success: hunter defeated or 2-minute timeout survived, partial dread relief granted.

**The Binding Ritual** (`RitualEvent`) — player-triggered, opt-in. Lighting all three Chapel braziers within
20 seconds spawns a guaranteed Starshard Fragment reward; failing to light all three in time spawns a
punishing Stalker at the altar instead. Trades a steady dread cost for a reliable reward path.

### 4.8 Advancement Tree

`root (Descent) → omens (You Feel Watched) → stalking (Something Circles) → confrontation (The Convergence,
challenge-frame)` — a linear tree mirroring the three-phase dread arc exactly, so completing it is a readable
record of a player's escalation through the mod's intended experience.

### 4.9 Config Options

JSON config at `config/hollowlight.json` (`HollowlightConfig`) exposing: dread gain/decay multipliers, hunt
event frequency multiplier, whether hunts can trigger in daytime (Overworld use), and per-entity minimum
perception distances, plus a toggle to disable the screen vignette for accessibility.

## 5. Non-Goals / Explicit Scope Cuts (v0.1.0)

- No custom shader-based post-processing pipeline yet (vignette is implemented via flat HUD fill layers,
  not a true GLSL shader) — see roadmap.
- No dedicated block-entity-driven brazier/altar blocks yet; `RitualEvent` is implemented as a functional
  static evaluator ready to be wired to real block interactions once brazier blocks are modeled.
- No bespoke entity models/textures/animations — renderers reuse vanilla player/wolf skeletons as
  placeholders so the mobs are fully functional and visible today.
