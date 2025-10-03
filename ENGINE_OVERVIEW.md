# Engine Overview

## 1. Introduction

Briefly describe the engine's design philosophy:
- Designed to be as independent of Minecraft as possible
- ECS & Data Oriented Technology Stack (much like Unity)
- Modern rendering architecture (GPU-driven geometry, meshlet, multi-draw indirect)
- CPU & GPU hybrid dual pipeline
- Semi-static diffuse global illumination with a final gather stage (may include RT)
- The primary goals are high performance, extensibility, and future-proof design

And the following is a walkthrough that guides you through the engine step by step.

## 2. ECS Runtime and Kirino Engine Setup

`KirinoCore.init()` will be executed at the end of `FMLClientHandler.beginMinecraftLoading()`, which occurs at the end of the `preInit` phase.

During `KirinoCore.init()`, the following steps will be performed:
- Set up `KHR_debug`
- Register default event listeners to `KirinoCore.KIRINO_EVENT_BUS`
- Initialize the ECS Runtime
- Initialize the Kirino Engine

> Note: 
> - We pass `KirinoCore.KIRINO_EVENT_BUS` to the constructors of `CleanECSRuntime` & `KirinoEngine`, so all relevant events will be posted to `KirinoCore.KIRINO_EVENT_BUS`
> - Please use `KirinoCore.KIRINO_EVENT_BUS` instead of `MinecraftForge.EVENT_BUS` for your event listeners

## 2.1 ECS Runtime Setup

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
INativeArray<?> array = archetypes.get(0).getArray(PositionComponent.class, "xyz", "z");
ArrayRange range = archetypes.get(0).getArrayRange();
for (int i = range.start; i < range.end; i++) {
    // skip deleted entities
    if (range.deprecatedIndexes.contains(i)) {
        continue;
    }
    LOGGER.info("debug: " + array.get(i));
} // assert: 3.0 3.0 3.0
```

As you can see, ECS is a way to manage entities and execute jobs on them effectively.
We'll use ECS to manage render targets, like `Chunk`, `Meshlet`, `Probe`, etc.
Although raw chunk data can't be stored directly in ECS, ECS is still useful to manage the components that store metadata like `isDirty`, `pos`, `aabb` etc.

### How do I register ECS components?
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
> - Available types `S` = {`int`, `float`, `boolean`, `org.joml.Vector2f`, `org.joml.Vector3f`, `org.joml.Vector4f`, `org.joml.Matrix3f`, `org.joml.Matrix4f`}
> - You can only use `S` in a struct
> - Yan can only use `S` or the structs you defined in a component

## 2.2 Kirino Engine Setup

Kirino Engine contains:
- `RenderingCoordinator`
- More coordinators coming soon...

`RenderingCoordinator` contains:
- An ECS world `MinecraftScene`
- `MinecraftCamera` (which wraps Minecraft's `ActiveRenderInfo`)
- `GizmosManager` (for debug visuals)
- `ShaderRegistry` and all shader-related stuff
- A bunch of `RenderPass`es

### How do I register shaders?
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
> - Please use shader suffix `vert`, `frag`, `geom`, `tesc`, `tese`, `comp`

### Engine's Lifecycle
`KirinoCore.updateAndRender()` is the only entry point of the Kirino Engine
```java
public static void updateAndRender(long finishTimeNano) {
    // KIRINO_ENGINE.renderingCoordinator.run*Pass
}
```

> Note: `KirinoCore.updateAndRender()` is a direct replacement of `EntityRenderer.renderWorld(float, long)`. Specifically, `anaglyph` logic is removed and all other functions remain the same.

## 3. Render Pass & Pipeline

`KIRINO_ENGINE.renderingCoordinator.run*Pass` runs render passes. A render pass is a container that contains several subpasses, and subpass is where we implement the true rendering logic.

The following pseudocode illustrates the concept
```java
ShaderProgram shaderProgram = shaderRegistry.newShaderProgram("kirino:shaders/test.vert", "kirino:shaders/test.vert");
Renderer renderer = new Renderer();
mainCpuPass = new RenderPass("Main CPU Pass");
mainCpuPass.addSubpass("Opaque Pass", new WhateverPass(renderer, PSOPresets.createOpaquePSO(shaderProgram), new Framebuffer()));
mainCpuPass.addSubpass("Cutout Pass", new WhateverPass(renderer, PSOPresets.createCutoutPSO(shaderProgram), new Framebuffer()));
mainCpuPass.addSubpass("Transparent Pass", new WhateverPass(renderer, PSOPresets.createTransparentPSO(shaderProgram), new Framebuffer()));

mainCpuPass.render();
```

As you can see, each subpass defines both its rendering logic and its associated `PSO` (Pipeline State Object) and `FBO` (Framebuffer).

### 3.1 Pipeline State Object
A `PSO` can be seen as an immutable value type that contains multiple GL states like `depth`, `raster` etc. 
We introduce `PSO` to avoid `GL.turnOn* -> render -> GL.turnOff*`-ish operations. During each subpass, all GL states are fixed and immutable (i.e. `GL.enable*`/`GL.getInt*` will never be used by clients).

> Note:
> - `GL.getInt*` causes pipeline stall
> - `PSO` makes every states predicatable so we drop manual `GL.enable*`/`GL.getInt*`

### 3.2 Framebuffer
A framebuffer, also known as a render target, is the target of a subpass.
Multi-resolution support is enforced here to facilitate future upgrades.

## 4. OpenGL Resource Abstraction
We adopt a buffer-view pattern for managing buffers/textures. For example, we use the untyped GL buffer container `GLBuffer` and views like `VBOView`.

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
Moreover, high-level views like `SegmentedVBOView`, `MorphismVBOView` etc. allow you to manage data without touching indexes.

## 4.1 Resource Disposal
All disposable GL resources will be added to `GLResourceManager` automatically by calling their `ctor`.
`GLResourceManager.disposeAll()` occurs at the end of the game, right before window destroy.
