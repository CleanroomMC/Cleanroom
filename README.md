# Kirino Engine

## Roadmap & Todos
[View Project Board](https://github.com/orgs/CleanroomMC/projects/13) to track development progress, features and ideas.

## How It Works?
If you are curious about the tech/code details, check out [Engine Overview Page](https://github.com/CleanroomMC/Cleanroom/blob/proposal/render-system/ENGINE_OVERVIEW.md)!

## Contributing
If you would like to contribute, check out our [Contributing Page](https://github.com/CleanroomMC/Cleanroom/tree/proposal/render-system?tab=contributing-ov-file)!

## MVP Goals

<details>
<summary>GL Abstraction</summary>

**Goal**: a semantic abstraction layer that preserves the meaning of GL operations instead of a black-box GL wrapper 

- GL Resource Abstraction 🚧
  - Resource manager 🚧
- Shader Abstraction 🚧
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
- Buffer Abstraction 🚧
  - Generic buffer object + View ✅
  - VAO + VBO + EBO -> Mesh
  - UBO, SSBO
  - PBO pack & unpack
  - TBO
  - Upload hint + access hint
  - Persistent buffer ✅
  - Framebuffer ✅
    - Attachment ✅
    - RenderBuffer ✅
- Mesh Abstraction 🚧
  - Mesh
  - InstancedMesh
  - MultiDrawMesh
  - Vertex attribute layout ✅
- Texture Abstraction 🚧
  - Sampler
  - Generic texture object + View ✅
  - Texture 🚧
    - Texture2D (for common uses) ✅
    - Texture2DMultisample (for multisampling fbo) 🚧
    - Texture2DArray (for texture atlas)
    - ...
- Sync / Fence Abstraction
- Debug Abstraction ✅
  - KHR_debug ✅
- Material Abstraction
  - MaterialTemplate to describe layout and shaders
  - MaterialInstance to hold actual parameters

</details>

<details>
<summary>ECS</summary>

- Overall ECS structure ✅
  - CleanWorld, CleanEntity, CleanComponent, CleanSystem ✅
- Entity ✅
  - Entity manager (utilizes archetype) ✅
- Component ✅
  - Component schema ✅
  - Class scan via ClassGraph ✅
- Storage ✅
  - Archetype ✅
- Runtime 🚧
  - `SystemExeGraph` to coordinate different systems
    - Execution priority
    - Async execution & barrier
- Job ✅
  - Job is a unit of work that can be split and executed in parallel ✅

</details>

<details>
<summary>Engine</summary>

- CPU & GPU hybrid dual pipeline
- DrawCommand decorating mechanism
- RenderPass / Subpass architecture 🚧
- Built-in Multi-resolution & Super-sampling
- Immutable Pipeline State Object 🚧
- Scriptable pipeline
- ...

</details>
