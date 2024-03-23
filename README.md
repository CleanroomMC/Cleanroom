# Cleanroom Minecraft

## Features
### Implemented

- 1.12.2 on Java 21
- LWJGL3
- A working *mod development template/kit*
- Patches for loading incompatible mods
- Built-in Mixin

### Planning

- CleanroomGradle to replace ForgeGradle
- No more Late and Early mixin
- Classfile API
- Scala 3 + Kotlin 2
- Actually useful APIs (See [here](https://github.com/orgs/CleanroomMC/projects/4/))
- Optimized
- Compatibility

## Components:

- Minecraft Coder Pack
- CleanroomLoader (Continuation + Revamp of ForgeModLoader)
- Cleanroom Minecraft (Continuation + Revamp of MinecraftForge)
- Mixin [(Coming Soon)](https://github.com/CleanroomMC/CleanMix)
- Bytecode Patcher (Coming Soon) \[Inspired by [Bansoukou](https://github.com/LoliKingdom/Bansoukou) and [Bytecode Patcher](https://github.com/jbredwards/Bytecode-Patcher)]
- [Fugue](https://github.com/CleanroomMC/Fugue), a mod patches many incompatibilities. (temporary)

## Downloads:

- For MultiMC-based launchers (PolyMC, PrismLauncher), go to CleanroomMMC's [action page](https://github.com/CleanroomMC/CleanroomMMC/actions), download the latest build and import it in your launcher(alternatively unzip patches and json inside to your 1.12 instance).
- For regular launcher (official launcher, AT launcher, FTB, HMCL), go to Cleanroom's [action page](https://github.com/CleanroomMC/Cleanroom/actions), click the latest action marked as **Build and Upload Test Artifact** and download the **installer** artifact. You could use the installer like the Forge one.

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
- The current patches is full of useless hunks after we switched to VineFlower, we encourage contributors to manual clean up their patches

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
