# Engine Overview

## 0. Introduction

Briefly describe the engine's design philosophy:
- Designed to be as independent of Minecraft as possible
- ECS & Data Oriented Technology Stack (much like Unity)
- Modern rendering architecture (GPU-driven geometry, meshlet, multi-draw indirect)
- CPU & GPU hybrid dual pipeline
- Semi-static diffuse global illumination with a final gather stage (may include RT)
- The primary goals are high performance, extensibility, and future-proof design

And the following is a walkthrough that guides you through the engine step by step.

***

**Table of Contents**
- 1 ECS Runtime & Kirino Engine Setup
  - 1.1 ECS Runtime Setup
  - 1.2 Kirino Engine Setup
- 2 Rendering Pipeline
  - 2.1 Pipeline State Object
  - 2.2 Framebuffer
  - 2.3 Render Pass & Subpass
  - 2.4 Draw Command
  - 2.5 Command Decoration
  - 2.6 Minecraft TileEntity & Entity Rendering
  - 2.7 Walkthrough & Overview
- 3 OpenGL Abstraction
  - 3.1 Resource Disposal
  - 3.2 Buffer Object
  - 3.3 Texture
  - 3.4 Shader
- 4 Chunk Meshlet & Virtual Geometry
- 5 Virtual Geometry & GPU Pipeline
- 6 Global Illumination
- 7 Hybrid & Final Gather
- 8 Tile Entity Renderer API

***

## 1. ECS Runtime & Kirino Engine Setup

- `KirinoCore.init()` will be executed at the end of `FMLClientHandler.beginMinecraftLoading()`, which occurs at the end of the `preInit` phase.
- `KirinoCore.postInit()` will be executed right after Forge's splash process finishes.

During `KirinoCore.init()`, the following steps will be performed:
- Set up `KHR_debug`
- Register default event listeners to `KirinoCore.KIRINO_EVENT_BUS`
- Initialize the ECS Runtime
- Initialize the Kirino Engine

During `KirinoCore.postInit()`
- OpenGL resources will be allocated

> Note: 
> - We pass `KirinoCore.KIRINO_EVENT_BUS` to the constructors of `CleanECSRuntime` & `KirinoEngine`, so all relevant events will be posted to `KirinoCore.KIRINO_EVENT_BUS`
> - Please use `KirinoCore.KIRINO_EVENT_BUS` instead of `MinecraftForge.EVENT_BUS` for your event listeners

Here's a list of events you can listen from ECS Runtime setup:
- `StructScanningEvent`
- `ComponentScanningEvent`
- `JobRegistrationEvent`

Here's a list of events you can listen from Kirino Engine setup:
- `ShaderRegistrationEvent`
- `PostProcessingRegistrationEvent`

### Example: How do I register shaders?
`shader.vert`
```glsl
#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec4 color;

out vec4 Color;

uniform vec3 worldOffset;

uniform mat4 viewRot; // Minecraft doesn't use standard viewModel matrix
uniform mat4 projection;

void main(void)
{
    gl_Position = projection * viewRot * vec4(position - worldOffset, 1.0);
    Color = color;
}

```
```java
@SubscribeEvent
public static void onShaderRegister(ShaderRegistrationEvent event) {
    event.register(new ResourceLocation("namespace:your/shader.vert"));
}
```

> Note:
> - Please use shader suffix `vert`, `frag`, `geom`, `tesc`, `tese`, `comp`. They indicate the shader type.

## 1.1 ECS Runtime Setup

Let's look at an example first
```java
PositionComponent positionComponent = new PositionComponent();
positionComponent.xyz = new Vector3f(1, 2, 3);

// components will be flattened and stored in archetype data pools
// and entities are represented as data rather than objects
// -> reduce per entity object allocations and gc pressure
ECS_RUNTIME.entityManager.createEntity(positionComponent);
ECS_RUNTIME.entityManager.createEntity(positionComponent);
ECS_RUNTIME.entityManager.createEntity(positionComponent);

// the side effects are deferred. guarantees thread safety
ECS_RUNTIME.entityManager.flush();

// the query is archetype oriented not entity oriented. faster query
List<ArchetypeDataPool> archetypes = ECS_RUNTIME.entityManager.startQuery(ECS_RUNTIME.entityManager.newQuery().with(PositionComponent.class));
LOGGER.info("archetype num: " + archetypes.size()); // assert: 1

// the array is fetched from archetype data pools directly. zero copy
// providing a friendly structure to parallel jobs
IPrimitiveArray<?> array = archetypes.get(0).getArray(PositionComponent.class, "xyz", "z");
ArrayRange range = archetypes.get(0).getArrayRange();
for (int i = range.start; i < range.end; i++) {
    // skip deleted entities
    if (range.deprecatedIndexes.contains(i)) {
        continue;
    }
    LOGGER.info("debug: " + array.getFloat(i));
} // assert: 3.0 3.0 3.0
```

As you can see, ECS is a way to manage entities and execute jobs on them effectively.
We'll use ECS to manage render targets, like `Chunk`, `Meshlet`, `Probe`, etc.
Although raw chunk data can't be stored directly in ECS, ECS is still useful to manage the components that store metadata like `isDirty`, `pos`, `aabb` etc.

### ECS Job usage example
```java
public class ChunkMeshletGenJob implements IParallelJob {
    @JobExternalDataQuery
    public ChunkProviderClient chunkProvider;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosX"})
    public IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosZ"})
    public IPrimitiveArray chunkPosZArray;

    @Override
    public void execute(int index) {
        int x = chunkPosXArray.getInt(index);
        int z = chunkPosZArray.getInt(index);
        KirinoCore.LOGGER.info("debug chunk xz: " + x + ", " + z);
    }

    @Override
    public void query(EntityQuery entityQuery) {
        entityQuery.with(ChunkComponent.class);
    }
}
```

### Example: How do I register ECS components?
```java
@CleanComponent
public class YourComponent implements ICleanComponent {
    public YourStruct a;
}

@CleanStruct
public class YourStruct {
    public int b;
}
```
```java
@SubscribeEvent
public static void onStructScan(StructScanningEvent event) {
    event.register("your.package.name");
}

@SubscribeEvent
public static void onComponentScan(ComponentScanningEvent event) {
    event.register("your.package.name");
}
```

> Note:
> - Built-in available types `T` = {`int`, `float`, `boolean`, `org.joml.Vector2f`, `org.joml.Vector3f`, `org.joml.Vector4f`, `org.joml.Matrix3f`, `org.joml.Matrix4f`}
> - You can only use any of `T` or the structs you defined in a struct
> - Yan can only use any of `T` or the structs you defined in a component

## 1.2 Kirino Engine Setup

Kirino Engine contains:
- `RenderingCoordinator`
- More coordinators coming soon...

`RenderingCoordinator` contains:
- **OpenGL related resources**
  - Frame finalizer
  - Indirect draw command generator
  - Fullscreen triangle instance
- **Utilities**
  - OpenGL state backup util
- **Logic**
  - Minecraft scene (an ECS world)
  - Minecraft camera
- **Patches**
  - Minecraft tile entity & entity culling overhaul
  - Minecraft entity rendering overhaul
  - Minecraft TESR rendering overhaul
- **Shaders**
  - Shader registry
- **Managers**
  - Staging buffer manager (where to upload buffer)
  - Graphic resource manager (where to manage resource lifecycle)
  - Gizmos manager (where to manage debug visuals)
- **Render Passes**

### Engine's Lifecycle
`KirinoCore.updateAndRender()` is the only entry point of the Kirino Engine (currently)
```java
public static void updateAndRender(long finishTimeNano) {
    // KIRINO_ENGINE.renderingCoordinator.doThings()
}
```

Minecraft `EntityRenderer` patch:
```java
EntityRenderer#updateCameraAndRender(float partialTicks, long nanoTime)
...
    if (KirinoCore.isEnableRenderDelegate())
    {
        KirinoCore.updateAndRender(System.nanoTime() + l);
    }
    else
    {
        this.renderWorld(partialTicks, System.nanoTime() + l);
    }
...
```

> Note: `KirinoCore.updateAndRender()` is a direct replacement of `EntityRenderer.renderWorld(float, long)`. Specifically, `anaglyph` logic is removed and all other functions remain the same.

## 2. Rendering Pipeline

`KIRINO_ENGINE.renderingCoordinator.run*Pass` runs render passes. A render pass is a container that contains several subpasses, and subpass is where we implement the true rendering logic.

The following pseudocode illustrates the concept
```java
RenderPass pass = new RenderPass("Main Pass");
pass.addSubpass("Opaque Pass", new OpaquePass(renderer, opaquePSO));
pass.addSubpass("Cutout Pass", new CutoutPass(renderer, cutoutPSO));
pass.addSubpass("Transparent Pass", new TransparentPass(renderer, transparentPSO));
pass.render(camera);
```

As you can see, each subpass (`OpaquePass`/`CutoutPass`/`TransparentPass`) defines both its rendering logic and its associated `PSO` (Pipeline State Object).

## 2.1 Pipeline State Object
A `PSO` can be seen as an immutable value type that contains multiple GL states like `depth`, `raster` etc. 
We introduce `PSO` to avoid `GL.turnOn* -> render -> GL.turnOff*`-ish operations. During each subpass, all GL states are fixed and immutable (i.e. `GL.enable*`/`GL.getInt*` will never be used by clients).

> Note:
> - `GL.getInt*` causes pipeline stall
> - `PSO` makes every states predicatable so we drop manual `GL.enable*`/`GL.getInt*`

## 2.2 Framebuffer
A framebuffer, also known as a render target, is the target of a subpass.
Multi-resolution and HDR support is enforced by `FrameFinalizer` to facilitate future upgrades.

Framebuffers are managed and initiated by our frame finalizer (`RenderingCoordinator.frameFinalizer`).
`FrameFinalizer` can be seen as the simplified version of a Frame Graph (Frame Graph is able to analyze the dependencies between render passes and framebuffers automatically),
where we handle framebuffer dependencies and swaps manually and hide the complexity under simple interfaces.

> Note: Render passes can't bind their target framebuffers. Every framebuffer-related thing is handled by `FrameFinalizer`.

## 2.3 Render Pass & Subpass
`RenderPass` is a container that doesn't handle any of the rendering logic or implementations but stores `Subpass`es in an order.

```java
public class RenderPass {
    public void render() {
        for (Subpass subpass : subpasses) {
            subpass.collectCommands(drawQueue);
            subpass.decorateCommands(drawQueue, decorator);
            subpass.render(drawQueue);
        }
    }
}
```

As you can see, `RenderPass` acts like an entry point and we implement abstract `Subpass` classes to handle the true rendering logic.

### `Subpass.render()` Procedure
```mermaid
graph TD
    A[Collect Draw Commands] --> B[Decorate Draw Commands]
    B --> C[Compile Draw Commands]
    C --> D[Simplify Draw Commands]
    D --> E[Sort Draw Commands]
    E --> G[Bind Pipeline State Object]
    G --> H[Update Shader Program / Uniforms]
    H --> I[Execute Draw Commands / Submit to GPU]
```

## 2.4 Draw Command
`DrawCommand` is an element of `DrawQueue`, and `Subpass` accepts a `DrawQueue` as the input.

There are only two types of `DrawCommand`:
- `HighLevelDC` - stores the resource ticket IDs
- `LowLevelDC` - stores the actual GPU handles (like `vao` IDs, indices, etc.)

### `HighLevelDC`-to-`LowLevelDC` Compilation Procedure
```mermaid
graph TD
    A0[Request Resource Tickets] --> A[Pass Resource Ticket IDs to HighLevelDC]
    A --> B[/HighLevelDC/]
    B --> C[Compile HighLevelDC]
    A0 --> A1[[GraphicResourceManager Handles the Request]]
    A1 --> A2[[GraphicResourceManager Uploads Tickets' Payload via StagingBufferManager During Updates]]
    A2 --> A3[[Fetch Tickets From GraphicResourceManager]]
    A3 --> A4[[Access Raw GPU Handles From Tickets' Receipt]]
    C --> A3
    A4 --> D
    C --> D[/LowLevelDC/]
```

And `LowLevelDC` stores the raw GPU handles that we use directly in raw GL draw calls.
Specifically, `LowLevelDC` corresponds exactly to raw GL draw calls.

Usually, we do the following in a `Subpass` implementation.
```java
@Override
public void collectCommands(DrawQueue drawQueue) {
    drawQueue.enqueue(gizmosManager.getDrawCommand());
}
```
Method calls like `getDrawCommand` construct a new `HighLevelDC`, which is where resource ticket requests take place.

To be more specific about ticket requests.
```java
graphicResourceManager.requestMeshTicket("my_mesh", uploadStrategy).ifPresent(builder -> {
    build(builder);
    graphicResourceManager.submitMeshTicket(builder);
});

return HighLevelDC.passInternal()
    .meshTicketID("my_mesh") // it's safe to use the ticket immediately even if the its payload hasn't been uploaded
    .mode(GL11.GL_TRIANGLES)
    .elementType(GL11.GL_UNSIGNED_INT)
    .build();
```
We _don't care_ if
- A ticket's payload is uploaded (uploading is usually an async process) OR 
- A ticket is expired

Every ticket's status will be read carefully during the `HighLevelDC` compilation stage, which is during `Subpass.render()`, and bad tickets will be ignored.

Therefore, there is a delay between generating the real `LowLevelDC`s and submitting the ticket requests (usually an one frame delay).

The resource ticket system makes every graphic resource predictable and transparent, and our design philosophy of `HighLevelDC` here is:
- Independent of raw GPU handles (`vao`, `ebo`, etc.)
- Independent of resource uploading (`glSubData` etc.)
- Independent of resource lifecycle (whether it's `CPU-only` OR `GPU-ready` OR `to-be-destroyed`)
- Just tell what you want to draw, like a simple keyword `my_mesh`

## 2.5 Command Decoration
WIP. You'll be able to insert draw commands per pass, making things like drawing overlays extremely easy.

## 2.6 Minecraft TileEntity & Entity Rendering
Unfortunately, TileEntity rendering (TESR) and entity rendering were written in the fixed-function OpenGL pipeline back then (with `glTranslate`, `glTexCoord`, etc.)

Therefore, TileEntity & Entity rendering _cannot_ be transformed into our shader-based rendering which will break countless mods.

Nevertheless, we rewrote and refactored Minecraft's `RenderGlobal.renderEntities()`. Now, we have another three classes instead:
- `MinecraftCulling`
- `MinecraftEntityRendering`
- `MinecraftTESRRendering`

A finer-culling enabled by meshlet-gen on TEs and entities will be performed by `MinecraftCulling`,
and then `MinecraftEntityRendering.renderEntities()` and `MinecraftTESRRendering.renderTESRs()` will be executed subsequently.

Due to the nature of the fixed-function pipeline, we won't be able to have Kirino Engine's global illumination and features to work on TEs and entities.
Directional light shadow, however, is still going to work on them.

## 2.7 Walkthrough & Overview
See `KirinoCore.updateAndRender()` for details.
```mermaid
graph TD
    A[/1. Pre Update/] --> A0[[GL State Backup]] 
    A0 --> A1[[Update Framebuffers' Resolution]]
    A1 --> A2[[Bind Main Framebuffer]]
    A2 --> B[/2. Minecraft Setup/]
    B --> B0[[Update Camera Matrices]]
    B0 --> B1[[Draw Fog]]
    B1 --> B2[[Draw Sky and Clouds]]
    B2 --> C[/3. Update/]
    C --> C0[[ECS-world Update]]
    C0 --> C1[[GraphicResourceManager Run Staging]]
    C1 --> D[/4. Chunk Opaque Pass/]
    D --> E[/5. Minecraft Rendering/]
    E --> E0[[Cull TEs and Entities]]
    E0 --> E1[[Draw TEs and Entities - Forge Pass 0]]
    E1 --> E2[[Draw Block Select Box]]
    E2 --> E3[[Draw Debug Visuals]]
    E3 --> E4[[Draw Destroy Process]]
    E4 --> E5[[Draw Particles]]
    E5 --> E6[[Draw Weather]]
    E6 --> F[/6. Chunk Transparent Pass/]
    F --> G[/7. Minecraft's Final Rendering/]
    G --> G0[[Draw TEs and Entities - Forge Pass 1]]
    G0 --> G1[[Draw Above Clouds]]
    G1 --> G2[[Post RenderWorldLastEvent]]
    G2 --> G3[[Draw Hand]]
    G3 --> H[/8. Gizmos Pass/]
    H --> I[/9. Post Update/]
    I --> I0[[Finalize Framebuffers]]
    I0 --> I1[[Blit Results to Minecraft's Framebuffer]]
    I1 --> I2[[Bind Minecraft's Framebuffer]]
```
As you can see, we still got lots of vanilla logic in our code, 
and we are actively trying to replace them with our own implementations.

## 3. OpenGL Abstraction
**Goal and Philosophy**: a semantic abstraction layer that preserves the meaning of GL operations instead of a black-box GL wrapper.

### Example: Buffer
For exmaple, we adopt a buffer-view pattern for managing buffers/textures. We use the untyped GL buffer container `GLBuffer` and views like `VBOView`.

```java
public class VBOView extends BufferView {
    public VBOView(GLBuffer buffer) {
        super(buffer);
    }

    @Override
    public int target() {
        return GL15.GL_ARRAY_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL15.GL_ARRAY_BUFFER_BINDING;
    }
}
```

As a result, we can use arbitrary many views to wrap a buffer/texture to modify them.
Moreover, we provide high-level mapping utilities like `Obj2BufMorphism` to manage data without touching indexes, 
which can be seen as a replacement to Minecraft's `BufferBuilder`.

### Example: Attribute Layout
```java
public static final AttributeLayout BLOCK_ATTRIBUTE_LAYOUT = new AttributeLayout();
static {
    BLOCK_ATTRIBUTE_LAYOUT.push(new Stride(28)
            .push(new Slot(Type.FLOAT, 3))
            .push(new Slot(Type.UNSIGNED_BYTE, 4).setNormalize(true))
            .push(new Slot(Type.FLOAT, 2))
            .push(new Slot(Type.SHORT, 2)));
}
```

When it comes to Vertex Attribute Layout, our abstraction aligns semantically with GL, allowing all possibilities.

## 3.1 Resource Disposal
All disposable GL resources will be added to `GLResourceManager` automatically by calling their `ctor`.
`GLResourceManager.disposeAll()` occurs at the end of the game, right before window destroy.

## 3.2 Buffer Object

## 3.3 Texture

## 3.4 Shader
