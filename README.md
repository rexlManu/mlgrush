# MLGRush

Modernized MLGRush minigame plugins for Paper `1.21.11`, split into runtime, arena library, and arena creation modules.

## Modules

- `plugin` - the main MLGRush Paper plugin
- `arenalib` - shared arena model and utility code
- `arenacreator` - helper plugin for creating and editing arenas

## Stack

- Java 21
- Gradle 9
- Paper `1.21.11`
- PacketEvents for packet-backed NPCs, scoreboards, and holograms
- Shadow for plugin packaging
- FreeFair Lombok for Lombok integration

## Features

- Lobby and arena gameplay for MLGRush style matches
- Packet-based sidebar and below-name scoreboards
- Packet-based lobby NPCs and holograms
- File-backed player data and stats wall updates
- Arena creation support through the `ArenaCreator` module

## Development

### Requirements

- JDK 21

### Common commands

```bash
./gradlew build
./gradlew spotlessApply
./gradlew runServer
```

`runServer` delegates to `:plugin:runServer` and launches a local Paper dev server.

## Project layout

```text
.
|- plugin/
|- arenalib/
|- arenacreator/
|- build.gradle.kts
`- settings.gradle.kts
```

## Publishing notes

- This repository includes source for the plugin and companion modules only
- No GitHub release workflow is configured here
- Build artifacts are produced through the Gradle Shadow tasks

## Contributing

See `CONTRIBUTING.md` for local setup, formatting, and pull request guidance.

## License

This project is licensed under the MIT License. See `LICENSE`.
