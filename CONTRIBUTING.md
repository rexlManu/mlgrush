# Contributing

Thanks for contributing to MLGRush.

## Setup

- Install JDK 21
- Clone the repository
- Run `./gradlew build`

## Development workflow

- Make changes in the relevant module: `plugin`, `arenalib`, or `arenacreator`
- Format changes with `./gradlew spotlessApply`
- Verify with `./gradlew build`
- Keep changes focused and avoid unrelated cleanup unless it helps the current task

## Style

- Java source is formatted with Spotless + Google Java Format
- Gradle Kotlin DSL files are formatted with Spotless
- Prefer existing project conventions over personal style changes

## Pull requests

- Use clear commit messages
- Describe the gameplay, API, or build impact in the PR summary
- Mention any manual test steps for lobby, arena, NPC, scoreboard, or stats changes
- Update documentation when setup, commands, or behavior changes

## Reporting issues

When opening an issue, include:

- Server version
- Plugin version or commit
- Reproduction steps
- Relevant logs or stack traces
