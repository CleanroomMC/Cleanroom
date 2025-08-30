# Contributing to Kirino Rendering

Kirino Rendering is a ECS Rendering Engine ([See proposal](https://github.com/CleanroomMC/Cleanroom/discussions/405)).<br>
Thanks for your interest! We welcome **all kinds of contributions** – code, documentation, bug reports, and ideas.

***

## Getting Started

- Fork the repo (branch: `proposal/render-system`) and clone it locally.
- `./gradlew cleanroomClient` to run the project.
- `./gradlew build` to build the project.
- `./gradlew genPatches` to generate patches if you modified Minecraft source code. 
- `Cleanroom/projects/cleanroom/src/main/java/` is where you modify Minecraft source code.
- `Cleanroom/src/main/java/com/cleanroom/kirino/` is where you contribute to Kirino Rendering.

## Ways to Contribute

- Report bugs via [Issue](https://github.com/CleanroomMC/Cleanroom/issues) (Mention Kirino Rendering in title)
- Improve / add more java docs (typos, explanations, tutorials).
- Add unit tests / coverage tests (`Cleanroom/src/test/java/com/cleanroom/test/kirino/`).
- Implement features. (Check Upcoming Features / Future Features / Propose your own). Contact me, tttsaurus, if you want to implement something but find it confusing.
- Propose features you want to have / implement via [Proposal](https://github.com/CleanroomMC/Cleanroom/discussions/405).
- Propose general ideas about Kirino Rendering via [Proposal](https://github.com/CleanroomMC/Cleanroom/discussions/405).

## Code Style

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
  **Good:**
  ```java
  if (escape) {
      return;
  }
  ```
- Keep lines reasonably short to maintain readability, but there is no explicit char count limit.

## Upcoming Features

- GL Buffer Abstraction (I, tttsaurus, plan to do it)
- Mesh & Vertex Layout Abstraction (I, tttsaurus, plan to do it)
- GL Framebuffer Abstraction (_No one is in charge / Help wanted_)
  - Flexible attachment support
  - Keep MRT support in mind
  - OR whatever that helps
- GL Shader Abstraction (_No one is in charge / Help wanted_)
  - Read `uniform` / `ubo` / `ssbo` entries from raw shader source and record them
  - Easy `setUniform` / `bind` / `unbind`
  - Type inference and widening for `setUniform`
  - Compute shader support is needed
  - OR whatever that helps
- ECS Job Scheduling (I, tttsaurus, plan to do it)
- ECS system coordinator (_No one is in charge / Help wanted_)
  - It has nothing related to ECS itself, which is a separate module
  - It's essentially a Directed Acyclic Graph
  - Edge represents a system
  - Node represents a barrier that synchronizes systems that are running asynchronously
  - So a system coordinator handles the dependencies between systems, allowing systems to run asynchronously together but also with an order
- Meshlet Algorithm (_No one is in charge / Help wanted_)
  - A meshlet is a small piece of mesh (like ~32 vertices or less)
  - Every meshlet also needs an AABB
  - We use meshlets to do fine-grained culling and more
  - This part is about how to get many meshlets from a voxel chunk
  - The algorithm itself should be as generic as possible

## Future Features

- Check MVP Goals in [Read Me](https://github.com/CleanroomMC/Cleanroom/blob/proposal/render-system/README.md)

## What's Done
- An ECS framework with basic functionalities. Check [Proposal/Progress on ECS](https://github.com/CleanroomMC/Cleanroom/discussions/405) so you'll understand what's ECS and how it works
