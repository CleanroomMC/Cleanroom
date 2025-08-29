# Kirino Rendering

## Contributing
Check [Contributing Page](https://github.com/CleanroomMC/Cleanroom/blob/proposal/render-system/CONTRIBUTING.md)!

## Design Principles
- As independent of Minecraft as possible
- Data/ID oriented

## MVP Goals

<details>
<summary>GL Abstraction</summary>

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

</details>

<details>
<summary>ECS</summary>

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

</details>

<details>
<summary>Engine</summary>

- Mesh generation
- Lighting
- `RenderPipeline`
- `RenderPass`
- `RenderCommand`
- Batching commands
- Decorating commands
- ...

</details>

## Misc
<details>
<summary>Drafts</summary>

### Ideas

- Meshlet
  - Definition: a meshlet is a small subdivision of geometry used as the fundamental rendering unit
  - It enables better culling and more
  - Each ChunkComponent contains multiple meshlets
  - Each MeshletComponent stores virtual geometry data, metadata (meshlet AABB etc.)
  - Merge and simplify meshlets based on LOD (somewhat easy cuz actual vertex & index gen are on GPU-side)
  - Split custom models to meshlets too (a challenge on how to design virtual geometry)
- Virtual Geometry
  - Goal: reduce CPU–GPU bandwidth by avoiding full mesh data uploads
  - So virtual geometry are high-level metadata
  - Actual vertex & index gen are on GPU-side
  - Metadata is lightweight and hopefully it'll be easier to merge and simplify meshlets
- Probe & Surface Cache Card -> Semi-static Diffuse GI
  - Each meshlet has one or more cards that record radiance, normal, color, etc. (might need a better card allocation strategy)
  - Probes are placed in world dynamically
  - Probes sample light sources and other cards (recursive indirect lighting with temporal accumulation; i.e. not heavy)
  - As a result, lighting info is updated gradually over frames, smoothing out noise
  - Cards read lighting info from surrounding probes
  - LOD affects the number of cards per meshlet
  - The light radiance and normal are interpolated per-pixel during shading, providing a somewhat accurate lighting
- Screen-space Radiance Cache
  - Probes & cards only provide a rough & semi-static lighting, but SSRC refines result in screen-space
  - SSRC is like a ray-traced final gather

Meshing: meshlet + virtual geometry<br>
Lighting: Semi-static Diffuse GI with Temporal Accumulation + SSRC

### Follow-up Ideas

- Maintain a meshlet pool with a fixed number of free meshlets, similar to how EntityManager works
- Destroy and reallocate meshlets during Load / Unload / Modify chunk callbacks
- Each ChunkComponent contains multiple MeshletHandle (meshletID + generation; so it's easy to check if a meshlet has expired)
- Try to decouple chunks and meshlets: chunks are not the owners of meshlets but the input of our meshlet gen function
- Similarily, meshlets are not the owners of surface cards but the input of our surface card gen function
- Enforce the idea of functional programming paradigm: inputs -> outputs, separating logic from resource management
- Treat ChunkComponent as 16x16x16 sections instead of 16x16x256? be friendly to future cubic chunks

</details>

<details>
<summary>Current Todos</summary>

- ECS world system coordinator (DAG). edge -> system; node -> barrier
- Modify EntityManager - callback on flush
- Finish JobScheduler
- MeshletManager ?
- Fix AccessHandlePool not able to find fields from parent classes

</details>
