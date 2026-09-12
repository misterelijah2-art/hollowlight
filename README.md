# Hollowlight

*Being seen is the danger.*

Hollowlight is an original horror mod for **Minecraft 1.21.1** on **Fabric**. Descend into the
**Understratum**, a corrupted rock layer grown wrong around a buried intelligence called the **Hollow Star**.
It doesn't hunt with claws. It hunts with attention.

Full design rationale — lore, progression arc, entity AI specs, sound design plan, and event system design —
lives in [`docs/DESIGN.md`](docs/DESIGN.md).

## Features

- **The Watcher** — a near-motionless entity whose entire threat is line-of-sight. Break eye contact near it
  and it blink-steps closer. Full custom AI state machine, no vanilla mob reuse.
- **The Stalker** — a fast, sound/light-drawn quadruped hunter with a telegraphed wind-up → charge → recovery
  lunge attack and last-known-position pursuit behavior.
- **Dread system** — a persistent 0–100 sanity stat (Fabric Data Attachment API) that gates real status
  effects, HUD distortion, and event escalation as it rises.
- **The Understratum** — a dedicated dimension: no skylight, capped light, deepslate terrain, exclusively
  authored spawns (no vanilla mob spawning).
- **Understratum Chapel & Bonewell Shaft** — structures with environmental storytelling, a false-exit loop,
  and found-lore via an in-world written book.
- **Three risk/reward artifacts** — Resonant Lantern, Effigy of Stillness, Tuning Fork of the Hollow — each
  with a real mechanical trade-off, not stat sticks.
- **Two dynamic events** — *The Convergence* (ambient world-driven hunt, persistent state machine) and
  *The Binding Ritual* (player-triggered, opt-in risk/reward sequence).
- **Advancement tree** mirroring the mod's Omens → Stalking → Confrontation dread arc.
- **JSON config** for tuning dread gain/decay, hunt frequency, and accessibility (vignette toggle).

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) `0.16.10` or newer for Minecraft `1.21.1`.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) `0.116.7+1.21.1` or newer.
3. Drop the built `hollowlight-<version>.jar` into your `mods/` folder.
4. Launch Minecraft `1.21.1` with the Fabric profile.

### Building from source

```
./gradlew build
```

The built jar will be in `build/libs/`. Run `./gradlew runClient` for a dev client, or
`./gradlew runDatagen` to regenerate recipes/loot tables/advancements/tags from the datagen providers in
`com.hollowlight.datagen`.

## Project Structure

```
src/main/java/com/hollowlight/
  Hollowlight.java          - main entrypoint, registry wiring order
  dread/                     - sanity system (DreadData, attachments, manager)
  config/                    - JSON-backed config
  sound/                     - SoundEvent registrations
  entity/                    - Watcher & Stalker entities + custom AI goals
  item/                      - custom artifacts
  world/                     - dimension/biome spawn wiring
  event/                     - HuntEventManager (Convergence) & RitualEvent (Binding Ritual)
  datagen/                   - Fabric Data Generation providers

src/client/java/com/hollowlight/
  client/                    - client entrypoint, renderers, dread HUD
  mixin/                     - client player mixin (dread sync landing point)

src/main/resources/          - fabric.mod.json, mixins config, dimension/biome/loot JSON
docs/                        - design document, in-world lore text
```

## Technical Notes / Version Decisions

- **Target:** Minecraft `1.21.1`, Fabric Loader `0.16.10`, Fabric API `0.116.7+1.21.1`, Yarn
  `1.21.1+build.3`, Fabric Loom `1.7.4`, Java 21.
- **Sanity/dread persistence** uses Fabric's **Data Attachment API** (`AttachmentRegistry`,
  `AttachmentType`) with a Codec-backed record, not NBT capability hacks — this is the current
  recommended pattern for 1.21.1+.
- **World-level event state** (`HuntEventManager`) uses `PersistentState` + `PersistentStateManager`,
  driven from `ServerTickEvents.START_SERVER_TICK` — no busy-wait loops, one cheap check per tick.
- **Entity AI** uses `GoalSelector`-based custom `Goal` subclasses (`StalkerStalkGoal`,
  `StalkerLungeGoal`). Brain-based AI (as used by Villagers/Piglins) was intentionally not used since it's
  designed for vanilla-integrated schedule/memory mobs; GoalSelector is the idiomatic choice for bespoke
  hostile mobs in 1.21.1.
- **Dread sync to client** is intentionally server-authoritative only in this version; the HUD reads a
  client-side cache (`ClientDreadState`) with a mixin landing point prepared for a follow-up S2C payload
  (see Roadmap).

## Roadmap

### Priority 1 — Art & Audio (highest impact, no code changes needed)
- Watcher & Stalker textures/models (renderers currently reuse vanilla player/wolf skeletons as functional
  placeholders — see `TODO(art)` markers in `client/render/`).
- All 12 registered `SoundEvent`s need actual `.ogg` files + `sounds.json` entries pointing to them (the
  registrations and `sounds.json` skeleton already exist).
- Dread HUD icon sprite (`textures/gui/dread_icon.png`).

### Priority 2 — Networking polish
- Wire a real S2C payload for dread sync (`PayloadTypeRegistry` + `ClientPlayNetworking`) so
  `ClientDreadState` updates from actual server pushes instead of remaining at its default value.

### Priority 3 — Structure completion
- Model the Understratum Chapel and Bonewell Shaft as real NBT structure templates (currently specified in
  design doc but not yet authored as `.nbt` files) with brazier/altar blocks wired to `RitualEvent`.
- Add a dedicated `BrazierBlock`/`AltarBlockEntity` to replace the current static-evaluator stub for the
  ritual's block interactions.

### Priority 4 — Additional content
- A third entity for late-game Confrontation-phase encounters.
- A true shader-based post-processing vignette (currently a flat HUD-layer approximation).
- Additional structures and a proper custom noise router for less generic Understratum terrain shaping.

## License

MIT — see [LICENSE](LICENSE).
