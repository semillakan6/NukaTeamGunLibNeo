# NukaTeam Gun Library (NTGL)

NeoForge library mod for **Minecraft 1.21.1** — shared systems and APIs for NukaTeam weapon content and companion mods.

## Requirements

- **Java 21**
- **NeoForge** (version aligned with `gradle.properties` → `neo_version`)

## Building

From the repository root:

```bash
./gradlew build
```

The mod JAR is written under `build/libs/`.

## Development runs

Gradle run configurations (after `prepareRuns` setup):

```bash
./gradlew runClient
./gradlew runServer
```

## License

This project is licensed under **LGPL-2.1** — see [LICENSE.txt](LICENSE.txt).

## Links

- **Community:** [NukaTeam Discord](https://discord.com/invite/njb6EKaDVJ)

## Authors

Maintainers and contributors are listed in the mod metadata (`gradle.properties` → `mod_authors`) and in packaged `META-INF/neoforge.mods.toml`.
