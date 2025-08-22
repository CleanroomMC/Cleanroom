# Kirino Rendering

## Contributing!
Check [Contributing Page](https://github.com/CleanroomMC/Cleanroom/blob/proposal/render-system/CONTRIBUTING.md)!

## Design Principles
- As independent of Minecraft as possible
- Data/ID oriented

## MVP Goals
### GL Abstraction

- GL Resource Abstraction
  - Resource manager
- Shader Abstraction 
  - Only support `vert` + `frag` for now, but design with `tess`, `compute`, etc. in mind
  - Global shader registry
    - Compile and store shaders
    - Shader source hashing
  - Uniform
    - Parse uniforms from shader source
    - Uniform location and type memorization
    - UBO support
  - ShaderProgram
    - Uniform input type widening
- Buffer Abstraction
  - VAO + VBO + EBO -> Mesh
  - UBO, SSBO
  - PBO pack & unpack
  - TBO
  - Upload hint + access hint
  - Persistent buffer
  - Framebuffer
    - Attachment
- Mesh Abstraction
  - Mesh
  - InstancedMesh
  - MultiDrawMesh
  - Vertex attribute layout
- Texture Abstraction
  - Sampler
  - Texture
    - Texture2D (for common uses)
    - Texture2DMultisample (for multisampling fbo)
    - Texture2DArray (for texture atlas)
    - ...
- Sync / Fence Abstraction
- Debug Abstraction
  - KHR_debug
- Material Abstraction
  - MaterialTemplate to describe layout and shaders
  - MaterialInstance to hold actual parameters
- Camera Abstraction
- Render Pass Abstraction

### ECS

- Overall ECS structure
  - CleanWorld, CleanEntity, CleanComponent, CleanSystem
- Entity ✔
  - Entity manager (utilizes archetype) ✔
- Component ✔
  - Component schema ✔
  - Class scan via ClassGraph ✔
- System
  - RenderSystem - a specialized system
    - RenderPass
    - ...
- Storage ✔
  - Archetype ✔
- Runtime
  - Job system to coordinate different systems
    - Execution priority
    - Async execution. Fence

### Pipeline

- RenderPass
- RenderCommand
- Batching commands
- Decorating commands
- ...

### Drafts
- Meshlet
  - A smaller object for better culling and more
  - ChunkComponent -> contains many meshlets
  - MeshletComponent -> contains geometry data, metadata (meshlet aabb etc.)
- Probe & Surface cache card -> Diffuse GI
  - Every meshlet has a card that contains a probe grid
  - Every probe samples light sources and other cards (recursive indirect lighting - temporal accumulation)

### Current Todos
- ECS world system coordinator (DAG). edge -> system; node -> barrier
- Modify EntityManager - callback on flush
- Finish JobScheduler
- MeshletManager ?
- Fix AccessHandlePool not able to find fields from parent classes

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

## Pack-making with Cleanroom

### Steps of Migrating to Cleanroom

1. Add Scalar and Fugue in your pack.
2. Add Relauncher (optional, this will make your pack a full Cleanroom pack)
3. Test launching and remove incompatible mods

### About Cross-compat Between Forge and Cleanroom

- Cleanroom mods (Fugue, Scalar) will be ignored by Forge, so then won't crash existed players
- Cleanroom integrated mods (MixinBooter, ConfigAnyTime) will be ignored by Cleanroom, then won't crash new players
- The version of built-in MixinBooter is configurable in forge_early.cfg

### List of Obsoleted/Incompatible Mods

- SpongeForge: Use [SpongeForge LTS](https://www.curseforge.com/minecraft/mc-mods/spongeforge)
- Phosphor: Use [Hesperus](https://www.curseforge.com/minecraft/mc-mods/hesperus) or [Alfheim Lighting Engine](https://www.curseforge.com/minecraft/mc-mods/alfheim-lighting-engine)
- Forgelin: Use [Forgelin-Continuous](https://www.curseforge.com/minecraft/mc-mods/forgelin-continuous)
- LibrarianLib: Use [LibrarianLib-Continuous](https://www.curseforge.com/minecraft/mc-mods/librarianlib-continuous)
- JustEnoughIds: Use [RoughlyEnoughIDs](https://www.curseforge.com/minecraft/mc-mods/reid)
- AdvancedShader: Binary patching, incompatible
- Polyfrost series: Waiting for official fix
- Essential.gg: Patched but still buggy

## Build Instructions:

1. Clone this repository
2. Import the `build.gradle` into your IDE (most preferably IntelliJ IDEA)
3. Once the import has finished, run `gradlew setup`
4. Run `gradlew --stop` to stop the daemon and prevent ForgeGradle gone wrong 
5. Build with `gradlew build`

## Development Tips:

- Only modify `projects/cleanroom/src/` directory if you want to change vanilla
- Run `gradlew genPatches` before commit, or the changes won't exist
- Modifications on `src/` doesn't need generating patches
- [Tips from Forge](https://github.com/MinecraftForge/MinecraftForge/wiki/If-you-want-to-contribute-to-Forge) are still apply, keep the patches clean!
- The current patches is full of useless hunks after we switched to VineFlower, we encourage contributors to clean up these patches manually.

## Mod Development:

Official template is here: [template](https://github.com/CleanroomMC/CleanroomModTemplate)

A porting guide is available in [Cleanroom wiki](https://cleanroommc.com/wiki/cleanroom-mod-development/introduction) (WIP).
