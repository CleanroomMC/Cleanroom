# Cleanroom

Cleanroom is an ecosystem for Minecraft 1.12.2, built on a continuation of MinecraftForge.
- Runs on modern Java versions (25+)
- LWJGL 3 + SDL 3 rendering and input stack
- Compatible with almost all Forge mods, with edge cases handled by [Fugue](https://github.com/CleanroomMC/Fugue)

If you want to...
- Play or make modpacks? Jump to [Installation](#installation).
- Write mods? See [For Mod Developers](#for-mod-developers).

## Platform Direction

### Developer Experience

- [CleanroomGradle](https://github.com/CleanroomMC/CleanroomGradle): Gradle plugin for Cleanroom development
- [Conventions](https://github.com/CleanroomMC/Conventions): Cleanroom code conventions
- Modding Templates: [CleanroomModTemplate](https://github.com/CleanroomMC/CleanroomModTemplate)
- Modpack Resources: [Preparing Your Modpack](https://cleanroommc.com/wiki/end-user-guide/preparing-your-modpack), [PackCompanion](https://github.com/AnasDevO/PackTemplateCompanion)

### User Experience

- Better performance and faster instance launching
- [CleanroomRelauncher](https://github.com/CleanroomMC/CleanroomRelauncher): Forge mod that relaunches the Forge instance with Cleanroom
- CJK IME-Friendly Input Handling: Allow gameplay keys such as WASD to function properly with IMEs on
- (Planned) Pre-Splash Bootstrap Screen: Show immediate launching feedback, so large modpacks don't hang before launching. Refer to [Proposal 650](https://github.com/CleanroomMC/Cleanroom/discussions/650)
- (Planned) Vanilla UI/UX Overhaul: Modern UI/UX design that preserves the vanilla pixel art style

### Platform APIs

- Caching API: A common caching framework for storing expensive data persistently
- (WIP) Compute API: A managed computing API that abstracts OpenCL

### Architecture Overhaul

- (WIP) [Kirino Engine](https://github.com/CleanroomMC/Kirino-Engine): It's a work-in-progress explicit rendering runtime for Minecraft
- (Planned) Strata: A unified asset loading pipeline for Cleanroom. Refer to [Proposal 584](https://github.com/CleanroomMC/Cleanroom/discussions/584)

### Content Mods / Remakes

[Refitbench](https://github.com/Refitbench): Suborganization of CleanroomMC that concentrates on refitting, maintaining and developing old open-source mods on 1.12.2

- [HadEnoughItems](https://github.com/CleanroomMC/HadEnoughItems)
- [AgriCraft](https://github.com/Refitbench/AgriCraft)
- [BetterPortalsRefitted](https://github.com/Refitbench/BetterPortalsRefitted)
- [CodeChickenLib](https://github.com/Refitbench/CodeChickenLib)
- [SpongeForge](https://github.com/Refitbench/SpongeForge)
- [MatterOverdrive](https://github.com/Refitbench/MatterOverdrive)
- [Dyson-Cube-Project](https://github.com/Refitbench/Dyson-Cube-Project)
- (WIP) Backportium: A proper and modular port of modern Minecraft contents

## Kirino Engine

[Kirino Engine](https://github.com/CleanroomMC/Kirino-Engine) ships as a module of this repository and is wired into the loader lifecycle.

It is a work-in-progress rendering engine that replaces Minecraft's implicit render state with an explicit structure, and gives mod authors a real rendering API instead of another layer of mixins.

## FAQs

> Is Cleanroom a fork of MinecraftForge?

At the beginning it was, though Cleanroom slowly developed independence, and most of the work no longer resides in Forge's code.
It now includes Forge, and we are still optimizing and fixing Forge's performance and other issues.
Cleanroom itself grew to its own ecosystem, and beyond being a simple mod loader, with plans to include major APIs and frameworks.

> Are there plans to migrate to Vulkan?

No, and
- OpenGL is capable of almost all rendering scenarios, and we are utilizing modern OpenGL features heavily. Modern OpenGL itself is not technically obsolete
- OpenGL-to-Vulkan one-to-one mapping type of migration is considered as the worst practice possible. Heavy architectural redesign is required
- Lack of manpower to maintain multi-backend
- We would rather prefer BGFX when we finally come to multi-backend

> How is the (OpenGL related implementations) compatibility with macOS?

Expect everything to run properly as how 1.12 Forge did.
Advanced OpenGL implementations that macOS does not support have fallbacks and would not crash.
However, we have no guarantee that mods provide fallbacks as well.

> How is the mod compatibility in Cleanroom?
 
We retain compatibility with old Forge mods as much as possible, if not, Fugue can be installed to retain compatibility.
As for Cleanroom mods, before Cleanroom 1.0.0, no binary compatibility is guaranteed due to lots of internal reworks and rewrites that may happen.

## Version History

<details>
<summary>Click to Expand</summary>

### 0.7

- CleanroomGradle adoption
- LWJGLY & SDL 3 migration
- Caching API
- Kirino Engine font rendering + vector graphics APIs
- EventBus optimization

### 0.6

- Migration to our Mixin fork (CleanMix), reach parity with MixinBooter 11+
- Modern mod list screen

### 0.5

- Windows Mica support
- Modern GUI scaling
- Taskbar integration

### 0.4

- Birth of the Kirino Engine

### 0.3

- Patches and mappings rework
- Compatibility fixes (+ help divided into Fugue)

### 0.2

- Classloading via Foundation
- Push for Java 25+ compatibility

### 0.1

- The Beginning
- LWJGL 3 Adoption
- Java 21 requirement

</details>

## Runtime

- Java 25
- LWJGL 3, with LWJGLY shim for LWJGL 2 APIs
- Foundation, powerful LaunchWrapper replacement
- CleanMix, Mixin fork with proper bootstrapping and more features
- ImagineBreaker
- Netty 4.2, Guava 33, Apache HttpClient 5, Jakarta APIs and more

## Installation

> [!IMPORTANT]
> Follow [our guide over at our website](https://cleanroommc.com/wiki/end-user-guide/preparing-your-modpack)!
> It explains steps needed to take to port existing modpacks, and also informs you of the latest and greatest mods to use and to update to.

### Client

- **MultiMC-based Launchers (PrismLauncher, PolyMC etc)**: download the .zip (MMC instance) and import it.
  - Alternatively, unzip and move the patches directory into your existing 1.12 instance (same level as the `.minecraft` directory).
- **Minecraft Official Launcher**: follow instructions when launching the installer jar. Or launch it with cli.
- **Other Launchers (ATLauncher, HMCL etc):** install the [relauncher](https://www.curseforge.com/minecraft/mc-mods/cleanroom-relauncher), and follow instructions in the pop-up GUI.

> [!NOTE]
> Direct downloads can be found in GitHub Actions, Releases & the [CleanroomMC Download Page](https://download.cleanroommc.com/).
> Action builds require logging into GitHub before downloading.

### Server

- Follow instructions when launching the installer jar. Or launch it with cli.

## For Mod Developers

- The official template is [CleanroomModTemplate](https://github.com/CleanroomMC/CleanroomModTemplate). A porting guide is in the [Cleanroom wiki](https://cleanroommc.com/wiki/cleanroom-mod-development/introduction) (WIP).

- Scala 3 and Kotlin 2 are both usable for mod code, through Scalar and Forgelin-Continuous.

## Development & Build System

1. Clone repository
2. `git submodule update --init`
3. Install a Java 25 JDK
4. Import `build.gradle`
5. `./gradlew setup`
6. `./gradlew build`

**Run `git submodule update` every time you fetch upstream.**

| Directory          | Contents                                                                                                            |
|--------------------|---------------------------------------------------------------------------------------------------------------------|
| `module/minecraft` | Patched Minecraft. Sources are generated by `setup` and are untracked, only `module/minecraft/patches` is committed |
| `module/forge`     | Continuation of MinecraftForge                                                                                      |
| `module/cleanroom` | Cleanroom ecosystem: Boot/Loader/API/Implementations                                                                |
| `module/kirino`    | Kirino Engine, WIP rendering backbone of Cleanroom                                                                  |
| `module/builtin`   | Mods integrated into Cleanroom itself                                                                               |

### Patches, ATs and SAS

- Change vanilla behavior by editing `module/minecraft/src/main/java/` only.
- Run `gradlew generateMinecraftDiffs` before committing, or the changes will not exist. Patches are written to `module/minecraft/patches`.
- Run `gradlew applyMinecraftDiffs` to rebuild `module/minecraft/src` from the committed patches.
  - Such as after pulling from upstream, to properly update patched minecraft's sources.
- Keep patches small:
  - Prefer one or two line calls into a hook class over logic inside Minecraft.
  - Do not import classes, rather fully qualify your references for patches.
- Access transformers live in `module/forge/src/main/resources/forge_at.cfg`
- Side annotation strippers in `module/cleanroom/minecraft.sas`.
  - Use SAS to strip side annotations rather than patch out a `@SideOnly`.

### Main Tasks

| Task                                  | Behavior                                                              |
|---------------------------------------|-----------------------------------------------------------------------|
| `setup`                               | Workspace setup (chain of tasks)                                      |
| `runCleanroomClient`                  | Launches the development client with Kirino disabled                  |
| `runCleanroomServer`                  | Launches the server from the development environment                  |
| `runKirinoClient`                     | Launches the development client in `run/kirino` with Kirino enabled   |
| `runVanillaClient`/`runVanillaServer` | Launches unmodified Minecraft, to compare behavior                    |
| `generateMinecraftDiffs`              | Regenerates the patch set from edited Minecraft sources               |
| `applyMinecraftDiffs`                 | Recreates the Minecraft sources from the committed patch set          |
| `build`                               | Builds the universal, installer, userdev and MMC pack artifacts       |
| `cleanroomInfo`                       | Prints the effective CleanroomGradle mode, versions, caches and tools |
