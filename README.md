# Cleanroom Minecraft

## Features
### Implemented

- 1.12.2 on Java 21
- *Latest* LWJGL3
- A working *mod development template/kit*
- Patches for loading incompatible mods
- Built-in Mixin w/ handy bootstrapping
- Develop using Scala 3 + Kotlin 2
- Compatibility to 99% of Forge mods

### Planning

- CleanroomGradle to replace ForgeGradle
- Classfile API
- Actually useful APIs (See [here](https://github.com/orgs/CleanroomMC/projects/4/))
- Optimized

## Components:

- Minecraft Coder Pack
- CleanroomLoader (Continuation + Revamp of ForgeModLoader)
- Cleanroom Minecraft (Continuation + Revamp of MinecraftForge)
- Customized Mixin
- Bytecode Patcher (Coming Soon) \[Inspired by [Bansoukou](https://github.com/LoliKingdom/Bansoukou) and [Bytecode Patcher](https://github.com/jbredwards/Bytecode-Patcher)]
- [Fugue](https://www.curseforge.com/minecraft/mc-mods/fugue), a mod patches many incompatibilities.
- Javassist
- [Scalar](https://www.curseforge.com/minecraft/mc-mods/scalar), a Scala provider. We made Scala libraries become a standalone mod so it can be updated.
- [Forgelin-Continuous](https://www.curseforge.com/minecraft/mc-mods/forgelin-continuous) and [LibrarianLib-Continuous](https://www.curseforge.com/minecraft/mc-mods/librarianlib-continuous)

## Download & Installation:

- For MultiMC-based launchers (PolyMC, PrismLauncher), download the MMC instance from [release](https://github.com/CleanroomMC/Cleanroom/releases), import it in your launcher(alternatively unzip patches and json inside to your 1.12 instance).
- For regular launcher (official launcher, AT launcher, FTB, HMCL), download the installer jar from release. You could use the installer like the Forge one.
- **WARNING:** Only MultiMC-based launchers are officially supported. This is because of the limit on removing vanilla libraries in other launchers.
- **Remember to install [Fugue](https://www.curseforge.com/minecraft/mc-mods/fugue)!**
- **And [Scalar](https://www.curseforge.com/minecraft/mc-mods/scalar)!**
- If you were told to use **action builds** (aka bleeding edge), here: [Cleanroom jar](https://github.com/CleanroomMC/Cleanroom/actions), [Cleanroom MMC zip](https://github.com/CleanroomMC/CleanroomMMC/actions/), [Fugue](https://github.com/CleanroomMC/Fugue/actions)
- You need to log-in with a GitHub account to download action artifacts. 

## Build Instructions:

1. Clone this repository
2. Import the `build.gradle` into your IDE (most preferably IntelliJ IDEA)
3. Once the import has finished, run `gradlew setup`
4. Build with `gradlew build`

## Development Tips:

- Only modify `projects/cleanroom/src/` directory if you want to change vanilla
- Run `gradlew genPatches` before commit, or the changes won't exist
- Modifications on `src/` doesn't need generating patches
- [Tips from Forge](https://github.com/MinecraftForge/MinecraftForge/wiki/If-you-want-to-contribute-to-Forge) are still apply, keep the patches clean!
- The current patches is full of useless hunks after we switched to VineFlower, we encourage contributors to clean up these patches manually.

## Mod Development:

There's an unofficial [template](https://github.com/kappa-maintainer/ExampleMod-1.12.2-FG5) exist. Note: You need to build before run.

A porting guide is available in [Cleanroom wiki](https://cleanroommc.com/wiki/cleanroom-mod-development/introduction).

## Roadmap flow chart

```mermaid
graph TD;
    A(Mixin integration)-->D(Bouncepad overhaul - we are here);
    B(LWJGL compat)-->D;
    C(Newer Java compat)-->D;
    D-->E(Config any time);
    D-->G(Minor improvement and fixes)
    E-->F(Greater improvement needs configs)
    X(Cleanroom Gradle)-->D
    D-->X
    D-->Y(Better Mixin Integration)
    Y-->D
```
