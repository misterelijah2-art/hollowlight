# Contributing to Hollowlight

Thanks for your interest in contributing! This is a small, design-driven horror mod project — please read
[`docs/DESIGN.md`](docs/DESIGN.md) before proposing new mechanics, so additions stay consistent with the
mod's core tension ("being perceived is the danger") rather than becoming generic spooky content.

## Getting started

1. Fork and clone the repository.
2. Ensure you have JDK 21 installed.
3. Run `./gradlew build` to verify the project builds.
4. Run `./gradlew runClient` to test in a dev environment.

## Guidelines

- **Stay on-theme.** New entities/items/events should reinforce dread through perception, sound, or light —
  not just be "another zombie variant."
- **No placeholder logic in core systems.** AI state machines, the dread system, and event state machines
  must be functionally complete Java. Art/audio assets may be stubbed with clearly marked `TODO(art)`
  comments, but gameplay logic may not.
- **Match existing code style:** tabs for indentation (matches the Fabric example mod convention), Javadoc
  on public classes explaining *why*, not just *what*.
- **Small, focused commits/PRs.** Prefer "Add X system" over large mixed-purpose changes.
- **Data generation over hand-written JSON** where the schema supports it — extend the providers in
  `com.hollowlight.datagen` rather than hand-editing generated JSON files directly.

## Reporting issues

Please include your Minecraft version, Fabric Loader/API versions, and full log output (`logs/latest.log`)
for any crash reports.

## Code of conduct

Be respectful and constructive. This is a hobby project — assume good faith.
