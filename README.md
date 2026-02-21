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
- [Scalar](https://www.curseforge.com/minecraft/mc-mods/scalar-legacy), a Scala provider. We made Scala libraries become a standalone mod so it can be updated.
- [Forgelin-Continuous](https://www.curseforge.com/minecraft/mc-mods/forgelin-continuous) and [LibrarianLib-Continuous](https://www.curseforge.com/minecraft/mc-mods/librarianlib-continuous)

## Download & Installation:

- For MultiMC-based launchers (PolyMC, PrismLauncher), download the MMC instance from [release](https://github.com/CleanroomMC/Cleanroom/releases), import it in your launcher(alternatively unzip patches and json inside to your 1.12 instance).
- For regular launcher (official launcher, AT launcher, FTB, HMCL), install the [relauncher](https://www.curseforge.com/minecraft/mc-mods/cleanroom-relauncher), launch the game and follow instructions.
- **WARNING:** Only MultiMC-based launchers are officially supported. This is because of the limit on removing vanilla libraries in other launchers.
- **Remember to install [Fugue](https://www.curseforge.com/minecraft/mc-mods/fugue)!**
- **And [Scalar](https://www.curseforge.com/minecraft/mc-mods/scalar-legacy)!**
- If you were told to use **action builds** (aka bleeding edge), here: [Cleanroom](https://github.com/CleanroomMC/Cleanroom/actions), [Fugue](https://github.com/CleanroomMC/Fugue/actions)
- You need to log in your GitHub account to download action artifacts. 

## Cleanroom on Server

- Cleanroom can be installed on server.
- Just use the installer mentioned in previous section, and run the jar with `--installServer`. See [this page](https://minecraft.fandom.com/wiki/Tutorials/Setting_up_a_Minecraft_Forge_server) for more detail. 

## Pack-making with Cleanroom

### Steps of Making Your Pack Cleanroom Ready

1. Add Scalar and Fugue in your pack.
2. Add Relauncher (optional, this will make your pack a full Cleanroom pack)
3. Test launching and remove incompatible mods

### Steps of Migrating Your Pack Fully to Cleanroom

1. Make your pack Cleanroom ready.
2. Install relauncher to your pack. There are 3 variants available: [official](https://www.curseforge.com/minecraft/mc-mods/cleanroom-relauncher), [unofficial](https://www.curseforge.com/minecraft/mc-mods/cleanroom-relauncher-unofficial), [improved](https://www.curseforge.com/minecraft/mc-mods/improved-cleanroom-relauncher)
3. Configure the relauncher. All variants have their pros and cons, choose what you need.

### About Cross-compat Between Forge and Cleanroom

- Cleanroom mods (Fugue, Scalar) will be ignored by Forge, so then won't crash Forge
- Jar of Cleanroom integrated mods (MixinBooter, ConfigAnyTime) will be ignored by Cleanroom, then won't crash under Cleanroom
- The version of built-in MixinBooter is configurable in forge_early.cfg

### Prepare Your Mods for Cleanroom

Some mods are obsoleted or need extra handle. See [wiki](https://cleanroommc.com/wiki/end-user-guide/preparing-your-modpack#incompatible-problematic-mods-on-cleanroom-launcher)

## Build Instructions:

1. Clone this repository
2. `git submodule init` then `git submodule update`
3. Import the `build.gradle` into your IDE (most preferably IntelliJ IDEA)
4. Once the import has finished, run `gradlew setup`
5. Run `gradlew --stop` to stop the daemon and prevent ForgeGradle gone wrong 
6. Build with `gradlew build`

**Remember to run `git submodule update` after everytime you fetched upstream!**

## Development Tips:

- Only modify `projects/cleanroom/src/` directory if you want to change vanilla
- Run `gradlew genPatches` before commit, or the changes won't exist
- Modifications on `src/` doesn't need generating patches
- [Tips from Forge](https://github.com/MinecraftForge/MinecraftForge/wiki/If-you-want-to-contribute-to-Forge) are still apply, keep the patches clean!
- The current patches is full of useless hunks after we switched to VineFlower, we encourage contributors to clean up these patches manually.

## Mod Development:

Official template is here: [template](https://github.com/CleanroomMC/CleanroomModTemplate)

A porting guide is available in [Cleanroom wiki](https://cleanroommc.com/wiki/cleanroom-mod-development/introduction) (WIP).
