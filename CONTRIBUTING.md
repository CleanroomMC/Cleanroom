# Contributing to Kirino Rendering

Kirino Engine is an ECS-based Rendering (general purpose) Engine ([See proposal](https://github.com/CleanroomMC/Cleanroom/discussions/405)/[See engine overview](https://github.com/CleanroomMC/Cleanroom/blob/proposal/render-system/ENGINE_OVERVIEW.md)).<br>
Thanks for your interest! We welcome **all kinds of contributions** – code, documentation, bug reports, and ideas.

***

## Getting Started

- Fork the repo (branch: `proposal/render-system`) and clone it locally.
- `./gradlew cleanroomClient` to run the project.
- `./gradlew build` to build the project.
- `./gradlew genPatches` to generate patches if you modified Minecraft source code. 
- Experimental: `./gradlew cleanroomClientRenderDoc` / `./gradlew cleanroomClientNsight` (check out `build.gradle`)
- `Cleanroom/projects/cleanroom/src/main/java/` is where you modify Minecraft source code.
- `Cleanroom/src/main/java/com/cleanroom/kirino/` is where you contribute to Kirino Engine.

## Ways to Contribute

- Report bugs via [Issue](https://github.com/CleanroomMC/Cleanroom/issues) (Mention Kirino Engine in title)
- Improve / add more java docs (typos, explanations, tutorials).
- Add unit tests / coverage tests (`Cleanroom/src/test/java/com/cleanroom/test/kirino/`).
- Implement features. (Check Upcoming Features / Future Features / Propose your own). Contact me, tttsaurus, if you want to implement something but find it confusing.
- Propose features you want to have / implement via [Proposal](https://github.com/CleanroomMC/Cleanroom/discussions/405).
- Propose general ideas about Kirino Engine via [Proposal](https://github.com/CleanroomMC/Cleanroom/discussions/405).

## Code Style Convention

- Use `camelCase` for methods / fields.
- Use K&R brace styling
  
  **Bad:**
  ```java
  while (true)
  {
      // code
  }
  ```
  **Good:**
  ```java
  while (true) {
      // code
  }
  ```
- Use braces for all control statements. i.e. Always use braces `{}` for `if`, `for`, `while`, etc., even if the body has only one line.
  
  **Bad:**
  ```java
  if (escape) return;
  ```
  ```java
  if (escape)
      return;
  ```
  **Good:**
  ```java
  if (escape) {
      return;
  }
  ```
- Keep lines reasonably short to maintain readability, but there is no explicit char count limit.
- Use `Jspecify` for `nullable`/`nonnull` annotations; Use `nullable`/`nonnull` on public APIs and where you think necessary.
- Don't use `this.` if not necessary.
- Use google `Preconditions` to check argument/state/nonnull preconditions instead of `Objects.requireNonNull()` or manual if-statement etc.
- Use `Preconditions` to check `nonnull` even after the `nonnull` annotations.
  
  **Example:**
  ```java
  @Override
  public IBuilder<S, I> setEntryCallback(@NonNull S state, @Nullable OnEnterStateCallback<S, I> callback) {
      Preconditions.checkNotNull(state, "Provided \"state\" can't be null.");
  
      ...
  }
  ```
- Add blank line after precondition checks / chunks of code / early escape.
- Start a setence/phrase with a capital letter in javadoc.
- Line-comments begin with a lowercase letter.
- Add space after line-comment (`//`).
- All error messages end with a period (`.`).
- Add a single space after each comma in a list of items, parameters, or arguments.
- Avoid returning null and use `Optional<T>` instead.
- Add a single space everywhere if possible.

  **Bad:**
  ```java
  if(a==1){
      return;
  }
  ```
  **Good:**
  ```java
  if (a == 1) {
      return;
  }
  ```
- Use `//<editor-fold desc="your desc">` & `//</editor-fold>` if necessary.
- **_Finally, while no style guide can cover every situation, maintaining consistency is critical!_**

## Upcoming Features

- GL Shader Abstraction (_No one is in charge / Help wanted_)
  - Read `uniform` / `ubo` / `ssbo` entries from raw shader source and record them
  - Easy `setUniform` / `bind` / `unbind`
  - Type inference and widening for `setUniform`
  - Compute shader support is needed
  - OR whatever that helps
- ECS system coordinator (_Default_)
  - It has nothing related to ECS itself, which is a separate module
  - It's essentially a Directed Acyclic Graph
  - Edge represents a system
  - Node represents a barrier that synchronizes systems that are running asynchronously
  - So a system coordinator handles the dependencies between systems, allowing systems to run asynchronously together but also with an order
- Meshlet Algorithm (_Eerie_)
  - A meshlet is a small piece of mesh (like ~32 vertices or less)
  - Every meshlet also needs an AABB
  - We use meshlets to do fine-grained culling and more
  - This part is about how to get many meshlets from a voxel chunk
  - The algorithm itself should be as generic as possible

## Future Features

- Check MVP Goals in [Read Me](https://github.com/CleanroomMC/Cleanroom/tree/proposal/render-system?tab=readme-ov-file)

## What's Done
- An ECS framework with basic functionalities
- Some GL abstractions
- Part of the rendering pipeline
