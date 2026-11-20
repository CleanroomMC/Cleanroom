# Forge EventBus 7 dependency

Cleanroom baseline `f3e4f5fe2a9c07da35bd8ae09bc6937a3f638dc0` (`0.7.14-alpha-9`), Kirino `9d446db2deaa505d5dd66b488a1af8c385ff27de`. CleanroomGradle 0.23.0, Gradle 9.7.0 and Temurin 25.0.5+8. The wrapper requires Java 25 to load the settings plugin. `gradlew.bat setup projects tasks --all` resolves the single root project and its `test`, `isolatedTest`, `benchmark`, `runCleanroomClient` and `runCleanroomServer` tasks.

The historical event sources use `7695d38a08f398f8732f3a40d56b580cb6a2d610`. [Cleanroom #617](https://github.com/CleanroomMC/Cleanroom/pull/617) remains open at `63408d0eb3bdafc4620909eccbda65f72ee7e6fe`. #618 already removed the production subscriber transformer in the current baseline.

| Artifact | SHA-256 |
| --- | --- |
| `net.minecraftforge:eventbus:7.0.6` JAR | `88f9fa4da5cb6ea72ca2ff11e8ac15955bb00ddf3769eac1f2c6720ff890fd56` |
| Sources JAR | `ef3bbfa167a609c09e5f8e249086b3b388f5e4b9dc9fdbc062e979f521afd19f` |
| POM | `3d24bfa780679c01cef1745e9d509dc131d68f1df584d751642a4e95fbe30b75` |
| Gradle module metadata | `cf9348385a214a07a2080e05e896f3993987cb566c404b8e36ae97ac618eab36` |
| `org.jspecify:jspecify:1.0.1` JAR | `070d75f261fe4c5b8202508366715f7f2d4660f88c8ef7e6d3575e48c9683b66` |

Both EventBus JAR hashes match the [Forge Maven sidecars](https://maven.minecraftforge.net/net/minecraftforge/eventbus/7.0.6/) and Gradle module metadata. All 32 published Java sources match the corresponding files in `588a6c7a2fb3d936aefc3c1bd89435c42f5a92d8` by path and bytes. CleanroomGradle already routes the dependency to CleanroomMC Maven, where the JAR, POM and Gradle metadata match the Forge Maven bytes. The resolved runtime libraries feed the Cleanroom installer and userdev metadata. [Pinned source](https://github.com/MinecraftForge/EventBus/tree/588a6c7a2fb3d936aefc3c1bd89435c42f5a92d8)

The JAR contains 39 classfiles targeting major 65 and has no package overlap with `net.minecraftforge.fml`. Its JPMS module `net.minecraftforge.eventbus` exports the upstream API, requires `java.logging` and transitively requires `org.jspecify`. The Foundation test worker loads the unmodified binary through the production `LaunchClassLoader` constructor and verifies record cancellation, mutable event identity, and exact token and bulk removal. It passed on Java 25.

EventBus is Copyright Forge Development LLC, LGPL-2.1-only. It is resolved as an external library from its original artifact. The published JARs omit the license text. [License](https://github.com/MinecraftForge/EventBus/blob/588a6c7a2fb3d936aefc3c1bd89435c42f5a92d8/LICENSE.txt) · [Corresponding sources](https://maven.minecraftforge.net/net/minecraftforge/eventbus/7.0.6/eventbus-7.0.6-sources.jar)
