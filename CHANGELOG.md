# Changelog

## 0.7.11-alpha - 2026-10-19

### Bug Fix

- **patch**: Erroneous variables being read in lightmap hook *[commit by [@Rongmario](https://github.com/Rongmario) in [873a9ab](https://github.com/CleanroomMC/Cleanroom/commit/873a9ab10ee58ebd9ca28a4fbad749379fd32083)]*

### Build and Dependencies

- **deps**: Update lwjgly to 2.3.0 *[commit by [@Rongmario](https://github.com/Rongmario) in [dcc04f2](https://github.com/CleanroomMC/Cleanroom/commit/dcc04f26a9da81ec9590a7dff7c44ab7478885cf)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.7.10-alpha...0.7.11-alpha

## 0.7.10-alpha - 2026-10-18

### Bug Fix

- **discovery**: Normalize mod paths when deduplicating candidates *[commit by [@Rongmario](https://github.com/Rongmario) in [3f52821](https://github.com/CleanroomMC/Cleanroom/commit/3f528210ef8bb46186b15b0bceacd37ae09e5de6)]*
    *[Fixes issue #703 by [@D3nnis3n](https://github.com/D3nnis3n)]*

### Build and Dependencies

- **deps**: Update foundation to 0.22.2 *[commit by [@Rongmario](https://github.com/Rongmario) in [141f0e9](https://github.com/CleanroomMC/Cleanroom/commit/141f0e9545d23ef93b8851e6ec46d60d944abfce)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.7.9-alpha...0.7.10-alpha

## 0.7.9-alpha - 2026-10-18

### Bug Fix

- **sdl**: Skip NVIDIA EGL exit teardown on Wayland *[commit by [@Rongmario](https://github.com/Rongmario) in [ae1b930](https://github.com/CleanroomMC/Cleanroom/commit/ae1b9302daeda1df4d9a5da897fd057aeedf7f58)]*
    *[Fixes issue #699 by [@teamnotus](https://github.com/teamnotus)]*
- **discovery**: Normalize game dir before gathering mod candidates *[commit by [@Rongmario](https://github.com/Rongmario) in [1de3e00](https://github.com/CleanroomMC/Cleanroom/commit/1de3e00b0dff281fca9cb4c31231199c91459b21)]*
    *[Fixes issue #697 by [@teamnotus](https://github.com/teamnotus)]*

### Documentation

- Add RELICENSING.md *[commit by [@tttsaurus](https://github.com/tttsaurus) in [35349b1](https://github.com/CleanroomMC/Cleanroom/commit/35349b181be6f21f8a27309d7d7744ffa6e4ee49)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.7.8-alpha...0.7.9-alpha

## 0.7.8-alpha - 2026-10-17

### Bug Fix

- **event**: Set active mod to Cleanroom if loading had not started yet *[commit by [@Rongmario](https://github.com/Rongmario) in [6591cbe](https://github.com/CleanroomMC/Cleanroom/commit/6591cbe3da14e752b75c524f4daa7ca73990073b)]*
    *[Fixes issue #680 by [@BuenChico785446](https://github.com/BuenChico785446)]*
    *[Fixes issue #688 by [@geyesborg](https://github.com/geyesborg)]*
- **compatibility**: Rethrow non-widening type mismatches in CompatibilityHooks.set *[commit by [@Rongmario](https://github.com/Rongmario) in [2bcc2b5](https://github.com/CleanroomMC/Cleanroom/commit/2bcc2b54cab7e338301ae5b46b13fd902fc52872)]*
- **client**: Widen ModelBlockDefinition$MissingVariantException.<init> to public in forge_at.cfg *[PR by [@geyesborg](https://github.com/geyesborg), Lilith in [#690](https://github.com/CleanroomMC/Cleanroom/pull/690)]*

### Performance

- **discovery**: Read access transformers during the scan instead of reopening the jar *[commit by [@Rongmario](https://github.com/Rongmario) in [b57228f](https://github.com/CleanroomMC/Cleanroom/commit/b57228f1672850cd2dc49399fabdd93de95a0e7f)]*
- **discovery**: Skip ASM parsing of classes that cannot carry a mod annotation *[commit by [@Rongmario](https://github.com/Rongmario) in [dac6284](https://github.com/CleanroomMC/Cleanroom/commit/dac628416e60b7bc5c9b460cb9fdf78a8c753da3)]*

### Refactor

- **discovery**: Drop duplicate cache and absolute path checks *[commit by [@Rongmario](https://github.com/Rongmario) in [e922286](https://github.com/CleanroomMC/Cleanroom/commit/e9222861cab0c4c781d8bdf4b73b552c50309196)]*

### Build and Dependencies

- **deps**: Update cleanmix, softens bugged mixin configs to not hard crash *[commit by [@Rongmario](https://github.com/Rongmario) in [48fa2f8](https://github.com/CleanroomMC/Cleanroom/commit/48fa2f8f54094dbfb83811c29d519f853f4ec842)]*

### First-time Contributors

- **[@geyesborg](https://github.com/geyesborg) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.7.7-alpha...0.7.8-alpha

## 0.7.7-alpha - 2026-10-16

### Bug Fix

- **compatibility**: Mirror LaunchClassLoader transformer fields to avoid loader constraint violations *[commit by [@Rongmario](https://github.com/Rongmario) in [199d4d8](https://github.com/CleanroomMC/Cleanroom/commit/199d4d8048d6ee8a920e82c3fee6bd6742e9e361)]*

### Build and Dependencies

- **deps**: Bump LWJGLY to 2.1.0 so Display.isCurrent ignores the splash shared context *[commit by [@Rongmario](https://github.com/Rongmario) in [ac6dfeb](https://github.com/CleanroomMC/Cleanroom/commit/ac6dfeb7d35b670f7fbe7f55dff79539c2a1b97b)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.7.6-alpha...0.7.7-alpha

## 0.7.6-alpha - 2026-10-15

### Bug Fix

- **registries**: Initialize holder classes before injecting object holders *[commit by [@Rongmario](https://github.com/Rongmario) in [acd2632](https://github.com/CleanroomMC/Cleanroom/commit/acd2632e30a2ef80c247733cc8b9b398b105f7a8)]*
- **sdl**: Deliver typed characters to every text widget *[commit by [@Rongmario](https://github.com/Rongmario) in [a6009b1](https://github.com/CleanroomMC/Cleanroom/commit/a6009b1be2c2e74fee749c97e0dbba92545d164c)]*
    *[Fixes issue #678 by [@BuenChico785446](https://github.com/BuenChico785446)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.7.5-alpha...0.7.6-alpha

## 0.7.5-alpha - 2026-10-15

### Feature

- **lwjgl**: Bridge window geometry, icons, display modes and expose events to LWJGLY *[commit by [@Rongmario](https://github.com/Rongmario) in [8d3ac4f](https://github.com/CleanroomMC/Cleanroom/commit/8d3ac4f70baaf73281998e89bd10e8110e02d50c)]*
- **lwjgl**: Back LWJGLY's SharedDrawable with SharedGLContext *[commit by [@Rongmario](https://github.com/Rongmario) in [6322201](https://github.com/CleanroomMC/Cleanroom/commit/63222012238f432ae35f3a3563ec2cd19010b3f0)]*

### Bug Fix

- **cleanmix**: Erroneous version being shown *[commit by [@Rongmario](https://github.com/Rongmario) in [868d55e](https://github.com/CleanroomMC/Cleanroom/commit/868d55e87435e41380f799630652951962c21eaf)]*

### Refactor

- **font**: Have jetbrains mono as default fallback *[commit by [@Rongmario](https://github.com/Rongmario) in [ba63fb7](https://github.com/CleanroomMC/Cleanroom/commit/ba63fb7ad107e39166f9b0c4e44b5e645a043579)]*
- **kirino**: Load fonts from cleanroom's cdn at runtime and cache *[commit by [@Rongmario](https://github.com/Rongmario) in [ef9d731](https://github.com/CleanroomMC/Cleanroom/commit/ef9d7317fcbe01d184cd2b48d96df30a44602b43)]*

### Build and Dependencies

- **deps**: Exclude old vanilla libraries duplicated under other coordinates *[commit by [@Rongmario](https://github.com/Rongmario) in [9ff7745](https://github.com/CleanroomMC/Cleanroom/commit/9ff77459e959807e0556065a976f7bf467ca9974)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.7.4-alpha...0.7.5-alpha

## 0.7.4-alpha - 2026-10-14

### Bug Fix

- **compatibility**: Keep LaunchClassLoader's exception set fields unredirected *[commit by [@Rongmario](https://github.com/Rongmario) in [73b461c](https://github.com/CleanroomMC/Cleanroom/commit/73b461c8d6614aa99eea0ee85af75d2237188e5e)]*
    *[Fixes issue #673 by [@MCTian-mi](https://github.com/MCTian-mi)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.7.3-alpha...0.7.4-alpha

## 0.7.3-alpha - 2026-10-14

### Feature

- **compatibility**: Enable final field mutation for Java 26+ *[commit by [@Rongmario](https://github.com/Rongmario) in [c815a0e](https://github.com/CleanroomMC/Cleanroom/commit/c815a0ef964c5129c1a77dff281383043e7500f3)]*
- **compatibility**: Fugue's generic patches, without whitelist & checks every Forge mod *[PR by [@Rongmario](https://github.com/Rongmario) in [#671](https://github.com/CleanroomMC/Cleanroom/pull/671)]*

### Bug Fix

- **lwjgl**: Report proper version in crashes and logging *[commit by [@Rongmario](https://github.com/Rongmario) in [2645a0f](https://github.com/CleanroomMC/Cleanroom/commit/2645a0f46bf040a47f6b6339ebf9797dd981c08a)]*
- **sdl**: Render at native resolution on HiDPI displays *[commit by [@Rongmario](https://github.com/Rongmario) in [8068eaa](https://github.com/CleanroomMC/Cleanroom/commit/8068eaad0bafd7c771bd88b4c027031c92851adb)]*
    *[Fixes issue #670 by [@teamnotus](https://github.com/teamnotus)]*

### Build and Dependencies

- **deps**: Remove vanilla's old oshi version *[commit by [@Rongmario](https://github.com/Rongmario) in [c481a48](https://github.com/CleanroomMC/Cleanroom/commit/c481a48df34b95b4219eeb7e3df8bb9608b3c1b1)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.7.2-alpha...0.7.3-alpha

## 0.7.2-alpha - 2026-10-13

### Comprehensive

- Several regression fixes and patches *[PR by [@ZZZank](https://github.com/ZZZank) in [#667](https://github.com/CleanroomMC/Cleanroom/pull/667)]*

### Bug Fix

- **sdl**: Make compositor-dependent window calls non-fatal *[commit by [@Rongmario](https://github.com/Rongmario) in [e9f958e](https://github.com/CleanroomMC/Cleanroom/commit/e9f958e6364b3d71a8654824a21b5fd5197611d5)]*
- **sdl**: Treat window icon errors as non-fatal *[commit by [@Rongmario](https://github.com/Rongmario) in [727eb5b](https://github.com/CleanroomMC/Cleanroom/commit/727eb5b2f641a9beda6c24217a52fa6d267a4c6d)]*
    *[Fixes issue #669 by [@teamnotus](https://github.com/teamnotus)]*
- **patch**: Mending consuming wrong amount of XP on item with custom `.getXpRepairRatio(...)` *[PR by [@ZZZank](https://github.com/ZZZank) in [#667](https://github.com/CleanroomMC/Cleanroom/pull/667)]*
- **patch**: Modded bed with no orientation always "too far away" *[PR by [@ZZZank](https://github.com/ZZZank) in [#667](https://github.com/CleanroomMC/Cleanroom/pull/667)]*
- **patch**: `ItemStack` with (both) no NBT but different caps being considered as equal *[PR by [@ZZZank](https://github.com/ZZZank) in [#667](https://github.com/CleanroomMC/Cleanroom/pull/667)]*
- **patch**: `Block#isReplaceable` with context not being used, causing difference when placing block on vegetation *[PR by [@ZZZank](https://github.com/ZZZank) in [#667](https://github.com/CleanroomMC/Cleanroom/pull/667)]*
- **patch**: Biome size hook *[PR by [@ZZZank](https://github.com/ZZZank) in [#667](https://github.com/CleanroomMC/Cleanroom/pull/667)]*

### Documentation

- Update README to illustrate roadmap & answer FAQs *[PR by [@tttsaurus](https://github.com/tttsaurus), Rongmario in [#654](https://github.com/CleanroomMC/Cleanroom/pull/654)]*

### Build and Dependencies

- **deps**: Update oshi *[commit by [@Rongmario](https://github.com/Rongmario) in [63c3394](https://github.com/CleanroomMC/Cleanroom/commit/63c3394ab32ded3bcd5f51a1d7c49e919143078d)]*
- **deps**: Update CleanroomGradle to 0.22.0 *[commit by [@Rongmario](https://github.com/Rongmario) in [2973da3](https://github.com/CleanroomMC/Cleanroom/commit/2973da34a392f0b9e06e3cf30705892352e7e4d3)]*
    *[Fixes issue #657 by [@MCTian-mi](https://github.com/MCTian-mi)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.7.1-alpha...0.7.2-alpha

## 0.7.1-alpha - 2026-10-13

### Bug Fix

- **sas**: Keep TextureOffset on the server *[commit by [@Rongmario](https://github.com/Rongmario) in [3889529](https://github.com/CleanroomMC/Cleanroom/commit/38895295cc7d29107b8b19fc8d27f5135c653b83)]*
- **sdl**: Bring up SDL video before the game settings load *[commit by [@Rongmario](https://github.com/Rongmario) in [c8c9df2](https://github.com/CleanroomMC/Cleanroom/commit/c8c9df2f7682b7bfd751c63646ed87a6e60d4570)]*
- **sdl**: Reapply the mouse grab on macOS when the cursor escapes *[commit by [@Rongmario](https://github.com/Rongmario) in [c4f735e](https://github.com/CleanroomMC/Cleanroom/commit/c4f735ed665ba6cd21e5c9096d31197298ca5aaa)]*
- **sdl**: Keep the splash thread from pumping events on macOS *[commit by [@Rongmario](https://github.com/Rongmario) in [86568f6](https://github.com/CleanroomMC/Cleanroom/commit/86568f64a62915875e8a02d7860306d724192292)]*

### Build and Dependencies

- **deps**: Update lwjgly to 1.0.2 *[commit by [@Rongmario](https://github.com/Rongmario) in [930c8e8](https://github.com/CleanroomMC/Cleanroom/commit/930c8e828f0f1a390b43cb2c420598ca238f2638)]*
    *[Fixes issue #659 by [@MCTian-mi](https://github.com/MCTian-mi)]*
- **deps**: Update Conventions to 1.4.1 *[commit by [@Rongmario](https://github.com/Rongmario) in [1d2d592](https://github.com/CleanroomMC/Cleanroom/commit/1d2d59223907e32c5de235df197d868f02651ccb)]*
- **deps**: Update CleanroomGradle to 0.21.1 *[commit by [@Rongmario](https://github.com/Rongmario) in [6010e54](https://github.com/CleanroomMC/Cleanroom/commit/6010e54d0670f205bc29f47542388f2909a19490)]*
- **deps**: Update lwjgly to 1.0.1 *[commit by [@Rongmario](https://github.com/Rongmario) in [63847b5](https://github.com/CleanroomMC/Cleanroom/commit/63847b581f6249256cca8f051c4599f9fe2950d9)]*

### CI

- Remove the manual publish workflow *[commit by [@Rongmario](https://github.com/Rongmario) in [314cfa4](https://github.com/CleanroomMC/Cleanroom/commit/314cfa4a8c5394652c506ad74b1b0641e09aff22)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.7.0-alpha...0.7.1-alpha

## 0.7.0-alpha - 2026-10-13

> [!WARNING]
> This version has breaking changes! More details below.

### Breaking Changes

- **config**: Drop early config options the SDL window no longer reads *[commit by [@Rongmario](https://github.com/Rongmario) in [d150534](https://github.com/CleanroomMC/Cleanroom/commit/d1505346fe9d8711665514fe689719350408543a)]*
  - ForgeEarlyConfig has removed fields
- Renamed kirino submodule *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*

### Comprehensive

- Project overhaul (CleanroomGradle, LWJGLY, SDL 3) *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*

### Feature

- **input**: Minecraft::getFrameDeltaSeconds, use it for MouseTurnEvent *[commit by [@Rongmario](https://github.com/Rongmario) in [fa7835a](https://github.com/CleanroomMC/Cleanroom/commit/fa7835a48378b35ecb4dab9b0d1d390d3d17c574)]*
- MouseTurnEvent *[PR by [@ndellagrotte](https://github.com/ndellagrotte), Claude Opus 5.5 in [#641](https://github.com/CleanroomMC/Cleanroom/pull/641)]*
- **cache**: Caching API *[PR by [@Rongmario](https://github.com/Rongmario) in [#636](https://github.com/CleanroomMC/Cleanroom/pull/636)]*
- Optimize NBTTagCompound and add builder-like methods *[PR by [@gardenevery](https://github.com/gardenevery) in [#655](https://github.com/CleanroomMC/Cleanroom/pull/655)]*
- **lwjgl**: Added `freetype` module *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **inputs**: Added SDL 3, replacing GLFW *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **inputs**: Native IME support *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **inputs**: Added inline IME when writing in books and signs *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **taskbar**: Implemented efficient taskbar flashing *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **window**: Implemented theme change events *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **window**: Proper borderless & normal fullscreen support *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **kirino**: New font rendering techniques *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- Make EventBus works without EventSubscriberTransformer *[PR by [@ZZZank](https://github.com/ZZZank) in [#618](https://github.com/CleanroomMC/Cleanroom/pull/618)]*

### Bug Fix

- **sdl**: Fall back through OpenGL versions when creating the context *[commit by [@Rongmario](https://github.com/Rongmario) in [56c6357](https://github.com/CleanroomMC/Cleanroom/commit/56c6357be0e9cc040bf9f044e9618fb8392586f3)]*
- **deps**: Erroneous configs leading to userdev not finding some libraries *[commit by [@Rongmario](https://github.com/Rongmario) in [f1cd20d](https://github.com/CleanroomMC/Cleanroom/commit/f1cd20d1fb9212d4ca429e6a611829ca6f85c7de)]*
- **event**: Keep re-entrant target changes and honor cancel in LivingSetAttackTargetEvent *[commit by [@Rongmario](https://github.com/Rongmario) in [7271d26](https://github.com/CleanroomMC/Cleanroom/commit/7271d269d39d995757d9dfa09362da9222958772)]*
    *[Fixes issue #535 by [@ThatBabu](https://github.com/ThatBabu)]*
    *[Fixes issue #545 by [@Natedogwoooof](https://github.com/Natedogwoooof)]*
- **discovery**: Scan the loader's class directory as a classpath candidate *[commit by [@Rongmario](https://github.com/Rongmario) in [b31209f](https://github.com/CleanroomMC/Cleanroom/commit/b31209f8a14dab18a41dbf6fcc4075f55ede976b)]*
- **build**: Fork isolatedTest only for isolated test classes *[commit by [@Rongmario](https://github.com/Rongmario) in [0244150](https://github.com/CleanroomMC/Cleanroom/commit/0244150b33784754abb59519b7d57b9701037799)]*
    *[Fixes issue #635 by [@Kuba663](https://github.com/Kuba663)]*
- **discovery**: Keep the loader jar as a classpath candidate *[commit by [@Rongmario](https://github.com/Rongmario) in [27335c1](https://github.com/CleanroomMC/Cleanroom/commit/27335c10fe534e45aca68e6d381e3f7f4218319b)]*
- **discovery**: Decide classpath libraries by content, not by name *[commit by [@Rongmario](https://github.com/Rongmario) in [9675aa4](https://github.com/CleanroomMC/Cleanroom/commit/9675aa4ce65fd58ba773d96fb0d2d47e4dc552b0)]*
- **lwjgl**: Make sure LWJGL2 only exists in the production runtime *[commit by [@Rongmario](https://github.com/Rongmario) in [7638742](https://github.com/CleanroomMC/Cleanroom/commit/7638742a38078fa10df37e0c0ef0d226c8d4f1da)]*
- **at**: Remap mods at runtime in dev properly *[commit by [@Rongmario](https://github.com/Rongmario) in [56422ea](https://github.com/CleanroomMC/Cleanroom/commit/56422ea63338abc2663e49772a8303aeb95a1544)]*
- **sdl**: Place the text field caret from drawTextBox's own layout *[commit by [@Rongmario](https://github.com/Rongmario) in [43ac715](https://github.com/CleanroomMC/Cleanroom/commit/43ac715a85e6a0c72692b8665061d36ff6ac7eaa)]*
    *[Fixes issue #646 by [@geyesborg](https://github.com/geyesborg)]*
- **sdl**: Drive IME state from the drawn screen and project text field carets *[commit by [@Rongmario](https://github.com/Rongmario) in [dfda99c](https://github.com/CleanroomMC/Cleanroom/commit/dfda99c84899f472fcde0986b0f1cb6b910f9d32)]*
- **sdl**: Rewind SDL-allocated buffers before SDL_free *[PR by [@ndellagrotte](https://github.com/ndellagrotte), Claude Opus 5.5 in [#642](https://github.com/CleanroomMC/Cleanroom/pull/642)]*
- **command**: Drop chat sent to a player while tab-completing their input *[commit by [@Rongmario](https://github.com/Rongmario) in [36aa3a7](https://github.com/CleanroomMC/Cleanroom/commit/36aa3a72638a2cb851077c90b939d7a6a61dc47c)]*
    *[Fixes issue #640 by [@NielsPilgaard](https://github.com/NielsPilgaard)]*
- **chat**: Cycle suggestions backward with Shift+Tab and resume only on edits *[commit by [@Rongmario](https://github.com/Rongmario) in [917292a](https://github.com/CleanroomMC/Cleanroom/commit/917292a341e5b26f1f914b5ae1a0a2fe02c5cfa1)]*
- **chat**: Keep Up and Down on history and cycle suggestions with Tab *[commit by [@Rongmario](https://github.com/Rongmario) in [6573c55](https://github.com/CleanroomMC/Cleanroom/commit/6573c55fcd7c730c3d32c6b4ea44903affd60a25)]*
    *[Fixes issue #623 by [@Wizzerinus](https://github.com/Wizzerinus)]*
- **sas**: Strip client-only markers from model classes again *[commit by [@Rongmario](https://github.com/Rongmario) in [74f4e37](https://github.com/CleanroomMC/Cleanroom/commit/74f4e37039705050f89ae775cfc387e2f30e51d7)]*
- **gradle**: Stop run tasks from inheriting mixin debug args by default *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **taskbar**: Native handle cleaned up *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **ime**: Remove previous IME hacks *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **patch**: `World#setBlockState` hunk regression *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **patch**: `WorldGenTaiga1` & `WorldGenTaiga2` hunk regressions *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **patch**: `WorldGenTrees` patch regression *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **patch**: `WorldGeneratorBonusChest` hunk regression *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **patch**: `BlockVine` hunk regression *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **patch**: `BlockPistonStructureHelper` hunk regression *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **patch**: Patch regression for `StructureVillagePieces` *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **awt**: Reduce AWT initializations, breaks in android and other headless setups *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*

### Refactor

- **mod-list**: Reuse GuiUtils and vanilla button textures *[PR by [@RuiXuqi](https://github.com/RuiXuqi), Rongmario in [#651](https://github.com/CleanroomMC/Cleanroom/pull/651)]*
- **debug**: Display proper LWJGL version, remove monitor/power info *[commit by [@Rongmario](https://github.com/Rongmario) in [bfa4943](https://github.com/CleanroomMC/Cleanroom/commit/bfa4943c7890423cbc4b23523d6ec6c82480f485)]*
- **gradle**: Drop the boot launchers for CleanroomGradle 0.18.0 *[commit by [@Rongmario](https://github.com/Rongmario) in [0af24d8](https://github.com/CleanroomMC/Cleanroom/commit/0af24d8276c5da91db7f1024e7839b57aacf11a7)]*
- Rewritten buildscripts *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- Removed `buildSrc/` *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- Modularized different portions of the project *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- Moved licenses to respective modules *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **hack**: Welcome back ImagineBreaker *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **lwjgl**: Remove `tinyfd` module *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **patch**: Removed unnecessary/verbose patches *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **patch**: Remove redundant `SideOnly` patches with SAS *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **patch**: New non-LZMA `ClassPatchManager` *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- **window**: Use SDL 3 methods to open links & files *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*

### Documentation

- New comprehensive and relevant `README` *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- Update CHANGELOG.md for 0.6.13-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [19b9fa8](https://github.com/CleanroomMC/Cleanroom/commit/19b9fa8f5042357efc9216f68204b368f7a03d90)]*

### Testing

- **junit**: Updated to JUnit 6 *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*

### Build and Dependencies

- Store gradlew.bat with LF in the index so tag builds see a clean worktree *[commit by [@Rongmario](https://github.com/Rongmario) in [6e5da28](https://github.com/CleanroomMC/Cleanroom/commit/6e5da28790627ead12cd32f7ce7a4b99eb50256c)]*
- **deps**: Update CleanroomGradle to 0.20.0 *[commit by [@Rongmario](https://github.com/Rongmario) in [c0728e6](https://github.com/CleanroomMC/Cleanroom/commit/c0728e6d26ff8381dbb6d8f8a1b872ee817722d1)]*
- **maven**: Disable javadocs & duplicated sources from being published for now *[commit by [@Rongmario](https://github.com/Rongmario) in [6d3192e](https://github.com/CleanroomMC/Cleanroom/commit/6d3192e9bc28d171aed9ef9c66c99a0464b0fb31)]*
- Move kirino run to cleanroom runs tasks group *[commit by [@Rongmario](https://github.com/Rongmario) in [99fc782](https://github.com/CleanroomMC/Cleanroom/commit/99fc782fd4ae438c41afa511063676c18c6c97d9)]*
- **deps**: Update CleanroomGradle to 0.19.0 *[commit by [@Rongmario](https://github.com/Rongmario) in [acc97d1](https://github.com/CleanroomMC/Cleanroom/commit/acc97d11b11ce938419afd9c88579f360585df57)]*
- **deps**: Update cleanmix to 0.7.3 *[commit by [@Rongmario](https://github.com/Rongmario) in [79a2a1b](https://github.com/CleanroomMC/Cleanroom/commit/79a2a1b20d3b8c50dc0574c278d7d24d9a8261a0)]*
- **deps**: Update foundation to 0.22.0 *[commit by [@Rongmario](https://github.com/Rongmario) in [21ef6d0](https://github.com/CleanroomMC/Cleanroom/commit/21ef6d02da67d319b67b33f76e249321558412f6)]*
- Update Conventions to 1.4.0 *[commit by [@Rongmario](https://github.com/Rongmario) in [a5292ca](https://github.com/CleanroomMC/Cleanroom/commit/a5292ca1893115805efc910193c074f8a04ca02d)]*
- Bump conventions to 1.1.9 and format the cleanroom and builtin modules *[commit by [@Rongmario](https://github.com/Rongmario) in [6e9b4c1](https://github.com/CleanroomMC/Cleanroom/commit/6e9b4c16c5fceff5bbf34429ee0c00e062306956)]*
- Adopt the full Cleanroom Conventions *[commit by [@Rongmario](https://github.com/Rongmario) in [64abc92](https://github.com/CleanroomMC/Cleanroom/commit/64abc92f95281346ab17a23b1ded462cdf58d61e)]*
- Separate run task for kirino enabled client environment + CG update *[commit by [@Rongmario](https://github.com/Rongmario) in [4ba7234](https://github.com/CleanroomMC/Cleanroom/commit/4ba7234839a3eae4b2dd42705b2b29e7a238247e)]*
- Update CG to 0.17.3 *[commit by [@Rongmario](https://github.com/Rongmario) in [6b4dd0e](https://github.com/CleanroomMC/Cleanroom/commit/6b4dd0ec0f0e3d2e949af6bc31e227e3eb6142c1)]*
- Adopt Conventions base plugin and shared workflows *[commit by [@Rongmario](https://github.com/Rongmario) in [b4e122b](https://github.com/CleanroomMC/Cleanroom/commit/b4e122b7bebd07be7bbb806957b8a68f5dbe1368)]*
- Excluded `com.ibm.icu:icu4j-core-mojang`, `com.mojang:patchy` *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- Exclude LWJGL 2 natives from runtime *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- Remove google's `jsr305`, documented jakarta/java 8's javax differences *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*
- Add `ca.weblite:java-objc-bridge:1.2` back *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*

### Other

- --------- *[PR by [@Rongmario](https://github.com/Rongmario), tttsaurus in [#606](https://github.com/CleanroomMC/Cleanroom/pull/606)]*

### First-time Contributors

- **[@ndellagrotte](https://github.com/ndellagrotte) made their first contribution!**
- **[@gardenevery](https://github.com/gardenevery) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.13-alpha...0.7.0-alpha

## 0.6.13-alpha - 2026-09-12

### Performance

- **asm**: Precheck annotation in SideTransformer *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [381edb2](https://github.com/CleanroomMC/Cleanroom/commit/381edb20f7c7c86596a3efe129dc9927da67b7f1)]*

### Refactor

- **mod-list**: Clean up impl mess *[PR by [@RuiXuqi](https://github.com/RuiXuqi) in [#630](https://github.com/CleanroomMC/Cleanroom/pull/630)]*

### Documentation

- Update CHANGELOG.md for 0.6.12-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [fe96cc1](https://github.com/CleanroomMC/Cleanroom/commit/fe96cc1a14631d0ffdbb42d1c840caba0990d720)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.12-alpha...0.6.13-alpha

## 0.6.12-alpha - 2026-08-26

### Bug Fix

- **startup**: Query finish call `onGuiClosed` *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#615](https://github.com/CleanroomMC/Cleanroom/pull/615)]*
    *[Fixes issue #613 by [@ukmojb](https://github.com/ukmojb)]*
- **startup**: Update confirm method to handle null results *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#614](https://github.com/CleanroomMC/Cleanroom/pull/614)]*
- **discovery**: Resolve #620 by filtering null/blank mod ids *[commit by [@Rongmario](https://github.com/Rongmario) in [f9dd525](https://github.com/CleanroomMC/Cleanroom/commit/f9dd525e1d06c66a92b163b8d106a5669d326bd5), issue by [@Apocollis](https://github.com/Apocollis)]*

### Documentation

- Update CHANGELOG.md for 0.6.11-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [7695d38](https://github.com/CleanroomMC/Cleanroom/commit/7695d38a08f398f8732f3a40d56b580cb6a2d610)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.11-alpha...0.6.12-alpha

## 0.6.11-alpha - 2026-08-17

### Feature

- Add side annotation to Config *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4ba1434](https://github.com/CleanroomMC/Cleanroom/commit/4ba1434f0b5110d8a55f08c88d92f237ed0b9ba7)]*
- Add debounce for progress *[commit by [@baka-gourd](https://github.com/baka-gourd) in [37281f3](https://github.com/CleanroomMC/Cleanroom/commit/37281f3960318d4beb626108d5d8dda609988289)]*

### Bug Fix

- **mod-list**: Query crashing when trying to find icons for mods via creative tabs *[commit by [@Rongmario](https://github.com/Rongmario) in [1e33d2f](https://github.com/CleanroomMC/Cleanroom/commit/1e33d2f7792c6bbacb20b43ea2283e580fcd03ee)]*
- **mod-list**: Compatible with OptiFine once again, resolves #600 *[commit by [@Rongmario](https://github.com/Rongmario) in [f8f27e4](https://github.com/CleanroomMC/Cleanroom/commit/f8f27e444a1020889b957ef4484c110144a954d0), issue by [@MKxingsheng](https://github.com/MKxingsheng)]*
- **discovery**: Add explicit support `Mod Director` and `JSON Paintings` *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#610](https://github.com/CleanroomMC/Cleanroom/pull/610)]*
- CleanroomClient crash on macOS due to an unexpected exclusion of java-objc-bridge-1.2 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [80d028f](https://github.com/CleanroomMC/Cleanroom/commit/80d028f6da1f4afca1da905a4481bd4d678516cc)]*

### Refactor

- **ui**: Unify `openFile` calls *[PR by [@RuiXuqi](https://github.com/RuiXuqi) in [#601](https://github.com/CleanroomMC/Cleanroom/pull/601)]*
- **discovery**: Use `MetadataCollection::from` to parse `mcmod.info` files *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#595](https://github.com/CleanroomMC/Cleanroom/pull/595)]*

### Documentation

- Update CHANGELOG.md for 0.6.10-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [406a808](https://github.com/CleanroomMC/Cleanroom/commit/406a8082cc17e5121dd6fad18702ff01aae3eaa4)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.10-alpha...0.6.11-alpha

## 0.6.10-alpha - 2026-08-10

### Feature

- **commands**: Modern suggestions, completions, argument highlighting *[PR by [@Rongmario](https://github.com/Rongmario) in [#557](https://github.com/CleanroomMC/Cleanroom/pull/557)]*

### Bug Fix

- **commands**: Only display visible amount of command suggestions *[commit by [@Rongmario](https://github.com/Rongmario) in [90669ae](https://github.com/CleanroomMC/Cleanroom/commit/90669ae4790d03d715e2c21d0560f6050e8740c8)]*
- **cleanmix**: Update to 0.7.2 to fix #603 *[commit by [@Rongmario](https://github.com/Rongmario) in [628ab32](https://github.com/CleanroomMC/Cleanroom/commit/628ab3278ecfebe3ad29114e0ef34ebbf81e2cc4), issue by [@bartoszkiljanski](https://github.com/bartoszkiljanski)]*
- **discovery**: Duplicate sites on loading crl.dev.mixin configs *[commit by [@Rongmario](https://github.com/Rongmario) in [67b5788](https://github.com/CleanroomMC/Cleanroom/commit/67b57883dc0249ec7d473c273616191fbd1179dd)]*

### Documentation

- Update CHANGELOG.md for 0.6.9-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [7e89265](https://github.com/CleanroomMC/Cleanroom/commit/7e892656f3c7cab2ff35061908a93bc1d63ba8dc)]*

### CI

- Update actions in workflow *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [dddb6a3](https://github.com/CleanroomMC/Cleanroom/commit/dddb6a37059fee4f7d30179bba2402bdf1d37cd7)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.9-alpha...0.6.10-alpha

## 0.6.9-alpha - 2026-08-08

### Bug Fix

- **os**: Open files the intended way *[commit by [@Rongmario](https://github.com/Rongmario) in [7874a07](https://github.com/CleanroomMC/Cleanroom/commit/7874a07403d2188347720f775cfb051da62ac04e)]*
- **discovery**: Queues crl.dev.mixin configurations later, resolves #599 *[commit by [@Rongmario](https://github.com/Rongmario) in [f5dd105](https://github.com/CleanroomMC/Cleanroom/commit/f5dd1055ab49173078f470e287ebd69c8851c7d5), issue by [@FuBangkun](https://github.com/FuBangkun)]*
- **console**: Guard console appender from writing to a closed terminal during shutdown *[PR by [@mvanhorn](https://github.com/mvanhorn) in [#579](https://github.com/CleanroomMC/Cleanroom/pull/579)]*
    *[Fixes issue #566 by [@NoDMoon](https://github.com/NoDMoon)]*
- **discovery**: Always scan Mod annotation *[commit by [@friendlyhj](https://github.com/friendlyhj) in [41047ab](https://github.com/CleanroomMC/Cleanroom/commit/41047ab1c3aa21b5002792b8b8461e98b6fb9f53)]*
- Patchy jar may crash cleanroomClient in some case *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fd78c16](https://github.com/CleanroomMC/Cleanroom/commit/fd78c16fee1841d1765f80d738b527f3f57f7993)]*
- Should fix #576 can't place block agains modded grass *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [388e90f](https://github.com/CleanroomMC/Cleanroom/commit/388e90fd856d50edfde6deec798ebda06b6ce14b), issue by [@IO2I](https://github.com/IO2I)]*
- **mod-list**: Issues fixed pulled in due to author nagging over and over and over again *[PR by [@RuiXuqi](https://github.com/RuiXuqi) in [#592](https://github.com/CleanroomMC/Cleanroom/pull/592)]*

### Documentation

- Update CHANGELOG.md for 0.6.8-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [0cd52d6](https://github.com/CleanroomMC/Cleanroom/commit/0cd52d65ff212683a7cd0033fad4acd089fbfecf)]*

### First-time Contributors

- **[@mvanhorn](https://github.com/mvanhorn) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.8-alpha...0.6.9-alpha

## 0.6.8-alpha - 2026-07-28

### Bug Fix

- **mixinextras**: Allows late ME environments to register with no issue *[commit by [@Rongmario](https://github.com/Rongmario) in [9946eb1](https://github.com/CleanroomMC/Cleanroom/commit/9946eb1f17a66a72d518e5e4a92d45c62c6d33fc)]*

### Performance

- Rewrite EventSubscriptionTransformer in core API *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5df027d](https://github.com/CleanroomMC/Cleanroom/commit/5df027d89e6ffcab116d271a3c1329dfc67811da)]*
- Optimized EventSubscriptionTransformer by reading super w/ ClassReader *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6956d37](https://github.com/CleanroomMC/Cleanroom/commit/6956d37ecd1b4397de97e226272b290549239e1a)]*

### Documentation

- Update CHANGELOG.md for 0.6.7-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [0c5490d](https://github.com/CleanroomMC/Cleanroom/commit/0c5490db41d27403d8402555328ec1e2930825eb)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.7-alpha...0.6.8-alpha

## 0.6.7-alpha - 2026-07-26

### Bug Fix

- **cleanmix**: Update to 0.7.0, fixes variety of issues *[commit by [@Rongmario](https://github.com/Rongmario) in [2d83b07](https://github.com/CleanroomMC/Cleanroom/commit/2d83b071fc959a4885eb1049258bb6900d7ff4d5)]*

### Documentation

- Update CHANGELOG.md for 0.6.6-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [4f7c6b5](https://github.com/CleanroomMC/Cleanroom/commit/4f7c6b50b19b8a10c0c82360d7efa46b7b88336d)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.6-alpha...0.6.7-alpha

## 0.6.6-alpha - 2026-07-24

### Documentation

- Update CHANGELOG.md for 0.6.5-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [8e82cda](https://github.com/CleanroomMC/Cleanroom/commit/8e82cda7a4ea02eaa46b4cdfd77d8380aca66c0b)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.5-alpha...0.6.6-alpha

## 0.6.5-alpha - 2026-07-24

### Feature

- **cleanmix**: Update to 0.6.0, more effort for backwards compatibility *[commit by [@Rongmario](https://github.com/Rongmario) in [25f1c0f](https://github.com/CleanroomMC/Cleanroom/commit/25f1c0f0640f9feb0fe6ca58b7ad85aca0489fbe)]*

### Bug Fix

- **discovery**: Removed unused mixin configs parameter *[commit by [@Rongmario](https://github.com/Rongmario) in [dfd2bca](https://github.com/CleanroomMC/Cleanroom/commit/dfd2bcaf4154fc660d053dfac38cc86ba7dd1f30)]*
- **discovery**: Unable to grab mod ids from certain metadata files *[commit by [@Rongmario](https://github.com/Rongmario) in [a4bc796](https://github.com/CleanroomMC/Cleanroom/commit/a4bc796c0074168f0f72ae8680af251fab716c77)]*

### Performance

- **discovery**: Remove additional candidate deduplication check *[commit by [@Rongmario](https://github.com/Rongmario) in [044356b](https://github.com/CleanroomMC/Cleanroom/commit/044356b4d7c2693cf1c97b6c7b74e97885308c6e)]*
- **discovery**: Optimized deduplication checks for mod candidates *[commit by [@Rongmario](https://github.com/Rongmario) in [3ac1213](https://github.com/CleanroomMC/Cleanroom/commit/3ac121329bc156367fc14093bd06f2d5c785afbb)]*
- **discovery**: Faster deduplication checks when gathering mods *[commit by [@Rongmario](https://github.com/Rongmario) in [f35a674](https://github.com/CleanroomMC/Cleanroom/commit/f35a674d6cb6b24686e9a8e0981f79cef11197e8)]*
- **discovery**: Walk directory once to find derped installations *[commit by [@Rongmario](https://github.com/Rongmario) in [6fe1114](https://github.com/CleanroomMC/Cleanroom/commit/6fe1114f410c22f86e2ad9cdaab9cca2d7d04f19)]*

### Documentation

- Update CHANGELOG.md for 0.6.4-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [914b3d4](https://github.com/CleanroomMC/Cleanroom/commit/914b3d420585b34802f3d455997cb98382633848)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.4-alpha...0.6.5-alpha

## 0.6.4-alpha - 2026-07-23

### Bug Fix

- **cleanmix**: Updated to fix accessors to eagerly load their targets *[commit by [@Rongmario](https://github.com/Rongmario) in [c001d07](https://github.com/CleanroomMC/Cleanroom/commit/c001d07f9c41fe71347d297e5e6ee0553f4143e4)]*

### Documentation

- Update CHANGELOG.md for 0.6.3-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [05b4553](https://github.com/CleanroomMC/Cleanroom/commit/05b4553afa58969483dada3e935429ecc073592a)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.3-alpha...0.6.4-alpha

## 0.6.3-alpha - 2026-07-22

### Feature

- **version check**: Add header of cleanroom *[commit by [@baka-gourd](https://github.com/baka-gourd) in [f0ef797](https://github.com/CleanroomMC/Cleanroom/commit/f0ef79765c2dc044e07226be41dd527e2c1408ab)]*

### Bug Fix

- **cleanmix**: Updated to fix various mixin loading bugs *[commit by [@Rongmario](https://github.com/Rongmario) in [daee859](https://github.com/CleanroomMC/Cleanroom/commit/daee859456fc067caa1cc0beef1b4827e19e124d)]*
- **discovery**: Handle directory sources *[commit by [@Rongmario](https://github.com/Rongmario) in [64af19c](https://github.com/CleanroomMC/Cleanroom/commit/64af19c3008c2533aca71260b862d85975baf6dd)]*

### Documentation

- Update CHANGELOG.md for 0.6.2-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [5294ea0](https://github.com/CleanroomMC/Cleanroom/commit/5294ea031d07610e892546dbd4b642a0dad7f2c9)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.2-alpha...0.6.3-alpha

## 0.6.2-alpha - 2026-07-20

### Bug Fix

- **discovery**: Don't parse built-in mods as discovered files *[commit by [@Rongmario](https://github.com/Rongmario) in [7e8bc27](https://github.com/CleanroomMC/Cleanroom/commit/7e8bc270a3b0fb31161ee64976185fd3d0a0c1a0)]*

### Documentation

- Update CHANGELOG.md for 0.6.1-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [d9f8b3d](https://github.com/CleanroomMC/Cleanroom/commit/d9f8b3d6408507db8c2182e7d8df8fe9a927e305)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.1-alpha...0.6.2-alpha

## 0.6.1-alpha - 2026-07-20

### Bug Fix

- **mod-list**: Scrollbar sensitive list width *[commit by [@Rongmario](https://github.com/Rongmario) in [dcffd58](https://github.com/CleanroomMC/Cleanroom/commit/dcffd580f565e0c0c310b5204f2241111c9c037e)]*
- **mod-list**: Consider scrollbar width when trimming text *[commit by [@Rongmario](https://github.com/Rongmario) in [568757b](https://github.com/CleanroomMC/Cleanroom/commit/568757bd70e0813ca983534c2da2e486d1ec11ae)]*
- **mod-list**: Set parent via `GuiModList`'s parent *[commit by [@Rongmario](https://github.com/Rongmario) in [26bc611](https://github.com/CleanroomMC/Cleanroom/commit/26bc6115d0cc2c41071443c4308a107bad8de7d4)]*

### Documentation

- Update CHANGELOG.md for 0.6.0-alpha [skip ci] *[commit by [@Rongmario](https://github.com/Rongmario) in [95b4af4](https://github.com/CleanroomMC/Cleanroom/commit/95b4af460e1a978de4e038195299e30425230916)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.6.0-alpha...0.6.1-alpha

## 0.6.0-alpha - 2026-07-19

### Feature

- **cleanmix**: Achieve parity with MixinBooter 11 *[commit by [@Rongmario](https://github.com/Rongmario) in [ed8e356](https://github.com/CleanroomMC/Cleanroom/commit/ed8e3564f75eeefa698766eb25ff50c136836960)]*
- **mod-list**: Implemented modern mod list UI *[commit by [@Rongmario](https://github.com/Rongmario), RuiXuqi in [bd4f552](https://github.com/CleanroomMC/Cleanroom/commit/bd4f552b80d3089f9e8909a323261da0b1e09aad)]*

### Bug Fix

- **mod-list**: Add `cleanmix` to default catalogue libraries *[commit by [@Rongmario](https://github.com/Rongmario) in [69f118c](https://github.com/CleanroomMC/Cleanroom/commit/69f118cb4abe123d5e3f0a3365623474ff8e751a)]*
- **patch-mods**: Check if patch mods are present outside of dev *[commit by [@Rongmario](https://github.com/Rongmario) in [fbf9fc2](https://github.com/CleanroomMC/Cleanroom/commit/fbf9fc255ed3940d0801d16b8416bd4ed9b6daaa)]*
- Vanilla json using broken objc-bridge *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e01a298](https://github.com/CleanroomMC/Cleanroom/commit/e01a29831376f15fa8f993605a81cf135514dacd)]*

### Performance

- **discovery**: Speed up `ASMDataTable::getAnnotationsFor` (O(n^2) -> O(n)) *[commit by [@romanfedyniak](https://github.com/romanfedyniak) in [6b8c126](https://github.com/CleanroomMC/Cleanroom/commit/6b8c1260fec2af78aa1ed353e720ef8e3a61511e)]*

### Refactor

- **mod-list**: Re-situate related event listeners *[commit by [@Rongmario](https://github.com/Rongmario) in [a476dbe](https://github.com/CleanroomMC/Cleanroom/commit/a476dbe805853de6af764e2d85cd66beb2303532)]*

### Documentation

- Update CHANGELOG.md for 0.5.17-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fcb1abd](https://github.com/CleanroomMC/Cleanroom/commit/fcb1abd5b0bbd58968376d84ab6690da456caeb6)]*

### CI

- Remove duplicated workflow trigger *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [38cd626](https://github.com/CleanroomMC/Cleanroom/commit/38cd62606b64ecb8638f7f88d16d249be3d47dc7)]*

### First-time Contributors

- **[@romanfedyniak](https://github.com/romanfedyniak) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.17-alpha...0.6.0-alpha

## 0.5.17-alpha - 2026-07-09

### Bug Fix

- Installer can't install server *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a436b61](https://github.com/CleanroomMC/Cleanroom/commit/a436b61ad97099497ac8ebda48819bb66d833d86)]*

### Documentation

- Update CHANGELOG.md for 0.5.16-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [535317d](https://github.com/CleanroomMC/Cleanroom/commit/535317d76ade894d3c31def4f600b592a6e56f8f)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.16-alpha...0.5.17-alpha

## 0.5.16-alpha - 2026-07-09

### Feature

- New installer and client.json compat w/ official launcher *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [db96747](https://github.com/CleanroomMC/Cleanroom/commit/db967479f558d66f3121259b82056a6a044b3258)]*
- Allow flash can be disabled *[commit by [@baka-gourd](https://github.com/baka-gourd) in [0fff847](https://github.com/CleanroomMC/Cleanroom/commit/0fff847600fcca3a3fd5d1a95e9aa57230233000)]*
- Early config now available in mod config menu *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [83ea6a4](https://github.com/CleanroomMC/Cleanroom/commit/83ea6a4b820d732f5aa6fde122a03a74002fa1c4)]*

### Bug Fix

- Fugue warning sometimes hang the game after pressed any key *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c0e60dc](https://github.com/CleanroomMC/Cleanroom/commit/c0e60dcebd3960fb871a147779d82ed78bbdf591)]*
- Ignore relation of all built-in mods *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b027c41](https://github.com/CleanroomMC/Cleanroom/commit/b027c41715dedad0f5d7e407aafbaa2478cd1bef)]*

### Documentation

- Update CHANGELOG.md for 0.5.15-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b6ada5b](https://github.com/CleanroomMC/Cleanroom/commit/b6ada5b6469dcb27fb7bff0cdbe57a86abbac8a6)]*

### Build and Dependencies

- Update Reflect lib *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [cd5fe1f](https://github.com/CleanroomMC/Cleanroom/commit/cd5fe1f6547bd1cd48756a81c3797b057d62aa14)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.15-alpha...0.5.16-alpha

## 0.5.15-alpha - 2026-06-29

### Feature

- Warn users if Fugue or Scalar not installed *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b5abee9](https://github.com/CleanroomMC/Cleanroom/commit/b5abee957b17fa405f99217ff9303ce60387bde1)]*
- Warn if Fugue or Scalar not installed *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e545c03](https://github.com/CleanroomMC/Cleanroom/commit/e545c037760efb34ed029834262348749810ce52)]*

### Bug Fix

- Not wrans right after coremod *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8789256](https://github.com/CleanroomMC/Cleanroom/commit/87892564cb7763f3e512d450ed2ed5c5bf914eb7)]*

### Documentation

- Update CHANGELOG.md for 0.5.14-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fc4ce6a](https://github.com/CleanroomMC/Cleanroom/commit/fc4ce6a10da5f53e1cc9f18af6182ea5760fa1d2)]*

### Build and Dependencies

- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [14deae3](https://github.com/CleanroomMC/Cleanroom/commit/14deae3957865fc69e77b464396c2d69733fd585)]*
- Update deps *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ef9073f](https://github.com/CleanroomMC/Cleanroom/commit/ef9073f1f590aa0584ff2632d358fcaf80616853)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [369b3ff](https://github.com/CleanroomMC/Cleanroom/commit/369b3ff69a2027be1cee02f76d296cc145bbb980)]*
- Merge branch 'main' into feat/patch_mod_warn *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [884d63b](https://github.com/CleanroomMC/Cleanroom/commit/884d63ba58857392f79432b9ae757318b1cf88d9)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f865a30](https://github.com/CleanroomMC/Cleanroom/commit/f865a30419152aa45a1a28cf207e628ffe4d2bab)]*
- Cache Utils.getArtifacts *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d89b393](https://github.com/CleanroomMC/Cleanroom/commit/d89b393f1a9f92b7082bb7f4f2ce48b7ec5f1543)]*
- Move MMC python script to groovy *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6cc0f70](https://github.com/CleanroomMC/Cleanroom/commit/6cc0f705e9c990b016df91563ca0cbc41874f3e5)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b009d4c](https://github.com/CleanroomMC/Cleanroom/commit/b009d4cccf5952576aba0a3959d753d0da80c19b)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.14-alpha...0.5.15-alpha

## 0.5.14-alpha - 2026-06-10

### Bug Fix

- Userdev jar is broken *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [43bcf47](https://github.com/CleanroomMC/Cleanroom/commit/43bcf475eb350be2ea6b9b8e10153d8ecbabe29e)]*

### Documentation

- Update CHANGELOG.md for 0.5.13-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4dde284](https://github.com/CleanroomMC/Cleanroom/commit/4dde28404b95fc701642ff37de5a2c4e7ad356aa)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.13-alpha...0.5.14-alpha

## 0.5.13-alpha - 2026-06-10

### Bug Fix

- Can't discover in-dev mod when multi-langs *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8843e7b](https://github.com/CleanroomMC/Cleanroom/commit/8843e7b991f3ce08a555d116469fe51fdcc08ec0)]*

### Documentation

- Update CHANGELOG.md for 0.5.12-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0215603](https://github.com/CleanroomMC/Cleanroom/commit/0215603c4d7cf09d832a494b37d3fc37cfa84522)]*

### Build and Dependencies

- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [878ff64](https://github.com/CleanroomMC/Cleanroom/commit/878ff64b286d98dc19fb9c4c78ac7ca3cdb4a218)]*
- Update deps *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [62bbd69](https://github.com/CleanroomMC/Cleanroom/commit/62bbd6970dce0bd734440f86c619ce69f4a0b2d0)]*
- Update netty *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [888c10e](https://github.com/CleanroomMC/Cleanroom/commit/888c10ece1f8bb682a5225977c0185222410f4bd)]*
- Dirty hack to speed up most tasks *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a1092ea](https://github.com/CleanroomMC/Cleanroom/commit/a1092eae8aeec72793345846506c07852efb9545)]*
- Some deps for Apache *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9254f23](https://github.com/CleanroomMC/Cleanroom/commit/9254f23f2485f0dab8db1535668a9e54be72cb83)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.12-alpha...0.5.13-alpha

## 0.5.12-alpha - 2026-05-09

### Bug Fix

- Lwjglxx and OpenGlHelper not using new oshi path *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0058f2f](https://github.com/CleanroomMC/Cleanroom/commit/0058f2fb9092c6909ca5409da1ca88dd11059f92)]*

### Documentation

- Update CHANGELOG.md for 0.5.11-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [070cd7f](https://github.com/CleanroomMC/Cleanroom/commit/070cd7fa708e7e4b2852af6ba4ab1cd3efda8a7d)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.11-alpha...0.5.12-alpha

## 0.5.11-alpha - 2026-05-09

### Bug Fix

- Oshi now divided into two packages *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ecdd87b](https://github.com/CleanroomMC/Cleanroom/commit/ecdd87bd33e2eb870bc264dd0407c4533450051e)]*

### Documentation

- Update CHANGELOG.md for 0.5.10-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [68fbf13](https://github.com/CleanroomMC/Cleanroom/commit/68fbf137d1dc3c62aa4389d82e12b44ddcbb7e6c)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.10-alpha...0.5.11-alpha

## 0.5.10-alpha - 2026-05-09

### Bug Fix

- Typesetting in FontRenderer *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [06824ad](https://github.com/CleanroomMC/Cleanroom/commit/06824ad4e9761d60b52b2c25b3345d4947130a5c)]*

### Documentation

- Update CHANGELOG.md for 0.5.9-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9aaaee7](https://github.com/CleanroomMC/Cleanroom/commit/9aaaee759652f5b9c33acf94d6a63b8616eed1e8)]*

### Build and Dependencies

- Update Gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fddce43](https://github.com/CleanroomMC/Cleanroom/commit/fddce43d8fe42e68c3dd4d3d7bb4f0ff30e395a1)]*
- Revert "build: change lwjglx dummy order" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [df489f6](https://github.com/CleanroomMC/Cleanroom/commit/df489f618ec9df5ca689f45c50e89f0145c4897e)]*
- Revert kirino *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [09a51f0](https://github.com/CleanroomMC/Cleanroom/commit/09a51f0e1ef6fbc64bb9b0828a6b6e14f996a5fc)]*
- Change lwjglx dummy order *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4688935](https://github.com/CleanroomMC/Cleanroom/commit/4688935c732bacf40de86b785469635e21a5731b)]*
- Update deps *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e30bec0](https://github.com/CleanroomMC/Cleanroom/commit/e30bec0e46ecba8d056bef48535288fef46837cc)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.9-alpha...0.5.10-alpha

## 0.5.9-alpha - 2026-04-26

### Feature

- Aggresive loading tracker *[PR by [@baka-gourd](https://github.com/baka-gourd) in [#553](https://github.com/CleanroomMC/Cleanroom/pull/553)]*

### Documentation

- Update CHANGELOG.md for 0.5.8-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [581401e](https://github.com/CleanroomMC/Cleanroom/commit/581401e5db15467b51311ba1b9f9a504a204a7de)]*

### Other

- Update mixin extra *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e238bed](https://github.com/CleanroomMC/Cleanroom/commit/e238bed837942573424ab12c02210afdec32ef59)]*
- Uncomment objc bridge *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7be307d](https://github.com/CleanroomMC/Cleanroom/commit/7be307dc2ad50c2f8bb1e91a1360d3728dd4aea6)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.8-alpha...0.5.9-alpha

## 0.5.8-alpha - 2026-04-08

### Documentation

- Update CHANGELOG.md for 0.5.7-alpha [skip ci] *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [382824a](https://github.com/CleanroomMC/Cleanroom/commit/382824a1794e02838020b9d6fdac594239f21c05)]*

### CI

- Display others in commit changelog *[commit by [@baka-gourd](https://github.com/baka-gourd) in [81f3a79](https://github.com/CleanroomMC/Cleanroom/commit/81f3a7992a610387d0d3c3075c45d29113bcf933)]*

### Other

- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2a723e2](https://github.com/CleanroomMC/Cleanroom/commit/2a723e2d47b6607942c3f209de364e716dadb4c6)]*
- Update jline *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d027e4d](https://github.com/CleanroomMC/Cleanroom/commit/d027e4d6a54eb33d779ebf13be8a5861525c16f1)]*
- Switch out from imaginebreaker *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1ebbe41](https://github.com/CleanroomMC/Cleanroom/commit/1ebbe4193afb953350e1d231741f93c18241d60c)]*
- Update deps *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [989612d](https://github.com/CleanroomMC/Cleanroom/commit/989612dfa7333f16180cd7310c53b2b46cc3dc69)]*
- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5ef9cf9](https://github.com/CleanroomMC/Cleanroom/commit/5ef9cf9b093bea2934a3b6f5dfa432d5be952df1)]*
- Add dep for Reflect *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [173a914](https://github.com/CleanroomMC/Cleanroom/commit/173a914ff4fad253d392c26d2d4253b5860c2a08)]*
- Update Reflect, add vararg enum test *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f100bc8](https://github.com/CleanroomMC/Cleanroom/commit/f100bc88b83daca1337c753a2a0038e48bb3a8a0)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.7-alpha...0.5.8-alpha

## 0.5.7-alpha - 2026-03-31

### Feature

- Add progressbar config *[commit by [@baka-gourd](https://github.com/baka-gourd) in [7646e15](https://github.com/CleanroomMC/Cleanroom/commit/7646e157d6937272d9374a0c4c90dc222e5d4002)]*
- Add subprogress *[commit by [@baka-gourd](https://github.com/baka-gourd) in [8acf305](https://github.com/CleanroomMC/Cleanroom/commit/8acf305102d8ed7435b223a11f36dee93c9a6065)]*
- Truly progress counting *[commit by [@baka-gourd](https://github.com/baka-gourd) in [bc98b18](https://github.com/CleanroomMC/Cleanroom/commit/bc98b1889a06ca43413281d51b0c7335338e57c2)]*
- More defensive programming, and add more log *[commit by [@baka-gourd](https://github.com/baka-gourd) in [be42de4](https://github.com/CleanroomMC/Cleanroom/commit/be42de4b6565f2c713d88c962f430dd92cec9d91)]*

### CI

- Add changelog gen *[commit by [@baka-gourd](https://github.com/baka-gourd) in [e992a89](https://github.com/CleanroomMC/Cleanroom/commit/e992a89f22553e846186ff7ad7093e29a3c2ed5a)]*

### Other

- Kirino update: fix gl unit tests failing in actions *[PR by [@tttsaurus](https://github.com/tttsaurus) in [#541](https://github.com/CleanroomMC/Cleanroom/pull/541)]*
- Roll back lwjgl to avoid JIT perf hit *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [11881a8](https://github.com/CleanroomMC/Cleanroom/commit/11881a8113fcafe98a5c5ddbb360f53124e3d9e1)]*
- Update kirino: fix potential crash due to strict exe order requirement violated by mixins *[commit by [@tttsaurus](https://github.com/tttsaurus) in [bc730f8](https://github.com/CleanroomMC/Cleanroom/commit/bc730f8b233240cffcc78e5559c4a86736f9765a)]*
- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [bedee69](https://github.com/CleanroomMC/Cleanroom/commit/bedee69777f7d546842f360bde16d2aec219b900)]*
- Update Foundation for a special case *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8a45489](https://github.com/CleanroomMC/Cleanroom/commit/8a45489657875647a79a3d682a862eebafa1324f)]*
- Update some syntax in ForgeHooks to modern *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a9c36c1](https://github.com/CleanroomMC/Cleanroom/commit/a9c36c16f3582ef81936a0e26ce29744d9ce55d6)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.6-alpha...0.5.7-alpha

## 0.5.6-alpha - 2026-03-25

### Bug Fix

- Crash on windows *[commit by [@baka-gourd](https://github.com/baka-gourd) in [8587a6b](https://github.com/CleanroomMC/Cleanroom/commit/8587a6bfb25b384a4b19e96f99f3b0b9a85506a4)]*

### Other

- Fix crash under non-Windows *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [96de833](https://github.com/CleanroomMC/Cleanroom/commit/96de8337a373c6addef705994cc12952b4038cea)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.5-alpha...0.5.6-alpha

## 0.5.5-alpha - 2026-03-25

### Feature

- Add modern windows styles support *[PR by [@baka-gourd](https://github.com/baka-gourd), arcade_kappa in [#534](https://github.com/CleanroomMC/Cleanroom/pull/534)]*

### Other

- Fix userdev config error *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ca7b6a2](https://github.com/CleanroomMC/Cleanroom/commit/ca7b6a290af592447f794fb4ee592d3a0e58b523)]*
- Rewrite typesetting algorithm with new Segmenter API in ICU4J *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#536](https://github.com/CleanroomMC/Cleanroom/pull/536)]*
- Fix build script try to generate libs in every run *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b6373ad](https://github.com/CleanroomMC/Cleanroom/commit/b6373ad49995aac28a4667cadaf951230ed3ebbe)]*
- Remove workflow_dispatch input in action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8bf3d60](https://github.com/CleanroomMC/Cleanroom/commit/8bf3d60d1ce36054fd82cd7ea326b35ef2257536)]*
- Remove deprecated method in ASMTransformerWrapper *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7ce6741](https://github.com/CleanroomMC/Cleanroom/commit/7ce6741786df39fa025125b5fbb31013db104d66)]*
- Update Gradle to 9.4.1 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5e062ad](https://github.com/CleanroomMC/Cleanroom/commit/5e062ad2bc961b8f34033a5758f5bb28c6eddff6)]*
- Update java comapt info in mmc json *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b151d7d](https://github.com/CleanroomMC/Cleanroom/commit/b151d7dbb928641993dda675d87d8286e6ef036a)]*
- Update issue template *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [08b5631](https://github.com/CleanroomMC/Cleanroom/commit/08b5631f75030670d06d9eafb511dab51f317a4f)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.4-alpha...0.5.5-alpha

## 0.5.4-alpha - 2026-03-15

### CI

- Update manually trigger *[commit by [@baka-gourd](https://github.com/baka-gourd) in [0b90768](https://github.com/CleanroomMC/Cleanroom/commit/0b907685feb2999b7690348fa05aed7a13fe9821)]*
- Adjust for new token *[commit by [@baka-gourd](https://github.com/baka-gourd) in [f5e3915](https://github.com/CleanroomMC/Cleanroom/commit/f5e391534135f62bee40f1d9fd6f922bbe408061)]*

### Other

- Implement modern gui scale *[PR by [@RuiXuqi](https://github.com/RuiXuqi) in [#531](https://github.com/CleanroomMC/Cleanroom/pull/531)]*
- Fix Style class patch *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f4160d0](https://github.com/CleanroomMC/Cleanroom/commit/f4160d0bb48dcdcec487b696dbd00d71a3c43916)]*
- Fix typo *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f3e2696](https://github.com/CleanroomMC/Cleanroom/commit/f3e26964a16e8baa6ceb33e49a9cc6ceab8ef69f)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.3-alpha...0.5.4-alpha

## 0.5.3-alpha - 2026-03-14

### Other

- Cleanup imports & use Reflect first *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4a3fd6e](https://github.com/CleanroomMC/Cleanroom/commit/4a3fd6e2b2b30a3dcdb5ba321499f104b074b5d4)]*
- Fix throw in ObjectHolder *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [33888ea](https://github.com/CleanroomMC/Cleanroom/commit/33888ea33949d8ddf2847b55b8c33e4082fd017d)]*
- Update ReflectionHackery *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a0c1d40](https://github.com/CleanroomMC/Cleanroom/commit/a0c1d40759f0e46c0736dc45ac09498f7d18c07a)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.2-alpha...0.5.3-alpha

## 0.5.2-alpha - 2026-03-14

### Other

- Make advanced tooltip configurable *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [830414e](https://github.com/CleanroomMC/Cleanroom/commit/830414e222762c20d498a65fd34737d93d039738)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.1-alpha...0.5.2-alpha

## 0.5.1-alpha - 2026-03-13

### Other

- Update action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1ca22f1](https://github.com/CleanroomMC/Cleanroom/commit/1ca22f12a3444c28a7c8e3f5f7a90462a954f8d7)]*
- Update readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [485540a](https://github.com/CleanroomMC/Cleanroom/commit/485540a5dc2c70fbbd390a833b381cf2ffa260c0)]*
- Move advanced tooltip to event *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c71de67](https://github.com/CleanroomMC/Cleanroom/commit/c71de673915acd773528b3c881558665c292e0d9)]*
- NOT displaying burn time by default *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d7e3485](https://github.com/CleanroomMC/Cleanroom/commit/d7e348500211a0787a8dd9e9a86f132632d0cc4b)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.5.0-alpha...0.5.1-alpha

## 0.5.0-alpha - 2026-03-10

### Feature

- Better debug information (AAInfo) pt.2 *[PR by [@Gebardensprache](https://github.com/Gebardensprache), Copilotfeat: complete---------, Copilot in [#525](https://github.com/CleanroomMC/Cleanroom/pull/525)]*

### Other

- Update deps *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4bdd72c](https://github.com/CleanroomMC/Cleanroom/commit/4bdd72c3ffb927075bd374e818fe99b2226b88b5)]*
- Update build artifact action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9f89bfc](https://github.com/CleanroomMC/Cleanroom/commit/9f89bfc125741a6d8324639be0057137187d79db)]*
- Cleanup hunk in ItemPredicate *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [030e8a2](https://github.com/CleanroomMC/Cleanroom/commit/030e8a2250a8917f88fc30df3d11817e43763201)]*
- Migrate to Java25 + Full patch cleanup *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#524](https://github.com/CleanroomMC/Cleanroom/pull/524)]*
- Add nsight gradle task integration *[PR by [@tttsaurus](https://github.com/tttsaurus), Rongmario in [#520](https://github.com/CleanroomMC/Cleanroom/pull/520)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.4.4-alpha...0.5.0-alpha

## 0.4.4-alpha - 2026-02-19

### Other

- Update Foundation to fix #519 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [74a03dc](https://github.com/CleanroomMC/Cleanroom/commit/74a03dc8b7ceeb52c3b91cb304b08b65363ab7ec), issue by [@MKxingsheng](https://github.com/MKxingsheng)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.4.3-alpha...0.4.4-alpha

## 0.4.3-alpha - 2026-02-13

### Other

- Update readme about submodule and preparing mods *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e6b1add](https://github.com/CleanroomMC/Cleanroom/commit/e6b1add29766edddfc82e8cfd6f0cc5068c8d0ea)]*
- Update kirino: better logging + remove interface `I` prefix *[PR by [@tttsaurus](https://github.com/tttsaurus) in [#518](https://github.com/CleanroomMC/Cleanroom/pull/518)]*
- Cleanup new PR + clarified javadocs and nullability *[commit by [@Rongmario](https://github.com/Rongmario) in [ffa6007](https://github.com/CleanroomMC/Cleanroom/commit/ffa6007044677b587d3d85ab0917bb4b734011d8)]*
- Improve LivingSetAttackEvent *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#29](https://github.com/CleanroomMC/Cleanroom/pull/29)]*
- Add RenderArmEvent *[PR by [@RuiXuqi](https://github.com/RuiXuqi) in [#507](https://github.com/CleanroomMC/Cleanroom/pull/507)]*

### First-time Contributors

- **[@RuiXuqi](https://github.com/RuiXuqi) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.4.2-alpha...0.4.3-alpha

## 0.4.2-alpha - 2026-01-30

### Other

- Update gradle-git *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b9f995a](https://github.com/CleanroomMC/Cleanroom/commit/b9f995a90787b843a9ac1145efe7ab39c79dee0f)]*
- Finally fixed kirino server crash + bump ecs & engine version *[commit by [@tttsaurus](https://github.com/tttsaurus) in [d7f9532](https://github.com/CleanroomMC/Cleanroom/commit/d7f95326237843ec5e8cce11a7044a0ee45bb07f)]*
- Fix kirino server-side crash by moving event listeners to client core *[commit by [@tttsaurus](https://github.com/tttsaurus) in [10e4234](https://github.com/CleanroomMC/Cleanroom/commit/10e423417858d6a1390ed82653631653d5659c27)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.4.1-alpha...0.4.2-alpha

## 0.4.1-alpha - 2026-01-29

### Other

- Update Reflect and junit *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [bd67ce4](https://github.com/CleanroomMC/Cleanroom/commit/bd67ce400055325091550eb4446e1054ed6f4698)]*
- Add gradle plugin portal in case we need more plugins *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5e00bf8](https://github.com/CleanroomMC/Cleanroom/commit/5e00bf8743c912be7f49a3a1abfbcc16377f31a1)]*
- Fix KirinoClientCore usage *[commit by [@tttsaurus](https://github.com/tttsaurus) in [7ca4a41](https://github.com/CleanroomMC/Cleanroom/commit/7ca4a4163e0a37697d519f74c6096f14d988ae52)]*
- Bump kirino engine module to e1.a2 *[commit by [@tttsaurus](https://github.com/tttsaurus) in [a47bdf5](https://github.com/CleanroomMC/Cleanroom/commit/a47bdf5de72abc42991c0ff34532bfad6f42ed12)]*
- Update kirino *[commit by [@tttsaurus](https://github.com/tttsaurus) in [d895c45](https://github.com/CleanroomMC/Cleanroom/commit/d895c455219267de32c4f508a4b6aceb73d203ba)]*
- Split KirinoCore into common,client, and server modules *[commit by [@tttsaurus](https://github.com/tttsaurus) in [4c6fb61](https://github.com/CleanroomMC/Cleanroom/commit/4c6fb613c4765d579efbd67266173f8ba442ab70)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.4.0-alpha...0.4.1-alpha

## 0.4.0-alpha - 2026-01-23

### Other

- Try to fix updating kirno not triggering action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0319313](https://github.com/CleanroomMC/Cleanroom/commit/03193136a9559877bc0f2dc3a335addc0ad03ab0)]*
- Update kirino *[commit by [@tttsaurus](https://github.com/tttsaurus) in [7046a7e](https://github.com/CleanroomMC/Cleanroom/commit/7046a7e457a69dfc0968131739a21635c23acf10)]*
- Fix typo in readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e681891](https://github.com/CleanroomMC/Cleanroom/commit/e681891d22b81cd8e080178daa75b751a1e0efdc)]*
- Separate EntityRenderer & KirinoEngine method handle initialization *[commit by [@tttsaurus](https://github.com/tttsaurus) in [22cde06](https://github.com/CleanroomMC/Cleanroom/commit/22cde06bdb52824e961e1f0bc9e3f02131988057)]*
- Update gitignore *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b1b0784](https://github.com/CleanroomMC/Cleanroom/commit/b1b078417eec58d9c9f6602d776c1a9722f52b56)]*
- Organized deps *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [44a1e59](https://github.com/CleanroomMC/Cleanroom/commit/44a1e591af8622203107860358c0a0f8d991c317)]*
- Disable kirino fsm tests for now *[commit by [@tttsaurus](https://github.com/tttsaurus) in [64cf617](https://github.com/CleanroomMC/Cleanroom/commit/64cf617f2b11b728f9c9ed45bc3741faeabc888f)]*
- Integrate kirino: update patches + update buildscript *[commit by [@tttsaurus](https://github.com/tttsaurus) in [db9bd1c](https://github.com/CleanroomMC/Cleanroom/commit/db9bd1c0370d628f11d410c5b8fed797abe0a8f4)]*
- Add kikiro to sourceSet, handle submodule in action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3b1c547](https://github.com/CleanroomMC/Cleanroom/commit/3b1c547323a56bc59362ba1935b10ed8f6caa7b1)]*
- Add an action for download site update *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [895429e](https://github.com/CleanroomMC/Cleanroom/commit/895429efc0154d64737c3ead1738c3d236eae522)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.35-alpha...0.4.0-alpha

## 0.3.35-alpha - 2026-01-17

### Other

- Nvm still need null check *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [17dae60](https://github.com/CleanroomMC/Cleanroom/commit/17dae6034212182572823b7b6aec0b48d8c1b720)]*
- Update Gradle to 9.3.0 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [89def06](https://github.com/CleanroomMC/Cleanroom/commit/89def0631bf4505be90518aee0dda2a30fef7947)]*
- Fix null crash when mods has no meta *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d086586](https://github.com/CleanroomMC/Cleanroom/commit/d0865864fe3abefb9c4780b0925c3cdffb665681)]*
- Revert "Fix using DummyModContainer wrongly null crash" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [15168e6](https://github.com/CleanroomMC/Cleanroom/commit/15168e62dac1246b88b6e7472887eb974da19807)]*
- Fix using DummyModContainer wrongly null crash *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8572599](https://github.com/CleanroomMC/Cleanroom/commit/8572599b02b10e9dcd02a7101dca03c9ca3d920b)]*
- Update MixinExtra to 0.5.3 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [15ea966](https://github.com/CleanroomMC/Cleanroom/commit/15ea966048fb2f564484bc2a13f02b69f9184a77)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [717b606](https://github.com/CleanroomMC/Cleanroom/commit/717b60619034237c14c8e7cc931c4bf5be37c8f0)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.34-alpha...0.3.35-alpha

## 0.3.34-alpha - 2026-01-10

### Other

- Update oshi and icu4j *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [59dcd1d](https://github.com/CleanroomMC/Cleanroom/commit/59dcd1d0b5db254869de05d56ee75de5512fc854)]*
- Fix EntityDragon patch regression *[PR by [@Rongmario](https://github.com/Rongmario), arcade_kappa in [#502](https://github.com/CleanroomMC/Cleanroom/pull/502)]*
- Remove side only on Vec2f *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [538e48a](https://github.com/CleanroomMC/Cleanroom/commit/538e48a660929695d3a727cc7cec60c6914f6613)]*
- Cleanup util.math *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c5cd672](https://github.com/CleanroomMC/Cleanroom/commit/c5cd67259717abc0ac0c416d271ca8c75cb29abf)]*
- Update MixinExtra to 0.5.2 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8aa4a4b](https://github.com/CleanroomMC/Cleanroom/commit/8aa4a4b9400b04bbb3286c1093f69d5c10f96ce3)]*
- Update MixinExtra *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [060a826](https://github.com/CleanroomMC/Cleanroom/commit/060a826d9e4e8032348c2100c06b1e5acc40586a)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.33-alpha...0.3.34-alpha

## 0.3.33-alpha - 2025-12-28

### Other

- Fix http client 5 crashing *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f675a1f](https://github.com/CleanroomMC/Cleanroom/commit/f675a1fb5a307c94435587513f7052b403c00323)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.32-alpha...0.3.33-alpha

## 0.3.32-alpha - 2025-12-27

### Other

- Fixes MinecraftForge/MinecraftForge#6283 *[commit by [@Rongmario](https://github.com/Rongmario) in [277630c](https://github.com/CleanroomMC/Cleanroom/commit/277630ca0b5f661142d141cea6bec3385cc99cba), issue by [@Player3324](https://github.com/Player3324)]*
- Update readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5e0bc6c](https://github.com/CleanroomMC/Cleanroom/commit/5e0bc6cad83306d8566d957a75c19f2ab5cd5a21)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.31-alpha...0.3.32-alpha

## 0.3.31-alpha - 2025-12-21

### Other

- Update dependencies *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b7240c4](https://github.com/CleanroomMC/Cleanroom/commit/b7240c4ee32cc4a2201b2f7338a9c275751accec)]*
- Update checker URL for cleanroom *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#500](https://github.com/CleanroomMC/Cleanroom/pull/500)]*
- Fix #499 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1e661ed](https://github.com/CleanroomMC/Cleanroom/commit/1e661ed4f613839d80443b1e7752e5831685158d), issue by [@Floi1](https://github.com/Floi1)]*
- Fix some mismatch patched *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c3ce5bd](https://github.com/CleanroomMC/Cleanroom/commit/c3ce5bd3dedd834dcbb742821f4530a82fa4e796)]*
- More string templating *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8a129eb](https://github.com/CleanroomMC/Cleanroom/commit/8a129eb7bc75437d97cba6b01ec913d733214dfb)]*
- Don't report mods with no fingerprinting as potentially tampered with *[PR by [@Desoroxxx](https://github.com/Desoroxxx) in [#498](https://github.com/CleanroomMC/Cleanroom/pull/498)]*
- No more deprecated new JsonParser() *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [503232d](https://github.com/CleanroomMC/Cleanroom/commit/503232d025a158313f9a9fd61255113fc0cd5906)]*
- Add extra properties to mcmod.info *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#497](https://github.com/CleanroomMC/Cleanroom/pull/497)]*
- Fix Gradle deprecation warnings *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f4bc7e8](https://github.com/CleanroomMC/Cleanroom/commit/f4bc7e895c97376872bfa4b49b459ddee740fcb3)]*
- Update Reflect library *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [20103c7](https://github.com/CleanroomMC/Cleanroom/commit/20103c7ff4a133ff82405cc924a7803526f51100)]*
- Fix Loader tries to load dir in dev *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#496](https://github.com/CleanroomMC/Cleanroom/pull/496)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.30-alpha...0.3.31-alpha

## 0.3.30-alpha - 2025-12-08

### Other

- Improve the crash reports `States` section *[PR by [@Desoroxxx](https://github.com/Desoroxxx) in [#495](https://github.com/CleanroomMC/Cleanroom/pull/495)]*
- Rewrite version token replacing into standalone task *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#494](https://github.com/CleanroomMC/Cleanroom/pull/494)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.29-alpha...0.3.30-alpha

## 0.3.29-alpha - 2025-12-07

### Other

- Make Intellij happy with groovy *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [292b28d](https://github.com/CleanroomMC/Cleanroom/commit/292b28dc1bba940f7491c60b708aa762a2c1c8c7)]*
- Bump plugin version *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#493](https://github.com/CleanroomMC/Cleanroom/pull/493)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.28-alpha...0.3.29-alpha

## 0.3.28-alpha - 2025-12-07

### Other

- Add helper methods for GeckoLib *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#492](https://github.com/CleanroomMC/Cleanroom/pull/492)]*
- Change Forge version to 2864 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [03ae112](https://github.com/CleanroomMC/Cleanroom/commit/03ae11265aa1ce2f79a5d385208f48303558e3fc)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0d00624](https://github.com/CleanroomMC/Cleanroom/commit/0d0062495841b1608fb8de2039a3d35d4e74fbe6)]*
- Switch to FG6 fork which supports Gradle 9 *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#490](https://github.com/CleanroomMC/Cleanroom/pull/490)]*
- Reset everything from gradle-9 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e5b987b](https://github.com/CleanroomMC/Cleanroom/commit/e5b987b70f57aea684bd7e84ec93a2583cb1f274)]*
- Update actions *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f646486](https://github.com/CleanroomMC/Cleanroom/commit/f646486ae8a114236e7c008af9ea985807297683)]*
- Adjust build script repository order *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [bf3acdd](https://github.com/CleanroomMC/Cleanroom/commit/bf3acdd7c24192c275bad27219bc03293cec238e)]*
- Improve string representation of listeners for `IContextSetter` event *[PR by [@ZZZank](https://github.com/ZZZank) in [#489](https://github.com/CleanroomMC/Cleanroom/pull/489)]*
- Use custom FG6 so we can update to Gradle 9 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7fcb45c](https://github.com/CleanroomMC/Cleanroom/commit/7fcb45cd4d833ca3c3c809b74dff3e51d50b01df)]*
- Sync upstream Forge version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fd11785](https://github.com/CleanroomMC/Cleanroom/commit/fd11785788ff4154accf42481e43c3e3d4e450a1)]*
- Update Reflect *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [58b5e8f](https://github.com/CleanroomMC/Cleanroom/commit/58b5e8fa43f2789c010b630b6c1e225c6fbb4c79)]*
- Fix 'outdated server' showing in ping before server fully boots *[PR by [@HaHaWTH](https://github.com/HaHaWTH) in [#488](https://github.com/CleanroomMC/Cleanroom/pull/488)]*
- Allow subscribing methods with illegal param type *[PR by [@ZZZank](https://github.com/ZZZank) in [#487](https://github.com/CleanroomMC/Cleanroom/pull/487)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.27-alpha...0.3.28-alpha

## 0.3.27-alpha - 2025-11-27

### Feature

- Auto update download page when release *[commit by [@baka-gourd](https://github.com/baka-gourd) in [3d64c36](https://github.com/CleanroomMC/Cleanroom/commit/3d64c366d11b6a9748ffa7d7b6acca7c5a3dea65)]*

### Other

- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2dbf4ec](https://github.com/CleanroomMC/Cleanroom/commit/2dbf4ec52613890f2922067e3806556b44f66a38)]*
- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fea47f4](https://github.com/CleanroomMC/Cleanroom/commit/fea47f432b2939a40e3d62d7a90a7495a89f8787)]*
- Update foundation for arch-loom *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [15f5c3c](https://github.com/CleanroomMC/Cleanroom/commit/15f5c3ca6cb5d04ceedc56017863065c361778d9)]*
- Update foundation again to fix grs crash *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8cb7fac](https://github.com/CleanroomMC/Cleanroom/commit/8cb7faca91087c1a2ef92429123ffd897ce5c251)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6feb853](https://github.com/CleanroomMC/Cleanroom/commit/6feb85386c3d1b1067170457940227edbd6d1e08)]*
- Update lwjglxx to fix macos crash *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9ee5fef](https://github.com/CleanroomMC/Cleanroom/commit/9ee5fef7cf18ffa7790f7e3e57bf01eb807df613)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a9649fd](https://github.com/CleanroomMC/Cleanroom/commit/a9649fdf361765fb703f3c36312cf91730408454)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.26-alpha...0.3.27-alpha

## 0.3.26-alpha - 2025-11-20

### Other

- Fix all broken patches in client package *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b1154ea](https://github.com/CleanroomMC/Cleanroom/commit/b1154eaaa6d441a623f93228ef4c26450a4e51e7)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.25-alpha...0.3.26-alpha

## 0.3.25-alpha - 2025-11-19

### Other

- Finish cleanup hunks in entity *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [41600e6](https://github.com/CleanroomMC/Cleanroom/commit/41600e6b8c225b0041ed27070b345b60cbc1ab86)]*
- Update kirino *[commit by [@tttsaurus](https://github.com/tttsaurus) in [85bf65a](https://github.com/CleanroomMC/Cleanroom/commit/85bf65a13abb382c622b29b884f5321a5049883f)]*
- Partially cleanup entity hunks *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4bd9faf](https://github.com/CleanroomMC/Cleanroom/commit/4bd9fafe1ce5b705049ee9ed5a1e870a2f578ec5)]*
- Fix submodule *[commit by [@tttsaurus](https://github.com/tttsaurus) in [9116e29](https://github.com/CleanroomMC/Cleanroom/commit/9116e29a8aee92dedcc5a4e3608c760ee59e97b7)]*
- Move kirino submodule to projects/kirino *[commit by [@tttsaurus](https://github.com/tttsaurus) in [18013c8](https://github.com/CleanroomMC/Cleanroom/commit/18013c86a127f4e7a3242842d389dca02c6e26ac)]*
- Clenup hunks in creativetab & enchantment *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d4fd12b](https://github.com/CleanroomMC/Cleanroom/commit/d4fd12b014b21576f82ba2eeca62d812dc203c5c)]*
- Cleanup hunks in net.minecraft.crash *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [28ecdb5](https://github.com/CleanroomMC/Cleanroom/commit/28ecdb5443e5dc391bb05908f1b4b06937c7c575)]*
- Cleanup hunks in net.minecraft.command *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [56b8fc9](https://github.com/CleanroomMC/Cleanroom/commit/56b8fc92037ff9c9e4c60a4a5b1bfa549a35bbc3)]*
- Clean up hunks in net.minecraft.block *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [90ebf86](https://github.com/CleanroomMC/Cleanroom/commit/90ebf86a928f8b73248859e603956376cc5d9813)]*
- Add back http4 since the transition will take a while *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2d83ff4](https://github.com/CleanroomMC/Cleanroom/commit/2d83ff4e8661dfd330faca435e0afbca27388015)]*
- Update LWJGLXX and fully fix cursor *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [74f343a](https://github.com/CleanroomMC/Cleanroom/commit/74f343a15a89455c7f9df6fb19dd814937654eb7)]*
- Update LWJGLXX to fix Cursor not working *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fcd2ef6](https://github.com/CleanroomMC/Cleanroom/commit/fcd2ef6596c39418d620fed7cddb6aee83558294)]*
- Cleanup patch for `client` package (with some leftovers from `advancements` package) *[PR by [@ZZZank](https://github.com/ZZZank) in [#463](https://github.com/CleanroomMC/Cleanroom/pull/463)]*
- Update LWJGLXX *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [bddf46c](https://github.com/CleanroomMC/Cleanroom/commit/bddf46c6d3202a554ef45acf553cde96bede8614)]*
- Update mmcpack json to match latest Prism *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [040d048](https://github.com/CleanroomMC/Cleanroom/commit/040d048f141424f868ab0d16de5cc84db5bac85d)]*
- Fix ItemStack#copy() not copying unserialized cap data *[PR by [@jchung01](https://github.com/jchung01) in [#474](https://github.com/CleanroomMC/Cleanroom/pull/474)]*
- Update lwjglxx for glDebug *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f54b728](https://github.com/CleanroomMC/Cleanroom/commit/f54b7286ece93baab500aa20d82ac07af9aeb805)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [637251f](https://github.com/CleanroomMC/Cleanroom/commit/637251f326785b36ab20acc2409e37618e99e779)]*
- Add kirino submodule *[commit by [@tttsaurus](https://github.com/tttsaurus) in [2d7ddeb](https://github.com/CleanroomMC/Cleanroom/commit/2d7ddeb4a0eb3bbfa31a17beb4682c8c09f01009)]*

### First-time Contributors

- **[@jchung01](https://github.com/jchung01) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.24-alpha...0.3.25-alpha

## 0.3.24-alpha - 2025-11-02

### Other

- Hotfix URI not absolute *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1cb6b06](https://github.com/CleanroomMC/Cleanroom/commit/1cb6b0660a3b6dbdfbdbe0b05bacc229c9d42b66)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.23-alpha...0.3.24-alpha

## 0.3.23-alpha - 2025-11-02

### Other

- Fix trying to load coremod before it in cp *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [62e55a4](https://github.com/CleanroomMC/Cleanroom/commit/62e55a4349e455f55a39e38bfa5ce37a8420ead3)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.22-alpha...0.3.23-alpha

## 0.3.22-alpha - 2025-11-02

### Other

- Fix icu4j typo *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ad5f359](https://github.com/CleanroomMC/Cleanroom/commit/ad5f359bc6f7f50813d2783b5e57d618f894e0ea)]*
- Update deps *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a545bcf](https://github.com/CleanroomMC/Cleanroom/commit/a545bcfb67d0f852a124ebcd98cb23e328a04d93)]*
- Fixed many running-game-in-dev related problems *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#468](https://github.com/CleanroomMC/Cleanroom/pull/468)]*
- Update default mixinbooter version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3bb6127](https://github.com/CleanroomMC/Cleanroom/commit/3bb6127dd8d3d5adb646be50645cd0636b84c23f)]*
- Fix action trigger *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1c7e9d2](https://github.com/CleanroomMC/Cleanroom/commit/1c7e9d2b25a9e4953ea8d90b8520b0d31ab26efc)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b9ecf87](https://github.com/CleanroomMC/Cleanroom/commit/b9ecf870bd08507dff222af95a783211c82dc65f)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.21-alpha...0.3.22-alpha

## 0.3.21-alpha - 2025-10-19

### Other

- Update template since 25 have released *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [68a4bf2](https://github.com/CleanroomMC/Cleanroom/commit/68a4bf22e2bfacc06a892c293689966ea6ef6216)]*
- Fix null manifest and null attributes getting past *[commit by [@Rongmario](https://github.com/Rongmario) in [7dfc4b0](https://github.com/CleanroomMC/Cleanroom/commit/7dfc4b07c18010a59fceea6772fbe1f44b0538b0)]*
- Use MCP mapping for patch *[PR by [@ZZZank](https://github.com/ZZZank) in [#458](https://github.com/CleanroomMC/Cleanroom/pull/458)]*
- Split build script for different projects *[PR by [@ZZZank](https://github.com/ZZZank) in [#461](https://github.com/CleanroomMC/Cleanroom/pull/461)]*
- Bansoukou v6.0.0 interop *[PR by [@Rongmario](https://github.com/Rongmario) in [#460](https://github.com/CleanroomMC/Cleanroom/pull/460)]*
- Ignore `/buildSrc/out/` dir *[PR by [@ZZZank](https://github.com/ZZZank) in [#459](https://github.com/CleanroomMC/Cleanroom/pull/459)]*
- Cleanup patches/minecraft/net/minecraft/block/state *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [66272c8](https://github.com/CleanroomMC/Cleanroom/commit/66272c8759218ddcc64b0489e172327a7e17918f)]*
- Random cleanup *[PR by [@ZZZank](https://github.com/ZZZank) in [#456](https://github.com/CleanroomMC/Cleanroom/pull/456)]*
- Remove hamcrest, opentest4j, updated JUnit to 6 *[commit by [@Rongmario](https://github.com/Rongmario) in [b431152](https://github.com/CleanroomMC/Cleanroom/commit/b4311528ad1b3ecfbf1176c5266fbaa8e5e6cec5)]*
- Revert update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7a636a3](https://github.com/CleanroomMC/Cleanroom/commit/7a636a3baadc8441e20c9dbcf02054bf09f2331a)]*
- Update Foundation to fix Proxy and Args. *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0bd133b](https://github.com/CleanroomMC/Cleanroom/commit/0bd133b053aede63b0cb3357dac39379126cb4ba)]*
- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [74c4365](https://github.com/CleanroomMC/Cleanroom/commit/74c4365b3e6cbf71affdc4bcf00f316eccf9fa27)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.20-alpha...0.3.21-alpha

## 0.3.20-alpha - 2025-10-10

### Other

- Update Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a1bf93a](https://github.com/CleanroomMC/Cleanroom/commit/a1bf93a8b8dc5930d45b277c57eb0de94104758b)]*
- Update Foundation to fix Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d508f39](https://github.com/CleanroomMC/Cleanroom/commit/d508f39a58586e1d900cf15a61d8db5f4a986e4a)]*
- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9b5b073](https://github.com/CleanroomMC/Cleanroom/commit/9b5b073656cce805d517a52403a86839762cea39)]*
- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [740c10f](https://github.com/CleanroomMC/Cleanroom/commit/740c10ff2a06ef3a36873430c01766ab58b76132)]*
- Update Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c95b7b4](https://github.com/CleanroomMC/Cleanroom/commit/c95b7b4612b3489f18336c9e27669cacca2a3628)]*
- Update deps *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e96cf46](https://github.com/CleanroomMC/Cleanroom/commit/e96cf46c248882c413f52969a52ce471428f8a0a)]*
- Publish mmcpack to maven *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f04b8c1](https://github.com/CleanroomMC/Cleanroom/commit/f04b8c1b309f2f0385ce8c3f452b26728c10b423)]*
- Revert "Publish mmcpack to maven. May not work as intented" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1c84cec](https://github.com/CleanroomMC/Cleanroom/commit/1c84cec50ed8168437759f96a9cd711bcc8c2075)]*
- Publish mmcpack to maven. May not work as intented *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [743399c](https://github.com/CleanroomMC/Cleanroom/commit/743399c0939db183a78d5fb11786dfc7a47b6d13)]*
- Update ASM to 9.9 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [cd58400](https://github.com/CleanroomMC/Cleanroom/commit/cd58400070b8b110a2a15830eff9fb8d1955108a)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.19-alpha...0.3.20-alpha

## 0.3.19-alpha - 2025-10-05

### Other

- Fix mmc pack script *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0d83deb](https://github.com/CleanroomMC/Cleanroom/commit/0d83deb7721141eec88d4ab707fe79d38b52ebaf)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.18-alpha...0.3.19-alpha

## 0.3.18-alpha - 2025-10-05

### Other

- Attempts to use maven universal jar in release *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [399dfd4](https://github.com/CleanroomMC/Cleanroom/commit/399dfd406f255f8cf18c8ad0a39fa2e23ff89e73)]*
- Update lwjglxx to fix changed GL not saving *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0201542](https://github.com/CleanroomMC/Cleanroom/commit/020154229bd6d41bc9479d4995771daad00f0227)]*
- Add sync method to coremod configs *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [90fe5ae](https://github.com/CleanroomMC/Cleanroom/commit/90fe5aee933c22692438e6f7072b28a58c645efb)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.17-alpha...0.3.18-alpha

## 0.3.17-alpha - 2025-10-04

### Other

- Update default MixinBooter version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a40280a](https://github.com/CleanroomMC/Cleanroom/commit/a40280ac1fb187430858576cd8165125569811ed)]*
- Fix typo *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c71f60b](https://github.com/CleanroomMC/Cleanroom/commit/c71f60b9fc5c5af1f30d7f0146eb941ec9f0f729)]*
- Massive dependencies update *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [930bd40](https://github.com/CleanroomMC/Cleanroom/commit/930bd404046e65cb560c95a965b69281e0d13993)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f05af78](https://github.com/CleanroomMC/Cleanroom/commit/f05af78ecd3510ebc067366913b6b67f5753c86e)]*
- Add more entries to `ForgeEarlyConfig` to guide the context creation process *[PR by [@tttsaurus](https://github.com/tttsaurus) in [#435](https://github.com/CleanroomMC/Cleanroom/pull/435)]*
- Update Nashorn *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a01e729](https://github.com/CleanroomMC/Cleanroom/commit/a01e7296e76a198bc29b0b508aa6ff43302a5c41)]*

### First-time Contributors

- **[@tttsaurus](https://github.com/tttsaurus) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.16-alpha...0.3.17-alpha

## 0.3.16-alpha - 2025-09-21

### Other

- Fix wrong gradle property in action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [10ca2f0](https://github.com/CleanroomMC/Cleanroom/commit/10ca2f09d3ae75a7bcdc5fe7981be98e1ff8a520)]*
- Prevent loading same coremod twice *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#431](https://github.com/CleanroomMC/Cleanroom/pull/431)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.15-alpha...0.3.16-alpha

## 0.3.15-alpha - 2025-09-13

### Other

- Fix full screen not so full *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#428](https://github.com/CleanroomMC/Cleanroom/pull/428)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.14-alpha...0.3.15-alpha

## 0.3.14-alpha - 2025-09-10

### Bug Fix

- Fix dep bug: netty requests need netty-codec-compression at runtime *[PR by [@barzamin](https://github.com/barzamin) in [#417](https://github.com/CleanroomMC/Cleanroom/pull/417)]*

### Other

- Update netty *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ff88869](https://github.com/CleanroomMC/Cleanroom/commit/ff888691fa4ed4b36deb65dd6d0cbdd577f371ac)]*
- ICON for MMC instance *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#412](https://github.com/CleanroomMC/Cleanroom/pull/412)]*
- Clean up SeverPinger hunks *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [348f19a](https://github.com/CleanroomMC/Cleanroom/commit/348f19a442a4cd4625dc2650944aed3012faac4f)]*
- Cleanup NetworkManager patch hunks *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [809a0bc](https://github.com/CleanroomMC/Cleanroom/commit/809a0bcf81c7e09699bbe0f505d15468161c27c6)]*
- Explain server installation in readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [df0d5f2](https://github.com/CleanroomMC/Cleanroom/commit/df0d5f2d1ecc59f665afc6497f50668e753cbb1d)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1db1e0c](https://github.com/CleanroomMC/Cleanroom/commit/1db1e0c4b2404d797bd44f18bc08a2e34e678e98)]*
- Fix ASMDataTable *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4b41bad](https://github.com/CleanroomMC/Cleanroom/commit/4b41bad13452e34e8282168c8705a93bf1cd12ab)]*
- Fix someone doesn't remember annotation IS interface *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [52243df](https://github.com/CleanroomMC/Cleanroom/commit/52243dfb335b5582942f8e5b17d52d9fb06fe3fd)]*
- Add methods to CleanroomVersion *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [492e5db](https://github.com/CleanroomMC/Cleanroom/commit/492e5dbdc1f703919eafb1dd91647bfe12c2eff3)]*
- Add Helper method to ASMDataTable *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#267](https://github.com/CleanroomMC/Cleanroom/pull/267)]*
- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7b98551](https://github.com/CleanroomMC/Cleanroom/commit/7b9855153760610c2cc31222760346556da9f2b9)]*
- Provide early side checking for Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [59a383e](https://github.com/CleanroomMC/Cleanroom/commit/59a383e041dbd235324ad38a9e98f80c5e7c6071)]*
- Update log4j *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [40d84d3](https://github.com/CleanroomMC/Cleanroom/commit/40d84d301803e7a212146836df7a912c489934cc)]*
- Update MixinExtra *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9d7fbb8](https://github.com/CleanroomMC/Cleanroom/commit/9d7fbb8ccde2b438083afb192f10a76bc32efa99)]*
- Ignore directory in coremod discovery *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [86bd4c5](https://github.com/CleanroomMC/Cleanroom/commit/86bd4c560639a565c7291b4179d64b15db333e04)]*

### First-time Contributors

- **[@barzamin](https://github.com/barzamin) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.13-alpha...0.3.14-alpha

## 0.3.13-alpha - 2025-07-27

### Other

- Give up on only-release-on-main *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9b2e87d](https://github.com/CleanroomMC/Cleanroom/commit/9b2e87d367801bc16b6c59ef67fcac2d76ed2a91)]*
- Use action to get branch name in tag action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [faea17d](https://github.com/CleanroomMC/Cleanroom/commit/faea17df3aa001ce86cd10fb1d18f3bd237149ca)]*
- Update readme description about action builds *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a093356](https://github.com/CleanroomMC/Cleanroom/commit/a093356a7c8f1dab35b5c12dc791591e493d5ea8)]*
- Better branch check *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f37b3a9](https://github.com/CleanroomMC/Cleanroom/commit/f37b3a9daf50eba58b7f1748db753a2d46509f64)]*
- Update action versions *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d153f48](https://github.com/CleanroomMC/Cleanroom/commit/d153f487449b3658d23a9c73ce9079170500371f)]*
- Move userdev removal to release action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [97351f0](https://github.com/CleanroomMC/Cleanroom/commit/97351f0c825957da544b84786da778198df617db)]*
- Fix action not supporting wildcard *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1762fbd](https://github.com/CleanroomMC/Cleanroom/commit/1762fbd475a222157888e2fdbba9a33e2313c647)]*
- Fix wrong rm command *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7c8b304](https://github.com/CleanroomMC/Cleanroom/commit/7c8b304db8a1964d2a98c7d5b6d0ab74d67ccdda)]*
- Triggering action on CI file changed *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e360c3c](https://github.com/CleanroomMC/Cleanroom/commit/e360c3c48b40a2316bf766e8810d119675a75886)]*
- Generate MMC pack locally *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9f821f8](https://github.com/CleanroomMC/Cleanroom/commit/9f821f8c3ae1e1e850e9f867c86a0ae1d26addae)]*
- More final in AT for optimization *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9714dd5](https://github.com/CleanroomMC/Cleanroom/commit/9714dd5b379130ec3e5715df85f52bbf37b3c102)]*
- NOT releasing packet payload in FMLEventChannel *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [cf2c0d2](https://github.com/CleanroomMC/Cleanroom/commit/cf2c0d246839903d2120c04e1b97220d2e9752a5)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.11-alpha...0.3.13-alpha

## 0.3.11-alpha - 2025-07-24

### Documentation

- Add launcher disclaimer *[PR by [@baka-gourd](https://github.com/baka-gourd) in [#389](https://github.com/CleanroomMC/Cleanroom/pull/389)]*

### Other

- Try to release allocated memory at end of event *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [445cf67](https://github.com/CleanroomMC/Cleanroom/commit/445cf6794da664155e5232055852ecededc7fe2f)]*
- Fix netty natives are empty artifact *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [28222e0](https://github.com/CleanroomMC/Cleanroom/commit/28222e0593a3e001d3c9585e89aba0d8445caed1)]*
- Update netty 4.2.3 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [428cc0b](https://github.com/CleanroomMC/Cleanroom/commit/428cc0b2c985937ed66fd412c72af30acd9bbc3a)]*
- Add getter to some deobf mappings *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [594866f](https://github.com/CleanroomMC/Cleanroom/commit/594866fb7f92423cc53ddcfb439c54c5201a6e48)]*
- Update issue template to include scalar legact explicitly *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0b38e65](https://github.com/CleanroomMC/Cleanroom/commit/0b38e6513ee97e6a9b4e497df2c980d57a3d986c)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6d46326](https://github.com/CleanroomMC/Cleanroom/commit/6d463263672c206a1cbf7aebac07115a2dd9a2a4)]*
- Null ModMetadata check *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#385](https://github.com/CleanroomMC/Cleanroom/pull/385)]*
- Remove the configuration file registration restriction. *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#335](https://github.com/CleanroomMC/Cleanroom/pull/335)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4f279ba](https://github.com/CleanroomMC/Cleanroom/commit/4f279baa44b804a281b2219df884af13a09e27b8)]*
- Update to Apache http5 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [57d889e](https://github.com/CleanroomMC/Cleanroom/commit/57d889ee5b6a3663f26aa06bfc49b52ba76b7a47)]*
- Update mixin extra *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2379da4](https://github.com/CleanroomMC/Cleanroom/commit/2379da4d23b4111a927a5fe7aae3c5c0df9bc260)]*
- Add culling to particle item pickup rendering *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [552ed74](https://github.com/CleanroomMC/Cleanroom/commit/552ed7460cd976b057234b6e4edc66efe7904ee2)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [58a29eb](https://github.com/CleanroomMC/Cleanroom/commit/58a29eb32304f95abb7e7cc799f374aa5153fe54)]*

### First-time Contributors

- **[@baka-gourd](https://github.com/baka-gourd) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.10-alpha...0.3.11-alpha

## 0.3.10-alpha - 2025-07-06

### Other

- Fix missing trove4j dep *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6733213](https://github.com/CleanroomMC/Cleanroom/commit/673321311e9402b243f767dae4a7bd15b7d90876)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.9-alpha...0.3.10-alpha

## 0.3.9-alpha - 2025-07-06

### Other

- Update trove4j *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a5d6713](https://github.com/CleanroomMC/Cleanroom/commit/a5d671343bfd93016458bf9a83db68d062f0084a)]*
- Clean up commented dependencies *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b12f061](https://github.com/CleanroomMC/Cleanroom/commit/b12f0615fbdde48c1df14aae704f8de9df10f909)]*
- Yet another dep for jaxb *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [346b112](https://github.com/CleanroomMC/Cleanroom/commit/346b112e93b003aaf716929f25818e8facd4fbae)]*
- Trim the mixin config *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#394](https://github.com/CleanroomMC/Cleanroom/pull/394)]*
- Add jaxb back *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6e31454](https://github.com/CleanroomMC/Cleanroom/commit/6e31454849dd35880e13344c7e33dc27d3f01918)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.8-alpha...0.3.9-alpha

## 0.3.8-alpha - 2025-07-06

### Other

- Dependency adjustment *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6d65c63](https://github.com/CleanroomMC/Cleanroom/commit/6d65c63a7bba51546d1753aef33717e04bd8bbc2)]*
- Allow adding mixin config in dev through preperty *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4b72197](https://github.com/CleanroomMC/Cleanroom/commit/4b72197929e6cef90380aa02da7428f1cd872972)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c2a0967](https://github.com/CleanroomMC/Cleanroom/commit/c2a09670230d0b27c3b1f7139552be71f87dfe9e)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.7-alpha...0.3.8-alpha

## 0.3.7-alpha - 2025-06-23

### Other

- Update oshi & netty *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9a73484](https://github.com/CleanroomMC/Cleanroom/commit/9a734847e6a47fda5ae8eeb6d898172fd7f0f684)]*
- Revert "Add check for entity trackingUpdateFrequency must be positive" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9ac9361](https://github.com/CleanroomMC/Cleanroom/commit/9ac936131742d206e50b1ffd1a7a02006d989321)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.6-alpha...0.3.7-alpha

## 0.3.6-alpha - 2025-06-22

### Other

- Make object holder log more easy to understand *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [991b930](https://github.com/CleanroomMC/Cleanroom/commit/991b930db3ad9d18267f5189840fc9b44e6b77f8)]*
- Add missing null check *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [58f948a](https://github.com/CleanroomMC/Cleanroom/commit/58f948a16501b32c69ef3efeaf41fffa4df254e6)]*
- Change default built-in mod version mechanic *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3480f3e](https://github.com/CleanroomMC/Cleanroom/commit/3480f3e9c88177b75d74686ab3aebc90a8367141)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.5-alpha...0.3.6-alpha

## 0.3.5-alpha - 2025-06-22

### Other

- Revert "EventBus : Enable All access of listener, include private" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6b339c0](https://github.com/CleanroomMC/Cleanroom/commit/6b339c04e27e664b1983db5aa0b2dfd3ce785ead)]*
- Make ItemStack#copy() mimic original behavior *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [dfa69e3](https://github.com/CleanroomMC/Cleanroom/commit/dfa69e34423a81af232d2b583864b9f131efaec5)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5eb71b5](https://github.com/CleanroomMC/Cleanroom/commit/5eb71b507d1068a3042334eaee5c91cbcfa5cbb5)]*
- Fix #383 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ee56571](https://github.com/CleanroomMC/Cleanroom/commit/ee565714f2ae005c5881fc1b9a002b581db68193), issue by [@XZYawa](https://github.com/XZYawa)]*
- EventBus : Enable All access of listener, include private *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb), ZZZank---------, kappa-maintainer, ZZZank in [#358](https://github.com/CleanroomMC/Cleanroom/pull/358)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [87e6649](https://github.com/CleanroomMC/Cleanroom/commit/87e6649e3bf06d1af138a551f971d53cc75616eb)]*
- Action trigger *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1b0c682](https://github.com/CleanroomMC/Cleanroom/commit/1b0c682f34376833ad2375c17ea5697efcd92fe8)]*
- Fix workflow *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e55074a](https://github.com/CleanroomMC/Cleanroom/commit/e55074ac3c46027c1a12f2d07a130ed2d2506e0b)]*
- Update README.md *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d9abc84](https://github.com/CleanroomMC/Cleanroom/commit/d9abc84dcf56a0d6138cd5ac41ee951b713af7ff)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e6f9d96](https://github.com/CleanroomMC/Cleanroom/commit/e6f9d965bc8a9f7fe71d5c86254a9d5155287798)]*
- Update Gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [270954e](https://github.com/CleanroomMC/Cleanroom/commit/270954e8e3cae0deb7e8c048eddbcdd95f6b3584)]*
- Not trying Dispatch CleanroomMMC for fork repository *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#369](https://github.com/CleanroomMC/Cleanroom/pull/369)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a2e2760](https://github.com/CleanroomMC/Cleanroom/commit/a2e276087a120d93dc657c66a9ac735d8525a194)]*
- Add check for entity trackingUpdateFrequency must be positive *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#367](https://github.com/CleanroomMC/Cleanroom/pull/367)]*
- Doc for WorldAccessContainer *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#366](https://github.com/CleanroomMC/Cleanroom/pull/366)]*
- Not `Repository Dispatch` when `secrets.CLEANROOMMC_DISPATCH_TOKEN` miss *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#364](https://github.com/CleanroomMC/Cleanroom/pull/364)]*
- Fix `main` artifical *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#363](https://github.com/CleanroomMC/Cleanroom/pull/363)]*
- Clean Plugins *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#365](https://github.com/CleanroomMC/Cleanroom/pull/365)]*
- Change log level of some coremod logging *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2a3c105](https://github.com/CleanroomMC/Cleanroom/commit/2a3c105169735a6236e12e5e47796063cbe30944)]*
- Update Oshi *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5462036](https://github.com/CleanroomMC/Cleanroom/commit/5462036426b3424b634c378ba4cf54c9b33dd1f6)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.4-alpha...0.3.5-alpha

## 0.3.4-alpha - 2025-05-15

### Other

- Revert "EventBus : Enable All access of listener, include private" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1955046](https://github.com/CleanroomMC/Cleanroom/commit/19550468fa022d28e72f9019cc3212a2244e2113)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.3-alpha...0.3.4-alpha

## 0.3.3-alpha - 2025-05-15

### Bug Fix

- Fix javadoc for two generic events *[PR by [@ZZZank](https://github.com/ZZZank) in [#329](https://github.com/CleanroomMC/Cleanroom/pull/329)]*
- Fix listener scanning shortcut skipping classloading for method param types *[PR by [@ZZZank](https://github.com/ZZZank) in [#350](https://github.com/CleanroomMC/Cleanroom/pull/350)]*

### Other

- EventBus : Enable All access of listener, include private *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#354](https://github.com/CleanroomMC/Cleanroom/pull/354)]*
- Reduce allocation in `ListenerList` *[PR by [@ZZZank](https://github.com/ZZZank) in [#344](https://github.com/CleanroomMC/Cleanroom/pull/344)]*
- Fix event listener registration for method with non-void return type *[PR by [@ZZZank](https://github.com/ZZZank) in [#353](https://github.com/CleanroomMC/Cleanroom/pull/353)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [01a5267](https://github.com/CleanroomMC/Cleanroom/commit/01a5267736b58deb93a060e0e1f11a358be91ad9)]*
- Update dependencies *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e69b5ca](https://github.com/CleanroomMC/Cleanroom/commit/e69b5cab01d668d7c0974fcfb8ad2f05bcaa6454)]*
- Fix codec dep *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2456ef4](https://github.com/CleanroomMC/Cleanroom/commit/2456ef468a226f496823a114f92c94b934228e41)]*
- Test update netty *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0fafa59](https://github.com/CleanroomMC/Cleanroom/commit/0fafa592ca803c8df27c5c7b482bd3cb304ad3fc)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d628639](https://github.com/CleanroomMC/Cleanroom/commit/d628639388206988d23ed7a6f9bc0547c562aa28)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e5f4e16](https://github.com/CleanroomMC/Cleanroom/commit/e5f4e167b522719d8ff0ec9ee57980a9394ad543)]*
- Cleanroom the server terminalconsole *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#339](https://github.com/CleanroomMC/Cleanroom/pull/339)]*
- Remove the "loader" in client title *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#340](https://github.com/CleanroomMC/Cleanroom/pull/340)]*
- More server console fixing *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d0689ee](https://github.com/CleanroomMC/Cleanroom/commit/d0689ee39a88a3762370561e13330eb7b6e061ba)]*
- More attempts on fixing server console *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1424918](https://github.com/CleanroomMC/Cleanroom/commit/1424918f237fcfb1c64680f9f042b8a4ab2bcf46)]*
- Fix #338 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b935586](https://github.com/CleanroomMC/Cleanroom/commit/b935586656f783957507150191def116700d2488), issue by [@2190303755](https://github.com/2190303755)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.2-alpha...0.3.3-alpha

## 0.3.2-alpha - 2025-04-30

### Other

- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0d6ab1a](https://github.com/CleanroomMC/Cleanroom/commit/0d6ab1a14d4dae296b8f6beb3b2b066d2828ad13)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7192c50](https://github.com/CleanroomMC/Cleanroom/commit/7192c507edaba25544f4e2ecb6fd4f8f4a68f1d2)]*
- Update readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c8f0648](https://github.com/CleanroomMC/Cleanroom/commit/c8f0648fd8493b45300835cf112a50e419c0c95b)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8830906](https://github.com/CleanroomMC/Cleanroom/commit/88309069e4f19515bcd660246b9d06353a5e0386)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.1-alpha...0.3.2-alpha

## 0.3.1-alpha - 2025-04-27

### Bug Fix

- Removed Realms Notification button in option gui *[PR by [@Gebardensprache](https://github.com/Gebardensprache) in [#295](https://github.com/CleanroomMC/Cleanroom/pull/295)]*

### Other

- Update mcp config *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [dcd0ef3](https://github.com/CleanroomMC/Cleanroom/commit/dcd0ef3ff584acd93d2ee34151824dd7d2172b54)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [bed18b0](https://github.com/CleanroomMC/Cleanroom/commit/bed18b0b69a2a402e8d54e1e4b620668b851d52e)]*
- Fix dep name *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5b0dd59](https://github.com/CleanroomMC/Cleanroom/commit/5b0dd5911d4d2bf73e2d6d0d1affc04201c73321)]*
- Roll back netty *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b9dbb65](https://github.com/CleanroomMC/Cleanroom/commit/b9dbb65bb5a04f1fe6d3c1bb29abc7940e16e02c)]*
- Update Lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [bc03419](https://github.com/CleanroomMC/Cleanroom/commit/bc03419d9775eacfc84e1314fd819a5bd52a33d0)]*
- Update jvm driver again *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a87794c](https://github.com/CleanroomMC/Cleanroom/commit/a87794cbc56e4c8f9a4c61653a2f17dcb3626b32)]*
- Update jvm driver *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [da38115](https://github.com/CleanroomMC/Cleanroom/commit/da38115932354c113444481db3c60e266e15d48c)]*
- Clean build script deprecated methods, fix build task not building universal *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2b5de3f](https://github.com/CleanroomMC/Cleanroom/commit/2b5de3fb3510dc176df34574d1abbb0f9ba7cc50)]*
- More test fix *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b059987](https://github.com/CleanroomMC/Cleanroom/commit/b0599876f7c103cab620b8e4e47d4a6abebe4de6)]*
- Update foundation again *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c290a5a](https://github.com/CleanroomMC/Cleanroom/commit/c290a5ace4f5784fa92f855fd82dcd52e6646ed5)]*
- Fix test *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2af53e0](https://github.com/CleanroomMC/Cleanroom/commit/2af53e09d06a0cded7f29cf1348ea7ed8d4a187f)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b3db460](https://github.com/CleanroomMC/Cleanroom/commit/b3db46045b9833b36f459e740961c642bcc24c75)]*
- Change to installerJar so it at least build *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9be4bb8](https://github.com/CleanroomMC/Cleanroom/commit/9be4bb8f01d36a705bd2d323b0759a50e27c824f)]*
- Update mixin extra *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b2e6748](https://github.com/CleanroomMC/Cleanroom/commit/b2e6748de84474a0d8d3b736aa2b6761e8311ce4)]*
- Add workflow trigger restriction, change to build *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [639c1f4](https://github.com/CleanroomMC/Cleanroom/commit/639c1f4eeee7a2e70475ebed3209c7bfc36c30d9)]*
- Fix misinfo in readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d86d89f](https://github.com/CleanroomMC/Cleanroom/commit/d86d89f2d822bc3292314df610f0354ac0f6b1a8)]*
- Add Cleanroom to F3 top-left *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e37cc08](https://github.com/CleanroomMC/Cleanroom/commit/e37cc085016c17b0f67450a12b15b342b81042ba)]*
- Test patching duplicated event *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [91a5b77](https://github.com/CleanroomMC/Cleanroom/commit/91a5b77336e81a25e1bd19b482663d65b2504bbd)]*
- Optimize event handler class scanning *[PR by [@ZZZank](https://github.com/ZZZank) in [#330](https://github.com/CleanroomMC/Cleanroom/pull/330)]*
- Fix crash on netty 4.2.0 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6062c1f](https://github.com/CleanroomMC/Cleanroom/commit/6062c1f8f5225155f50dc165cae595bbf3f063d4)]*
- Cleanup NetworkSystem hunk *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [79f1c42](https://github.com/CleanroomMC/Cleanroom/commit/79f1c426a4ad718d7ee2138f188e2a0ddb321bac)]*
- Use codec-base instead of codec *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3ab0ea4](https://github.com/CleanroomMC/Cleanroom/commit/3ab0ea43efd3adeabf6b06aca8fe812131ee7c79)]*
- MethodHandle based `IEventListener` creation *[PR by [@ZZZank](https://github.com/ZZZank) in [#328](https://github.com/CleanroomMC/Cleanroom/pull/328)]*
- Update netty *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fbfb208](https://github.com/CleanroomMC/Cleanroom/commit/fbfb2089f3a233b985a9fefc511f0e368c4d6399)]*
- Update ASM *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ffb8e01](https://github.com/CleanroomMC/Cleanroom/commit/ffb8e01901c00ea8f18680a31ea0d8dacfc6698e)]*
- Update readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8a823b1](https://github.com/CleanroomMC/Cleanroom/commit/8a823b1c1565bc797bf39b171fc6b8a76d61945a)]*
- Default IFMLLoadingPlugin *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#322](https://github.com/CleanroomMC/Cleanroom/pull/322)]*
- Encourage modders to use the modern entity registration method *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#250](https://github.com/CleanroomMC/Cleanroom/pull/250)]*
- Improved Mod List Version Check Edtion *[PR by [@Desoroxxx](https://github.com/Desoroxxx) in [#321](https://github.com/CleanroomMC/Cleanroom/pull/321)]*
- Improve the mod list again *[PR by [@Desoroxxx](https://github.com/Desoroxxx) in [#319](https://github.com/CleanroomMC/Cleanroom/pull/319)]*
- Removed some unused mixin logic *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5869cd5](https://github.com/CleanroomMC/Cleanroom/commit/5869cd5aeddd80d1db9127d6fde78215c5bec2ad)]*
- Add config switch for ime blocker *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [65dbdd4](https://github.com/CleanroomMC/Cleanroom/commit/65dbdd43bd489ba7ac0afb44f4c3be33fa97b306)]*
- Only hack the windows timer on windows & Low priority *[PR by [@Desoroxxx](https://github.com/Desoroxxx) in [#318](https://github.com/CleanroomMC/Cleanroom/pull/318)]*
- Improved mod-list *[PR by [@Desoroxxx](https://github.com/Desoroxxx) in [#313](https://github.com/CleanroomMC/Cleanroom/pull/313)]*
- Update oshi & jna *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0f3dd36](https://github.com/CleanroomMC/Cleanroom/commit/0f3dd368320bccb7e9c669fda8cd13d01fb32823)]*
- Update icu4j & mixinextra *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [43bcf5a](https://github.com/CleanroomMC/Cleanroom/commit/43bcf5a46fdb2aa6598fb1eaf8c04f42ec1cb23d)]*
- Test maven caching *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6166685](https://github.com/CleanroomMC/Cleanroom/commit/6166685b0a62399fa837781b99e20e02c2009f0a)]*
- Revert "What if we..." *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [438bb1f](https://github.com/CleanroomMC/Cleanroom/commit/438bb1f95962d97f6693268c43ebf512574f4502)]*
- What if we... *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2fbec62](https://github.com/CleanroomMC/Cleanroom/commit/2fbec628fc6a7640234a8741396f29807f4b2aac)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [09332db](https://github.com/CleanroomMC/Cleanroom/commit/09332dbc0bdf033ea8ce26befbee5ece3d06f551)]*
- Better debug logging in Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [806630e](https://github.com/CleanroomMC/Cleanroom/commit/806630e1f5b418ac20b0460c06aaf763ff5450ff)]*
- Make class loading debug more obvious *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [87202c4](https://github.com/CleanroomMC/Cleanroom/commit/87202c4f9ecf90de3f56a96c14ec6fe7a5b178bc)]*
- Move some debug log to info *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7e32b68](https://github.com/CleanroomMC/Cleanroom/commit/7e32b6852c91d7df6e940dc67bee28553ed26edc)]*
- Update Foundation again *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [997e789](https://github.com/CleanroomMC/Cleanroom/commit/997e789e03786ff4fe8578a1a4003d7b06a1a91c)]*
- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [821262f](https://github.com/CleanroomMC/Cleanroom/commit/821262fb127cb013451dbc79331d98d5c30a297d)]*
- Com.cleanroommc.relauncher.CleanroomEntrypoint *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#306](https://github.com/CleanroomMC/Cleanroom/pull/306)]*
- Remove duplicated entry in gitignore *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3efbba8](https://github.com/CleanroomMC/Cleanroom/commit/3efbba81c264eb17d6ee0ced3b09bc3c2819c157)]*
- Update randome gradle plugin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a966d1c](https://github.com/CleanroomMC/Cleanroom/commit/a966d1c97fbfc87618e294662e834c8e321f20a2)]*
- Update dependencies *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [abe9224](https://github.com/CleanroomMC/Cleanroom/commit/abe9224d1e32c56ea961be48dfd6d78cfc6d7aba)]*

### First-time Contributors

- **[@Desoroxxx](https://github.com/Desoroxxx) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.3.0-alpha...0.3.1-alpha

## 0.3.0-alpha - 2025-02-25

### Feature

- Project icon in intellij *[PR by [@Gebardensprache](https://github.com/Gebardensprache) in [#286](https://github.com/CleanroomMC/Cleanroom/pull/286)]*

### Other

- Pirate Speak Lang *[PR by [@strubium](https://github.com/strubium) in [#220](https://github.com/CleanroomMC/Cleanroom/pull/220)]*
- Update Java 24 to RC and add Java 25 EA in the issue template *[commit by [@Rongmario](https://github.com/Rongmario) in [f385fbd](https://github.com/CleanroomMC/Cleanroom/commit/f385fbda8501f4d07f258fde47bb168c763cecf4)]*
- Update jline *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b7695cb](https://github.com/CleanroomMC/Cleanroom/commit/b7695cb3266ec1fec8470052d474552619870c64)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7c1d4d6](https://github.com/CleanroomMC/Cleanroom/commit/7c1d4d6d39ae7299c3a01f38ed26b3595e3e431e)]*
- Bump lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [50464f1](https://github.com/CleanroomMC/Cleanroom/commit/50464f1a9b4f6972ef3c90aac2061d44c0a7bd36)]*
- Update Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [19c79e9](https://github.com/CleanroomMC/Cleanroom/commit/19c79e90a44622eb47f116fdb24829b270fd695f)]*
- Workaround for null crash in Context *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e5e08c0](https://github.com/CleanroomMC/Cleanroom/commit/e5e08c06cd828d17cb344a501a6906993a651397)]*
- Avoid oshi crash on Windows *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [485c1c5](https://github.com/CleanroomMC/Cleanroom/commit/485c1c580fed6cd1ff31bcb0127a677725597997)]*
- Add Force X11 option *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b2cf41f](https://github.com/CleanroomMC/Cleanroom/commit/b2cf41f0375647ad76b38dca02fd9432b015b0a1)]*
- Update Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c124b42](https://github.com/CleanroomMC/Cleanroom/commit/c124b42c045fa7e961a89aa5fe6c676ea19008fc)]*
- Bump default mixinbooter version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5361c82](https://github.com/CleanroomMC/Cleanroom/commit/5361c82313685a190c370ab9a89c1e713f5706b7)]*
- Update MixinBooter *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#275](https://github.com/CleanroomMC/Cleanroom/pull/275)]*
- Update gson *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [556959d](https://github.com/CleanroomMC/Cleanroom/commit/556959d7fbdd9aa6dda200f16ba4b31381e305d9)]*
- Update Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [91a6b65](https://github.com/CleanroomMC/Cleanroom/commit/91a6b6575a89c1c1f274d712bd8ab6f1bf52d6e9)]*
- Update lwjglxx 3 times a day to avoid license *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [49857d4](https://github.com/CleanroomMC/Cleanroom/commit/49857d48cc84f7ac46ceb1d9be70cec54a98bd66)]*
- Revert "Update lwjglxx" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7abb6b9](https://github.com/CleanroomMC/Cleanroom/commit/7abb6b990605c7a5e3c30dec0e28b9edfe7fcc3b)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [85d24e5](https://github.com/CleanroomMC/Cleanroom/commit/85d24e5ba40425e3af69b6de99f24b5bf1ed88ac)]*
- Revert "Make OpenGL version configurable" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b0be8cc](https://github.com/CleanroomMC/Cleanroom/commit/b0be8cc0e23f5a22ed338f43e85e2d388ddfe2b4)]*
- Make OpenGL version configurable *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [db3df52](https://github.com/CleanroomMC/Cleanroom/commit/db3df52c59a2fb26baaf9381f5ad5ee6ef83cc6d)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b33bded](https://github.com/CleanroomMC/Cleanroom/commit/b33bdeddf69b8ea1e3082d81297b6c166310d568)]*
- Fix lwjglxx crash on Windows + nvidia *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a5da021](https://github.com/CleanroomMC/Cleanroom/commit/a5da021c83fe082e35671725e4dc677649ff67d8)]*
- Update lwjglxx to handle nvidia + wayland *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [241ac8f](https://github.com/CleanroomMC/Cleanroom/commit/241ac8f6b33bdcb2b15beb7c96fdf565163c65fe)]*
- Rollback lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [abcc3ff](https://github.com/CleanroomMC/Cleanroom/commit/abcc3ff3fe1712562a6b356e874eb92802e1880f)]*
- Add jinput *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fb5445b](https://github.com/CleanroomMC/Cleanroom/commit/fb5445baab65708ed77f4807fc705fb13cd8706b)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [cc7cb09](https://github.com/CleanroomMC/Cleanroom/commit/cc7cb096a16fbe2057a70c8032a71accccfdca0e)]*
- Add desc about kotlin provider *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fa5a17b](https://github.com/CleanroomMC/Cleanroom/commit/fa5a17bf6c3620f4cb30932efb916349d8a96715)]*
- Rollback foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3400524](https://github.com/CleanroomMC/Cleanroom/commit/340052440e98d099edc5a63ef15704197937d015)]*
- Update readme and issue template *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ba1cc0b](https://github.com/CleanroomMC/Cleanroom/commit/ba1cc0b1a479fde280c30a65e06396b8ff8e2899)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [53024a8](https://github.com/CleanroomMC/Cleanroom/commit/53024a8ccffea31222f77dcb1886fba52da6fea4)]*
- Remove cocoa ime handler *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6c5a1fc](https://github.com/CleanroomMC/Cleanroom/commit/6c5a1fcf5d66feee88156d1468dba6d27b0c3560)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2458b9a](https://github.com/CleanroomMC/Cleanroom/commit/2458b9ae0c103a443d54f78173576c62de7556d5)]*
- Update dependency *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d7a0625](https://github.com/CleanroomMC/Cleanroom/commit/d7a062515f3706ad00d717b637f479923d44f7f8)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5317351](https://github.com/CleanroomMC/Cleanroom/commit/5317351a98cf1b93f1a9c10021e4d7dc122641a1)]*
- Porting of #158 solves the problem that the problem reappears when main is merged into foundation. *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#272](https://github.com/CleanroomMC/Cleanroom/pull/272)]*
- Try to fix #270 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6aba966](https://github.com/CleanroomMC/Cleanroom/commit/6aba966f1f95126ed26cfd2901399ff741525ce1), issue by [@create-xiaoyu](https://github.com/create-xiaoyu)]*
- Update dependency *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [87d1f9b](https://github.com/CleanroomMC/Cleanroom/commit/87d1f9b7b8f67d5d1756148a49c2b4cf37f109ae)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [191e32a](https://github.com/CleanroomMC/Cleanroom/commit/191e32a831bf90463e93f996362f057a2bab5b7f)]*
- Update mixin extra *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5885b66](https://github.com/CleanroomMC/Cleanroom/commit/5885b669f82c61e1254a1dcba201af2cc44636df)]*
- Use dummy handler on mac *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2d4e2b2](https://github.com/CleanroomMC/Cleanroom/commit/2d4e2b23008b43da8c443ee103cf4a78c19acbec)]*
- Update LWJGL to 3.3.6 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4fe26de](https://github.com/CleanroomMC/Cleanroom/commit/4fe26de0720787895fb0f7bef6fffec3ded17d0e)]*
- Try to avoid no GL context in crash *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [242e418](https://github.com/CleanroomMC/Cleanroom/commit/242e41881f9897e12230760e8901010d9489584f)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [87d9b85](https://github.com/CleanroomMC/Cleanroom/commit/87d9b854dcd211ae22b50a39f2bd21dbe90a3b1d)]*
- Recrash_cleanroomversion *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#273](https://github.com/CleanroomMC/Cleanroom/pull/273)]*
- Remove duplicate language entries *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#271](https://github.com/CleanroomMC/Cleanroom/pull/271)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2a5629a](https://github.com/CleanroomMC/Cleanroom/commit/2a5629af78b6dcd093d7babd7ad2ca2ddc80cf6a)]*
- Revert "Try glfw solution on cocoa" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7284884](https://github.com/CleanroomMC/Cleanroom/commit/728488412b705d77f9b17da7f40d84c8375f134a)]*
- Try glfw solution on cocoa *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [cd52a92](https://github.com/CleanroomMC/Cleanroom/commit/cd52a92516ced7d3d3da042e97904ae371182a15)]*
- Real update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [83db4af](https://github.com/CleanroomMC/Cleanroom/commit/83db4af65b45411a54382e790a4ac607275b3020)]*
- Update lwjglxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3d3e6bd](https://github.com/CleanroomMC/Cleanroom/commit/3d3e6bd3de12a21c0ebcec197ed908026eef647a)]*
- Rollback to glfw_async *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [da08e05](https://github.com/CleanroomMC/Cleanroom/commit/da08e05f3973fce9d36c23076b520c99b092ae3a)]*
- Update lwjgl init to fix MacOS *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [30b211c](https://github.com/CleanroomMC/Cleanroom/commit/30b211c300f43b58ea081db242a42ce9fe0332ed)]*
- Use wayland when possible *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [460604c](https://github.com/CleanroomMC/Cleanroom/commit/460604c7d5f381d6c38d7b1f1d5d5da231dcafbe)]*
- Fix wrong handle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e08f5e7](https://github.com/CleanroomMC/Cleanroom/commit/e08f5e767c793a065ad1ddf1bc1eeee0dc45e471)]*
- Haven't test ime pr on x11 yet *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b9863b3](https://github.com/CleanroomMC/Cleanroom/commit/b9863b3be7a5579902069bd7b7dbf78802e3e6a1)]*
- Add dummy of X11 and wayland *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [df3a8b3](https://github.com/CleanroomMC/Cleanroom/commit/df3a8b3e189348701fe9d655b6c691b7108901c3)]*
- Improve ime whitelist *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2214135](https://github.com/CleanroomMC/Cleanroom/commit/2214135078c0f3c025105196f19b13674447d0a7)]*
- Copied cocoa ime blocker *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [effa043](https://github.com/CleanroomMC/Cleanroom/commit/effa043061528e90352cd8ac138e5d59f39dbf84)]*
- Remove unused zh_TW.lang which hates Windows developers *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [cdf5ca6](https://github.com/CleanroomMC/Cleanroom/commit/cdf5ca632a00cf77c1e050749fc4544d4e783628)]*
- New IME config *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a72662a](https://github.com/CleanroomMC/Cleanroom/commit/a72662a404ff1fa878becbf5aa264e0ae268f532)]*
- Update readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e15695c](https://github.com/CleanroomMC/Cleanroom/commit/e15695c9e7b196e9bbf8879e505ece48f29ed033)]*
- Add problem report from gradle 8.11 to git ignore *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ab42464](https://github.com/CleanroomMC/Cleanroom/commit/ab424648729100d39c834683c685380160b8ab24)]*
- Add Java 24 EA to issue template *[commit by [@Rongmario](https://github.com/Rongmario) in [f81dd1e](https://github.com/CleanroomMC/Cleanroom/commit/f81dd1ee501a64a91a60a7883bddc0492971982a)]*
- Test ime blocker *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e8a5384](https://github.com/CleanroomMC/Cleanroom/commit/e8a5384ec9e4f383b1cae47881cc456a6e4ed5e5)]*
- Special patch for lwjgl AL *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ebc783a](https://github.com/CleanroomMC/Cleanroom/commit/ebc783a519b238158c2bcb14a424a6727091e77a)]*
- SideTransformer logging improve *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [604ce23](https://github.com/CleanroomMC/Cleanroom/commit/604ce2395ed99b664cb0a9de979ceb870e0a1e12)]*
- Patch auto-fix *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [53dc8c0](https://github.com/CleanroomMC/Cleanroom/commit/53dc8c0d13d6820673807c0ac4c06fb65f61ed10)]*
- Update mcp config *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4f07c58](https://github.com/CleanroomMC/Cleanroom/commit/4f07c589a1bd4d731a3d8fd1a2af404430f99c7c)]*
- Update VineFlower *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a51e182](https://github.com/CleanroomMC/Cleanroom/commit/a51e18258440b50821d9ffd330b0f0254df472a9)]*
- Change wayland desc *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [da0e33d](https://github.com/CleanroomMC/Cleanroom/commit/da0e33d5887699f97c9238087f9853ac6cbfd3cc)]*
- Switch to official LWJGL 3.3.5 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ca8bf8e](https://github.com/CleanroomMC/Cleanroom/commit/ca8bf8e8171b0ae55144a8857e4e3db519656545)]*
- Revert "I hate xml" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f9b5813](https://github.com/CleanroomMC/Cleanroom/commit/f9b58134d1ac87b2073fa6167fe876b47ef433c6)]*
- I hate xml *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3dd1764](https://github.com/CleanroomMC/Cleanroom/commit/3dd1764a61741d93c1ce156e65a185f2dfb3d5e9)]*
- Idk why but it seems jaxb need more deps *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7411c8a](https://github.com/CleanroomMC/Cleanroom/commit/7411c8a6322f89f38d2466d54b00adb451b4f464)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [281d852](https://github.com/CleanroomMC/Cleanroom/commit/281d852dad51b043be5cdb7d17aec3e8a9d10d89)]*
- Cleanup patch hunks *[PR by [@HaHaWTH](https://github.com/HaHaWTH) in [#253](https://github.com/CleanroomMC/Cleanroom/pull/253)]*
- Fix wrong annotation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7c7e759](https://github.com/CleanroomMC/Cleanroom/commit/7c7e759b5365239955f07319959d516ccef45833)]*
- Better mixin error handling *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1b3517e](https://github.com/CleanroomMC/Cleanroom/commit/1b3517e83b8079e6b26b62bdd9ca42ecf9937e63)]*
- Revert "Avoid + on publish versioning" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f46676f](https://github.com/CleanroomMC/Cleanroom/commit/f46676f4d71ce12c0c374485286b9cc1b8fe25ab)]*
- Avoid + on publish versioning *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1d25c76](https://github.com/CleanroomMC/Cleanroom/commit/1d25c76885eac4ee5eed5678aeca337e0d73bf15)]*
- More info about coremod blacklist *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [283d396](https://github.com/CleanroomMC/Cleanroom/commit/283d396c355d68595659b713dbdc1d4e428e8c1f)]*
- Add Forgelin to coremod blacklist *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [15529fd](https://github.com/CleanroomMC/Cleanroom/commit/15529fdc26d1ef8b0169f488e1f9a84e2f626471)]*
- Update default mixinbooter version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [841822f](https://github.com/CleanroomMC/Cleanroom/commit/841822f37d3999b4b278cfe53944c6cac520a6b5)]*
- Ignore Gradle 8.11 problem report file *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fa7fd6d](https://github.com/CleanroomMC/Cleanroom/commit/fa7fd6d33c020f3a568c498677624d863e9feed0)]*
- Add more entried to coremod blacklist *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [788a91a](https://github.com/CleanroomMC/Cleanroom/commit/788a91ac52a8f390bc33cc8913fbfcec9a551c8a)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2dea6c8](https://github.com/CleanroomMC/Cleanroom/commit/2dea6c8c18640efa26ee8e465420122f35f57e2a)]*

### First-time Contributors

- **[@Gebardensprache](https://github.com/Gebardensprache) made their first contribution!**
- **[@HaHaWTH](https://github.com/HaHaWTH) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.2.4-alpha...0.3.0-alpha

## 0.2.4-alpha - 2024-11-18

### Other

- Move to new API *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [39e9c4d](https://github.com/CleanroomMC/Cleanroom/commit/39e9c4d6607a628aee88c5f159c18a1efe3add41)]*
- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3594a2d](https://github.com/CleanroomMC/Cleanroom/commit/3594a2de57ff638acf98306fd9e35c007eb893bc)]*
- Add manually publish action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [27f74c2](https://github.com/CleanroomMC/Cleanroom/commit/27f74c20f1acd18aa687ff0a0a1fdae86eaa9de7)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9307990](https://github.com/CleanroomMC/Cleanroom/commit/93079903812b4e41ffffa0151825a83cd14a378e)]*
- Cleanup MixinFixer. Will move it to Fugue *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d7d7360](https://github.com/CleanroomMC/Cleanroom/commit/d7d7360ff066f93751605bbf4aac402218694a8b)]*
- MORE debug logging *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ce01e07](https://github.com/CleanroomMC/Cleanroom/commit/ce01e07c6a59e1235245171c201745289a13cb45)]*
- Test late mixin loader logging *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c0c668a](https://github.com/CleanroomMC/Cleanroom/commit/c0c668a938e92a32882a625b59bff49491f5e66f)]*
- Make built-in mixinbooter version configable *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3f4950f](https://github.com/CleanroomMC/Cleanroom/commit/3f4950f0184d43cf8abd4353f7243ed502d69976)]*
- Update dependencies *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e5a7c11](https://github.com/CleanroomMC/Cleanroom/commit/e5a7c117785c97b39d5e4fdbc22b96f538f1503d)]*
- Java Doc for some Client events and helpful api *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#242](https://github.com/CleanroomMC/Cleanroom/pull/242)]*
- Update bug report template to include Java 23 *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#231](https://github.com/CleanroomMC/Cleanroom/pull/231)]*
- Can not skip code in AccessTransformer *[PR by [@friendlyhj](https://github.com/friendlyhj) in [#241](https://github.com/CleanroomMC/Cleanroom/pull/241)]*
- Skip more asm reader operations *[PR by [@friendlyhj](https://github.com/friendlyhj) in [#240](https://github.com/CleanroomMC/Cleanroom/pull/240)]*
- Skip code in scan annotations *[PR by [@friendlyhj](https://github.com/friendlyhj) in [#239](https://github.com/CleanroomMC/Cleanroom/pull/239)]*
- Update Lwjgl3ify *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#233](https://github.com/CleanroomMC/Cleanroom/pull/233)]*
- Update Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d4b84d3](https://github.com/CleanroomMC/Cleanroom/commit/d4b84d3ddc4d04de1d589019b82ee3d1ac3da56e)]*
- Update MixinExtra like upstream mixinbooter *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a28f191](https://github.com/CleanroomMC/Cleanroom/commit/a28f191ff6c18bb1f24ca0c7e3d1646c8f60f0ba)]*
- Test update queue size of mouse event *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7a41514](https://github.com/CleanroomMC/Cleanroom/commit/7a415144d6ffca4c63bf1e5503e8cc2f126d0fdd)]*
- Revert "Using NeoForge Installer instead of Forge, update correct icon" *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [6629255](https://github.com/CleanroomMC/Cleanroom/commit/6629255f380f3b4fd2f435337958fcbc7a11f7ff)]*
- Using NeoForge Installer instead of Forge, update correct icon *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#121](https://github.com/CleanroomMC/Cleanroom/pull/121)]*
- Fix SoundManager handling when no sound devices exist *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#226](https://github.com/CleanroomMC/Cleanroom/pull/226)]*
- Improve the error when you try to sync the same registry instance *[PR by [@strubium](https://github.com/strubium) in [#223](https://github.com/CleanroomMC/Cleanroom/pull/223)]*
- Chenge non-mod file warning *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b570c8e](https://github.com/CleanroomMC/Cleanroom/commit/b570c8ef95b4a74ca3ca3be3a97fefe6541399c1)]*
- Fix wrong asm version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [71f03bf](https://github.com/CleanroomMC/Cleanroom/commit/71f03bf961046b202f34bed9c2556a1262aabb14)]*
- Update dependency of asm and netty *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2bb4b74](https://github.com/CleanroomMC/Cleanroom/commit/2bb4b74c30936dcacc5053f5661015577fc74192)]*
- Migrate exclusion to foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [898bb0f](https://github.com/CleanroomMC/Cleanroom/commit/898bb0f998e38ad4954ef0742bee0202d44c5445)]*
- :getParent *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#216](https://github.com/CleanroomMC/Cleanroom/pull/216)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [cae4448](https://github.com/CleanroomMC/Cleanroom/commit/cae444861dc612638a5929fed31b89498fd59968)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d4904ce](https://github.com/CleanroomMC/Cleanroom/commit/d4904ce0345a363de7feaf1313286c6bacd118ec)]*
- Fully moved to new log4j plugin registeration system *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [065d0d6](https://github.com/CleanroomMC/Cleanroom/commit/065d0d6bce5cc585575e0d4a7b1d553d0e1090db)]*
- Use real dat *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a1785a9](https://github.com/CleanroomMC/Cleanroom/commit/a1785a9b5e053bed234cdad81ce202ac9606ee7c)]*
- Comment log4j ap *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [120e576](https://github.com/CleanroomMC/Cleanroom/commit/120e57613601c74c1f7719aaa1c922db87c67902)]*
- Add Log4j2Plugins.dat manually *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9d2e4d3](https://github.com/CleanroomMC/Cleanroom/commit/9d2e4d347a738c2f5ba8da01b061758a26b18816)]*
- Roll back terminal change *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [aba45c8](https://github.com/CleanroomMC/Cleanroom/commit/aba45c8ede4795eca2735b303da071b469d03209)]*
- Add log4j annotation processor *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [560bf33](https://github.com/CleanroomMC/Cleanroom/commit/560bf330025023011634ce8d487276fc2bdf356b)]*
- More config on terminal builder *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8b4d8d3](https://github.com/CleanroomMC/Cleanroom/commit/8b4d8d348026493894d47d17d0588687235fe920)]*
- Massive dependency update *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e0a517b](https://github.com/CleanroomMC/Cleanroom/commit/e0a517b69c905a35365a727852fd3fead38aed41)]*
- Fix Exception throwing in AL creation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3599136](https://github.com/CleanroomMC/Cleanroom/commit/3599136aac6f1a53d7cd492ef9ec523e869dd375)]*
- Fix wrong config file name in ConfigAnyTime *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7532818](https://github.com/CleanroomMC/Cleanroom/commit/7532818592db9f88f3f72dbe18adf87907f00945)]*
- Implement Display.setLocation() *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2dc6daf](https://github.com/CleanroomMC/Cleanroom/commit/2dc6dafa909d44f06e8e2a208da6321f583eddaa)]*
- Fix mouse handling *[PR by [@JL2210](https://github.com/JL2210) in [#198](https://github.com/CleanroomMC/Cleanroom/pull/198)]*
- Workaround to mouse shifting *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3cd03f3](https://github.com/CleanroomMC/Cleanroom/commit/3cd03f3e092fda59fd5122720ff3842673fa8039)]*
- Experimental DECORATED setting *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [042bc3d](https://github.com/CleanroomMC/Cleanroom/commit/042bc3d7c6080d22b66a1d3676b6087c55da136c)]*
- Remove macOS hint *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [054f754](https://github.com/CleanroomMC/Cleanroom/commit/054f754445bd17357c9ff6d645f913c41bea3e91)]*
- Port scale fix from https://github.com/GTNewHorizons/lwjgl3ify/pull/171 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ed6dba0](https://github.com/CleanroomMC/Cleanroom/commit/ed6dba0d2a4f521ae516f4ced7ec6860d0d04d74)]*

### First-time Contributors

- **[@JL2210](https://github.com/JL2210) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.2.3-alpha...0.2.4-alpha

## 0.2.3-alpha - 2024-08-19

### Other

- Javadoc for BiomeDictionary *[PR by [@strubium](https://github.com/strubium) in [#187](https://github.com/CleanroomMC/Cleanroom/pull/187)]*
- Javadoc for DungeonHooks *[PR by [@strubium](https://github.com/strubium) in [#186](https://github.com/CleanroomMC/Cleanroom/pull/186)]*
- Javadoc for ProgressManager *[PR by [@strubium](https://github.com/strubium) in [#185](https://github.com/CleanroomMC/Cleanroom/pull/185)]*
- Improve the "Duped mods" error message *[PR by [@strubium](https://github.com/strubium) in [#181](https://github.com/CleanroomMC/Cleanroom/pull/181)]*
- Add zh_CN and zh_TW *[PR by [@ChAoSUnItY](https://github.com/ChAoSUnItY) in [#170](https://github.com/CleanroomMC/Cleanroom/pull/170)]*
- Update MixinExtra *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [12fce85](https://github.com/CleanroomMC/Cleanroom/commit/12fce8578a8fdd0f7c346a253f3fe5a5eb76030a)]*
- Add crl version to crash report details *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f42af37](https://github.com/CleanroomMC/Cleanroom/commit/f42af37ca0dbae50c6ecbf151eb622b8e6aa737e)]*
- Update Foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9ad524c](https://github.com/CleanroomMC/Cleanroom/commit/9ad524cc64d86196a19426d24d46a8cbf8ed2a69)]*
- Should fix FontRenderer crash in edge case *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b5ebc78](https://github.com/CleanroomMC/Cleanroom/commit/b5ebc7817d253e9d542fefbf48ea16a4f80781a4)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d95b89f](https://github.com/CleanroomMC/Cleanroom/commit/d95b89fbba5af537ab833ff72de4a78b9e39b6af)]*
- Add config to ignore coremods by loading plugin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9e5e47e](https://github.com/CleanroomMC/Cleanroom/commit/9e5e47e9a64aaa354b38d2eac0232023df16af9c)]*
- Check if supported before set hint *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [97e6e5f](https://github.com/CleanroomMC/Cleanroom/commit/97e6e5f4d7a31e9da58193ef4359648ec601ff76)]*
- Add wayland config *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1b865af](https://github.com/CleanroomMC/Cleanroom/commit/1b865af3132ae4309ad57dd9e95306c474a0cc9d)]*
- Update maven url *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0ac9e4b](https://github.com/CleanroomMC/Cleanroom/commit/0ac9e4b910f0163740ef8162a3c75b1e5d6aa4bf)]*
- Update mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [870e914](https://github.com/CleanroomMC/Cleanroom/commit/870e914c8526e740564ebdf278f3341ce73ca57f)]*
- Print Cleanroom version on initialization *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5af8616](https://github.com/CleanroomMC/Cleanroom/commit/5af8616ecdbc83a68bcbb824dba7a738d7d9d2f9)]*
- Make CAT version configurable *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6a6c760](https://github.com/CleanroomMC/Cleanroom/commit/6a6c76041f29ba64dc7399c2075f5204ac0dd2c9)]*
- Test update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [98e5b38](https://github.com/CleanroomMC/Cleanroom/commit/98e5b38377e23538ea01328a6f0d800bbf6f8dae)]*
- Extra version info when launching *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [bbe4de3](https://github.com/CleanroomMC/Cleanroom/commit/bbe4de3d4723116e5c0d5e06f3ea5f28670998eb)]*
- Fix fabric logic in Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0f4cba5](https://github.com/CleanroomMC/Cleanroom/commit/0f4cba53ad9da9b2fe4ea7fdfa6912fbd0991ef9)]*
- Yet again *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d281129](https://github.com/CleanroomMC/Cleanroom/commit/d2811296900e1cdbd9b4066eb857f5d063bd3636)]*
- And again *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [04f50b6](https://github.com/CleanroomMC/Cleanroom/commit/04f50b677a5fd43611b969424a9d94644f9c3423)]*
- Update Mixin again *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [468aae5](https://github.com/CleanroomMC/Cleanroom/commit/468aae58ecc5253679f47a9e66b79f75016ba8c8)]*
- Update Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a803063](https://github.com/CleanroomMC/Cleanroom/commit/a803063b471cf20747cc53706634ea1bc241bfd9)]*
- Sync Mixin to upstream Fabric 0.8.7 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1cfd082](https://github.com/CleanroomMC/Cleanroom/commit/1cfd0825ac6f134ead80f74681e31977ef7b3711)]*
- Mimic Mixin behavior on ForceLoadAsMod *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [93765d5](https://github.com/CleanroomMC/Cleanroom/commit/93765d5113bf79fe01d7341efeec081f952419ad)]*
- Test update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b5cfa24](https://github.com/CleanroomMC/Cleanroom/commit/b5cfa24bd9a1f39b185d565b408e1876d82109e4)]*
- Implement dummy for native methods *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7f7e5f6](https://github.com/CleanroomMC/Cleanroom/commit/7f7e5f6af4984340c22ca5b6b9886d6108e164b1)]*
- Move netty version to props *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9e99d50](https://github.com/CleanroomMC/Cleanroom/commit/9e99d50bd5ef551db0bd48528ae1979e8c98edea)]*
- Special handle non-coremod mixin mods *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4d6a0a3](https://github.com/CleanroomMC/Cleanroom/commit/4d6a0a3969ce095f3e6ba25ac84c219534e6b18c)]*
- Fix special case of mixin mods *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [bf8b243](https://github.com/CleanroomMC/Cleanroom/commit/bf8b2437b204758d142084c82a3a63fa34010016)]*
- Fit high version resize behavior *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b9fa503](https://github.com/CleanroomMC/Cleanroom/commit/b9fa503c959103071c983602a1a6a8a3c00df379)]*
- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9e93b66](https://github.com/CleanroomMC/Cleanroom/commit/9e93b666825139937ce93055ee41e5d8dfa9e8ad)]*
- Align resize behavior to lwjgl2 *[PR by [@vfyjxf](https://github.com/vfyjxf) in [#163](https://github.com/CleanroomMC/Cleanroom/pull/163)]*
- Update bug report template to include Java 22 *[commit by [@Rongmario](https://github.com/Rongmario) in [7ff6718](https://github.com/CleanroomMC/Cleanroom/commit/7ff6718f23879d7d0b9831c1a09f611751200e86)]*
- Fix rare crash case in enum adding *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ccced5f](https://github.com/CleanroomMC/Cleanroom/commit/ccced5f6b1f022db9ddf488beb805f21144a3af8)]*
- More mixin mod handling *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8579a70](https://github.com/CleanroomMC/Cleanroom/commit/8579a70c93e1414e058ff6002c9bfd3358366abf)]*
- Use official fg *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [96845fc](https://github.com/CleanroomMC/Cleanroom/commit/96845fc74822969a6473d647aa4047e1a28ff078)]*
- Make FML no signature error a info *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8291c2d](https://github.com/CleanroomMC/Cleanroom/commit/8291c2dd659f54dd2bd5ad2e715a25c4bcbf29f6)]*
- Update zh_cn.lang *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer), TUsama in [0ecfdf8](https://github.com/CleanroomMC/Cleanroom/commit/0ecfdf824126d4c6d7351e2361ce0245e68c5963)]*
- Lowcase The Cleanroom Resources *[PR by [@Teawhat](https://github.com/Teawhat) in [#158](https://github.com/CleanroomMC/Cleanroom/pull/158)]*

### First-time Contributors

- **[@strubium](https://github.com/strubium) made their first contribution!**
- **[@ChAoSUnItY](https://github.com/ChAoSUnItY) made their first contribution!**
- **[@vfyjxf](https://github.com/vfyjxf) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.2.2-alpha...0.2.3-alpha

## 0.2.2-alpha - 2024-06-05

### Other

- Update gradle *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2446b52](https://github.com/CleanroomMC/Cleanroom/commit/2446b52de76696924c033eb79d7b32383bb76de0)]*
- Massive dependency update *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c58a6f6](https://github.com/CleanroomMC/Cleanroom/commit/c58a6f60730c6b19c54bcfd64c6fc0ba01690a7a)]*
- Add two more langs that we have natives *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4b971c9](https://github.com/CleanroomMC/Cleanroom/commit/4b971c9741eb63fcd42b35b45a98087f75cab9d4)]*
- Add langs from crowdin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [be9db06](https://github.com/CleanroomMC/Cleanroom/commit/be9db06308973d58c60d92f6fe6977039aec33d6)]*
- Fix ttext wrapping *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2022514](https://github.com/CleanroomMC/Cleanroom/commit/20225147b7cbd2060a411912942c7d24097672c0)]*
- Add ko_kr *[PR by [@yor42](https://github.com/yor42) in [#145](https://github.com/CleanroomMC/Cleanroom/pull/145)]*
- Fix source of built-in mods *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5ea3854](https://github.com/CleanroomMC/Cleanroom/commit/5ea385475e7d6692cd7448f3570bd17cdd41510c)]*
- Remove `/120F` *[PR by [@Teawhat](https://github.com/Teawhat) in [#153](https://github.com/CleanroomMC/Cleanroom/pull/153)]*
- Only apply GuiIngameForge options when running on client *[PR by [@ZZZank](https://github.com/ZZZank) in [#155](https://github.com/CleanroomMC/Cleanroom/pull/155)]*
- Move thread context check to crash generation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e84be9c](https://github.com/CleanroomMC/Cleanroom/commit/e84be9cfe4d475e38b2139816a1d151f3a4cf2fb)]*
- Revert "Fix getting glString in no context thread" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [15307f4](https://github.com/CleanroomMC/Cleanroom/commit/15307f4dafb237117709e3952edb2171e3c767bd)]*
- Fix getting glString in no context thread *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1116434](https://github.com/CleanroomMC/Cleanroom/commit/11164347d83ca045177284698a578a2aadf5d4d8)]*
- Actually update mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5b5d6d9](https://github.com/CleanroomMC/Cleanroom/commit/5b5d6d93710121930849a1054af1326d1c5de4f9)]*
- Bump foundation again *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2347c38](https://github.com/CleanroomMC/Cleanroom/commit/2347c384fc762ee0e9ac337c74d89debc96202d5)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3b89dd0](https://github.com/CleanroomMC/Cleanroom/commit/3b89dd085e3c146acbe1858ab64c2cf2e65836c9)]*
- Update mixinextra & foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5a2e303](https://github.com/CleanroomMC/Cleanroom/commit/5a2e3031e6f85b23043913274a83ceb6feade060)]*
- Create GuiGameOver.java.patch *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#111](https://github.com/CleanroomMC/Cleanroom/pull/111)]*
- Config for HUD rendering *[PR by [@ZZZank](https://github.com/ZZZank), arcade_kappa in [#136](https://github.com/CleanroomMC/Cleanroom/pull/136)]*
- Update readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f885641](https://github.com/CleanroomMC/Cleanroom/commit/f8856412a24c1e0588518eac79b5ede8ab393cf4)]*

### First-time Contributors

- **[@Teawhat](https://github.com/Teawhat) made their first contribution!**
- **[@yor42](https://github.com/yor42) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.1.0-infedv...0.2.2-alpha

## 0.1.0-infedv - 2024-05-09

### Other

- Update readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0fcca8d](https://github.com/CleanroomMC/Cleanroom/commit/0fcca8d554715dbe7e017d24432978bade6ee9bf)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.2.1-alpha...0.1.0-infedv

## 0.2.1-alpha - 2024-05-09

### Other

- Better mmc zip naming *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7b8d6b2](https://github.com/CleanroomMC/Cleanroom/commit/7b8d6b219130b801c1d7f8455f3ec55cfb884f1c)]*
- Remove duplicated suffix *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [651c2f4](https://github.com/CleanroomMC/Cleanroom/commit/651c2f4f631187e2d44494e2eb15755b43caa080)]*
- Fix incorrect path *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3cd44ed](https://github.com/CleanroomMC/Cleanroom/commit/3cd44ed3ae19127e9f7e29b602189ba8ccbf4762)]*
- Add auto-changelog *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2098ef5](https://github.com/CleanroomMC/Cleanroom/commit/2098ef50de37217a46ec0c46dacbba2fda992f9d)]*
- Fix working directory *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [95888d0](https://github.com/CleanroomMC/Cleanroom/commit/95888d0966d5338b1c05a7b515dadd1a6f90e24f)]*
- Rename release yml *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [73fd70b](https://github.com/CleanroomMC/Cleanroom/commit/73fd70bbafa7e25e68a6dc0914781bb9e096d07a)]*
- All in tag-based *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [50511ed](https://github.com/CleanroomMC/Cleanroom/commit/50511ed61c6c5471ef37a85eecff624f6593ce9a)]*
- All in tag-based *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7ba8550](https://github.com/CleanroomMC/Cleanroom/commit/7ba8550cc78b297fe9102168f10a0a7c217a4226)]*
- Move suffix to tag *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1bd1566](https://github.com/CleanroomMC/Cleanroom/commit/1bd1566b34f17a6fa01c3323bfd6d3b8c6312ab3)]*
- First ver of release action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [67a0a2c](https://github.com/CleanroomMC/Cleanroom/commit/67a0a2c4b264ae6a765cd47f1a2e26feff33114c)]*
- Revert "Add back server side duplicated cp handling" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [97af02a](https://github.com/CleanroomMC/Cleanroom/commit/97af02a60e129329c866b9d5645c1ccc72032e98)]*
- Add back server side duplicated cp handling *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e503058](https://github.com/CleanroomMC/Cleanroom/commit/e503058987adff4289cee7306dadbe74fc0c7106)]*
- Rollback & rewrite coremod discovery w/ mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e99fa3e](https://github.com/CleanroomMC/Cleanroom/commit/e99fa3ecb7ab5ec7fe671872ec779d8d0c09a2df)]*
- More attempts to fix coremod discover w/ mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [76ef37d](https://github.com/CleanroomMC/Cleanroom/commit/76ef37df619e3c4cae498c6653f994474341d954)]*
- Fix typo *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [39cabeb](https://github.com/CleanroomMC/Cleanroom/commit/39cabeb673acddfd9ac3833f758f7d1bae4fe30f)]*
- Add missing run number *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [bd200d7](https://github.com/CleanroomMC/Cleanroom/commit/bd200d7b2d5679ed221ba9b0ec7e55c5db688503)]*
- Add publish *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [064e0ba](https://github.com/CleanroomMC/Cleanroom/commit/064e0ba7b74eb66ab6930acc33aebcf9e85f550b)]*
- Fix FMLCorePluginContainsFMLMod *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a164160](https://github.com/CleanroomMC/Cleanroom/commit/a1641603083ecd418a4a3854014bba61c4275f64)]*
- Should fix bungeeforge + malisiscore this time *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5485a8e](https://github.com/CleanroomMC/Cleanroom/commit/5485a8e5b95870310f9f4ce3fa19139fde4b2c98)]*
- Revert "Fix not ignoring MixinTweaker mods" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [243ab4b](https://github.com/CleanroomMC/Cleanroom/commit/243ab4bf4d108c9308bb80d8ba071583b68894e1)]*
- Fix not ignoring MixinTweaker mods *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [6405838](https://github.com/CleanroomMC/Cleanroom/commit/6405838f950459b69dc22729afaf383256e5b1ac)]*
- Update branding *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [87e0cdd](https://github.com/CleanroomMC/Cleanroom/commit/87e0cdd55b90ff5e68717529dfb6d837f75f5e9f)]*
- More semver *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c72573f](https://github.com/CleanroomMC/Cleanroom/commit/c72573fb21fe98138cdcf0088d5f554725ec0fe3)]*
- Update cat version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f3bca9e](https://github.com/CleanroomMC/Cleanroom/commit/f3bca9e10f2e7cb6469ef0027f03bf8d3fb58f08)]*
- Bump version to alpha *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [bee8640](https://github.com/CleanroomMC/Cleanroom/commit/bee8640e38aef8cc857c95834929dd622319d831)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.2.0...0.2.1-alpha

## 0.2.0 - 2024-05-04

### Feature

- Early class path adding and mixin config searching *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [59e01cb](https://github.com/CleanroomMC/Cleanroom/commit/59e01cba09c6eb1950d42ec2a9286ca43f2a2ea3)]*
- Better coremod wrapper toString() *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [88f3ec8](https://github.com/CleanroomMC/Cleanroom/commit/88f3ec87679f095c29e47c26bd9bd1084a96f4ed)]*
- Try remove realms *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [260a4a1](https://github.com/CleanroomMC/Cleanroom/commit/260a4a1ebb1f9970761293863c4e9f2fafeca4c5)]*
- Update foundation for removeTransformerExclusion *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4599fe2](https://github.com/CleanroomMC/Cleanroom/commit/4599fe2f609acbae363e32d0d30a56a4b251642e)]*
- Update foundation for fugue *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [532f804](https://github.com/CleanroomMC/Cleanroom/commit/532f804e7fae9b559fa1c86ace1ed86e6affbea4)]*

### Bug Fix

- Scala script engine gone *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8186850](https://github.com/CleanroomMC/Cleanroom/commit/8186850f90e45f333655187e4a4d7383fd67ce13)]*
- Fix not excluding fastutil from transformer *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d430777](https://github.com/CleanroomMC/Cleanroom/commit/d430777e2bcb886d1c2ff141e9fd630d432d388f)]*
- TransformerWrapper now get priority correctly *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c107a51](https://github.com/CleanroomMC/Cleanroom/commit/c107a51baf5785429b299335193b7817905e037f)]*
- Update dependency to fix some mod adding cl exclusion *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [27cea6a](https://github.com/CleanroomMC/Cleanroom/commit/27cea6a529bf81b937afdf028d62a6d70391f480)]*
- Should not ignore loading plugin for MixinTweaker *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3c00e39](https://github.com/CleanroomMC/Cleanroom/commit/3c00e39a8e3834d6ef154d35f7932ceb9c0af732)]*
- Try use AutoCloseable instead of finalize *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f291fd3](https://github.com/CleanroomMC/Cleanroom/commit/f291fd328357c9ff044ef664ef174fdddf071093)]*
- Update foundation to fix mixin transforming itself *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [121952b](https://github.com/CleanroomMC/Cleanroom/commit/121952b9e9de730eb234a89c5dcd6050b170af35)]*
- Mods button wrong size *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [edf34f9](https://github.com/CleanroomMC/Cleanroom/commit/edf34f99b9ea522ae9b925bf699829821b5ba768)]*
- Fix glGetIntegerv not working *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5a27ca1](https://github.com/CleanroomMC/Cleanroom/commit/5a27ca1744bd264e7ab92e4916ff4b4ab286eaf3)]*
- Fix nashorn in foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9c15414](https://github.com/CleanroomMC/Cleanroom/commit/9c1541484b00149dc2190e3452fe31fd91392f9a)]*
- Update foundation to fix wrong ASM patching *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [df59662](https://github.com/CleanroomMC/Cleanroom/commit/df59662b944407a112737399210a0fc7a36a7946)]*
- JVM Driver was borken *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a88d63c](https://github.com/CleanroomMC/Cleanroom/commit/a88d63c7bc1f51ea48bcb03596ceb479a2bbd1f0)]*
- Update foundation to fix some mods using logwrapper *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [27cc481](https://github.com/CleanroomMC/Cleanroom/commit/27cc4814ca2b0b0611b5ab77a8582cbf37079fa3)]*
- Add back TerminalTransformer so TickCentral happy *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [605fba9](https://github.com/CleanroomMC/Cleanroom/commit/605fba93d43ab1760c11cc5ff70e489add90758e)]*

### Refactor

- Move scala out *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2f0b54d](https://github.com/CleanroomMC/Cleanroom/commit/2f0b54dd9c6ccff45f4d910d3f275d0c00da1729)]*
- Control Mixin phase in FML *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [15dea2b](https://github.com/CleanroomMC/Cleanroom/commit/15dea2bef09f0500fa68c81ed79aa5541c5b3634)]*

### Documentation

- Update readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [957c22a](https://github.com/CleanroomMC/Cleanroom/commit/957c22a6323e5ef47ebacdbb85e77e4c948f8255)]*
- Update deprecated ILate/EarlyMixinLoader javadoc *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [639c371](https://github.com/CleanroomMC/Cleanroom/commit/639c3718d1deccf4e7143e97dbe782b15aa2db8f)]*

### Build and Dependencies

- Update Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [77fec45](https://github.com/CleanroomMC/Cleanroom/commit/77fec45eea60e17c9fff1635b6930d982e8bcba9)]*
- Update foudation and Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7c04b26](https://github.com/CleanroomMC/Cleanroom/commit/7c04b266e4a36ebfced8c5c10bac89362e5aaac9)]*
- Update dependency *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b2010f4](https://github.com/CleanroomMC/Cleanroom/commit/b2010f42b57b9dea46ac3edc0f4da9f98b9755a7)]*

### Other

- Fix version without run number *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fe4839f](https://github.com/CleanroomMC/Cleanroom/commit/fe4839f0acd8cae28e40700b830ee9ed42ec52e3)]*
- Build installer *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d6fda63](https://github.com/CleanroomMC/Cleanroom/commit/d6fda634f5ad350a5f45645ccdedf12ab19cd484)]*
- Shouldn't add that space... *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f187c3a](https://github.com/CleanroomMC/Cleanroom/commit/f187c3ae49ba45739cb7ff32b85b7143e26613dd)]*
- Fetch whole history since we don't tags often *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a87f1e3](https://github.com/CleanroomMC/Cleanroom/commit/a87f1e334bfdaeb0e5feb4868537a46c4787f27c)]*
- Fetch tags true *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [aa72318](https://github.com/CleanroomMC/Cleanroom/commit/aa72318c1a3dec24e0e423cb9c88ffb38d41f660)]*
- More print message *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a466e57](https://github.com/CleanroomMC/Cleanroom/commit/a466e57b2f97eb00265bd311d89eef7903d60d9f)]*
- Blossom dumb *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [21d865c](https://github.com/CleanroomMC/Cleanroom/commit/21d865c03ebeeda48875d0b33a9be91e305b26b5)]*
- Update java 22 support for jvm-driver *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [eee22c6](https://github.com/CleanroomMC/Cleanroom/commit/eee22c6a3ca717654546fd89194ca4e4d7e78d94)]*
- Update Mixin to fix a transformer problem *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b193a9b](https://github.com/CleanroomMC/Cleanroom/commit/b193a9be177454f0a7ddfe54d18408a6fcc2e7ae)]*
- Update dependency *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9b68c1f](https://github.com/CleanroomMC/Cleanroom/commit/9b68c1fd44dd065962680bcdf4f7d697d8cae67c)]*
- Bump version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [bff8af0](https://github.com/CleanroomMC/Cleanroom/commit/bff8af020f76d991ca9b681ff14c2a62c6ec2fe5)]*
- Fix non-core mixin mods handling *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f194d7f](https://github.com/CleanroomMC/Cleanroom/commit/f194d7f42dc1a8edd371dc947e78c06444c04c5a)]*
- Remove duplicated unsafe in lwjgl3ify *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [57f95db](https://github.com/CleanroomMC/Cleanroom/commit/57f95dbc3b48a6d31e94f0ec35e692c50dab10da)]*
- Remove duplicate unsafe in ObjectHolder *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3079600](https://github.com/CleanroomMC/Cleanroom/commit/3079600edf3af6d11494434cb6ab088a1651e4c7)]*
- Update Mixin to fix fugue broken *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f795fa5](https://github.com/CleanroomMC/Cleanroom/commit/f795fa5d40d02f033f8e882a311849fa0df716c2)]*
- Update Mixin for mixin class modifiable *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c60e7a3](https://github.com/CleanroomMC/Cleanroom/commit/c60e7a32cd44c2d6ca5d3d40ab6ecdf4e4a86e87)]*
- Update icu4j *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [44733dc](https://github.com/CleanroomMC/Cleanroom/commit/44733dc2efb809ca5bc94414da49386c86c01fc1)]*
- Update slf4j *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fac5936](https://github.com/CleanroomMC/Cleanroom/commit/fac59360a479710e45d33809516e6f0c054e4071)]*
- Update jopt version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [79a9a0b](https://github.com/CleanroomMC/Cleanroom/commit/79a9a0b610b701f9995829e533383913307ed610)]*
- Bump version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [23e2f72](https://github.com/CleanroomMC/Cleanroom/commit/23e2f7225e1be750f6473b5f83aa185ba80da8d0)]*
- Decent netty now *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [19bce57](https://github.com/CleanroomMC/Cleanroom/commit/19bce577f7174fbaa5cebf0a6fb305f7a2a485e0)]*
- Final attempt *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [33247ef](https://github.com/CleanroomMC/Cleanroom/commit/33247ef657db21ffb0463496a7794c94ae43e3e9)]*
- Switch to netty-all by Forge *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2804172](https://github.com/CleanroomMC/Cleanroom/commit/28041727c5240e3779a60f14afccd0d6e40a5d61)]*
- Add MORE netty libs *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c41bb87](https://github.com/CleanroomMC/Cleanroom/commit/c41bb874e552ce4fbcf5834f10033af048179dea)]*
- Add missing netty epoll *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1b927b6](https://github.com/CleanroomMC/Cleanroom/commit/1b927b62f00a56626818d3c82fc8b0e707c21a5e)]*
- Ships netty *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0015d38](https://github.com/CleanroomMC/Cleanroom/commit/0015d387662d01168a41d024e5219720165aaad0)]*
- Fix openj9 support *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [760e833](https://github.com/CleanroomMC/Cleanroom/commit/760e8331b2d57c640500becf777f47f00a2fcbb6)]*
- Update WindowsTest.yml *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8f5d846](https://github.com/CleanroomMC/Cleanroom/commit/8f5d84639e6fa99b2d172fd606b829fad1693a68)]*
- Add openj9 support *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [56f157a](https://github.com/CleanroomMC/Cleanroom/commit/56f157ae7a1af03ecbf64475ddb8128d24a1c9c8)]*
- Fix some dumb edge case *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [74a526c](https://github.com/CleanroomMC/Cleanroom/commit/74a526c42d8f8529add655c3143d12f0992c008a)]*
- Add extra methods for nothirium *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4fee087](https://github.com/CleanroomMC/Cleanroom/commit/4fee087598e0bbcd2ca74a20b08e9e83160ecbe5)]*
- Update foundation to fix lwjgl dwheel *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [70cca12](https://github.com/CleanroomMC/Cleanroom/commit/70cca12d8348aa0d5eb96f4c77d8dc71ad2b0a45)]*
- Update foundation to fix dynamic surrounding *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [eec1aeb](https://github.com/CleanroomMC/Cleanroom/commit/eec1aebfc4e5e4756d2d285084fbe4e48ee86c31)]*
- Update Mixin from upstream *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e64a51c](https://github.com/CleanroomMC/Cleanroom/commit/e64a51cf11c4f817e2976d07ef94f448bfd70cf3)]*
- Add CRL mod type *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [da2774e](https://github.com/CleanroomMC/Cleanroom/commit/da2774e0877859048daeae565305455ed69202d6)]*
- Fix loading adapter still too early *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9d8bedc](https://github.com/CleanroomMC/Cleanroom/commit/9d8bedc0e73eeec048be76bb7e1795a5aa8468d9)]*
- Delay language adapter shipped with mods *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a2cb137](https://github.com/CleanroomMC/Cleanroom/commit/a2cb1370d3846669fd9ffc5f477d71388a9b8ba4)]*
- Bump version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2661a74](https://github.com/CleanroomMC/Cleanroom/commit/2661a742d3b225e2f3ac618a2104eb651845fb35)]*
- Bump to 3036 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d6a43d3](https://github.com/CleanroomMC/Cleanroom/commit/d6a43d3688bbe7f2e23d6a571193f43dd967c9a0)]*
- Revert "Bump version" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5ed4d11](https://github.com/CleanroomMC/Cleanroom/commit/5ed4d11c75c6db8ed7a422260fbf757039a08b84)]*
- Bump version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [de06b16](https://github.com/CleanroomMC/Cleanroom/commit/de06b1669d6430557417278411ec9fc76c2e3c94)]*
- Update ASM *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ea5d2b8](https://github.com/CleanroomMC/Cleanroom/commit/ea5d2b840cd65a7f10d175c7444fc95f473bd891)]*
- Fix getting config from tweaker mod *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [faf331f](https://github.com/CleanroomMC/Cleanroom/commit/faf331f9647dced49499e6b150515c14e1676106)]*
- Remove manually ignore Mixin ITweaker mods *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0059dd3](https://github.com/CleanroomMC/Cleanroom/commit/0059dd35919b892f93e1b38ae45a3fa773713970)]*
- Fix not adding MixinTweaker non coremod to cp *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [34f88de](https://github.com/CleanroomMC/Cleanroom/commit/34f88de3d9414d56fc1c5980c16b508e9e9926b9)]*
- Fix URI not absolute *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [67e36c8](https://github.com/CleanroomMC/Cleanroom/commit/67e36c840e3a22bf16270ef98e3bda7bb6c28cfc)]*
- Add NonModDeps attribute for non mod libs *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d7c966c](https://github.com/CleanroomMC/Cleanroom/commit/d7c966ccc7bcb3ef5c0d5d0fbd9d6c0f852783ba)]*
- FIx wrong logic *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e9a45f7](https://github.com/CleanroomMC/Cleanroom/commit/e9a45f7c7aaf77377befea66440704b3bee9d037)]*
- Fix calling null's method *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [09f9f5e](https://github.com/CleanroomMC/Cleanroom/commit/09f9f5edb57d4e4c8f191d4de6636eadcc181ec5)]*
- Fix conflict with Mixin config searching *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [961b476](https://github.com/CleanroomMC/Cleanroom/commit/961b4762ebed491a54f515341f5ab5d739903a10)]*
- Switch early cp to normal behavior *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e63a3fa](https://github.com/CleanroomMC/Cleanroom/commit/e63a3fa00a90aec92672512c4720550c31329511)]*
- Exclude eraly class path from cp mod scanning *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [db3538e](https://github.com/CleanroomMC/Cleanroom/commit/db3538e74c0d9ef3e458e091dd1364cba83c6bbf)]*
- Prevent duplicate cp in a new way *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a38736d](https://github.com/CleanroomMC/Cleanroom/commit/a38736d8dff820abca6bbdb8f10632eb133b0a20)]*
- Update dependency, handle guava correctly this time *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [442c64c](https://github.com/CleanroomMC/Cleanroom/commit/442c64ce1c20c2395dfba9d6c9c5ab069421c771)]*
- Add wider guava exclusion *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ab3d0b1](https://github.com/CleanroomMC/Cleanroom/commit/ab3d0b1f1bce350e45a0fc405874d5290a72d13b)]*
- Roll back to 0.12.3 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [255b7db](https://github.com/CleanroomMC/Cleanroom/commit/255b7db8f0ed9534d82420f76d8156c04deb9aac)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5e4dc03](https://github.com/CleanroomMC/Cleanroom/commit/5e4dc03274231a7862d689e4b6056b6e1a89496c)]*
- Bump foundation version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4e5fb0f](https://github.com/CleanroomMC/Cleanroom/commit/4e5fb0ff3c5e8e89c9091354110fa017503d14ec)]*
- Ignore mod api container validate *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2e54326](https://github.com/CleanroomMC/Cleanroom/commit/2e54326b06108a8443a7fdd119dece6a9990878f)]*
- Remove akka dependency, they are fxxking scala *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b5d62f5](https://github.com/CleanroomMC/Cleanroom/commit/b5d62f56697261ff554658c7caa922e32ad59b59)]*
- Fix guava loaded by appCL *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b28364c](https://github.com/CleanroomMC/Cleanroom/commit/b28364cf1ee967b0367ae11583209c100b3892ba)]*
- Fix not adding cp if no manifest *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [00d0dbd](https://github.com/CleanroomMC/Cleanroom/commit/00d0dbd7c07393e2a8dfa24f08f60ae2dc5d1e75)]*
- Fix adapter map being used before initialized *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2b470f4](https://github.com/CleanroomMC/Cleanroom/commit/2b470f47112f1e6b9cad920b989e2a5b4408dd8e)]*
- Remove scala adapter *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a008ef1](https://github.com/CleanroomMC/Cleanroom/commit/a008ef1b8b022193ba14a39d4a20484d0eab7cb0)]*
- Move lang adapter registry out of FMLModContainer *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ee450c3](https://github.com/CleanroomMC/Cleanroom/commit/ee450c3b107ad8d3f395fb78b545fd4169529006)]*
- Remove addURL in later stage *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8eb72ab](https://github.com/CleanroomMC/Cleanroom/commit/8eb72ab50b906e9a733554f0f9742d2004d00dbd)]*
- Make language adapter register-able *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f508420](https://github.com/CleanroomMC/Cleanroom/commit/f5084207ce898f495e9190c083f3ae210347fb1c)]*
- Experimental early cp adding *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [656d39c](https://github.com/CleanroomMC/Cleanroom/commit/656d39ce65dc58229272189139517381773f95c9)]*
- Revert "fix: try use AutoCloseable instead of finalize" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ec0c102](https://github.com/CleanroomMC/Cleanroom/commit/ec0c10235b0284f73ce433470ffeccbef8d5430b)]*
- Test update dependencies *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1684911](https://github.com/CleanroomMC/Cleanroom/commit/16849110fc44325525ba1c3bb2e0f656105ed459)]*
- Test dependency update *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [14185ee](https://github.com/CleanroomMC/Cleanroom/commit/14185eebab1052d449fd60789ffe29c207b1e113)]*
- Revert "Test bump version" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5d61632](https://github.com/CleanroomMC/Cleanroom/commit/5d61632bc3d6dabd3a6a0e578df23297880cf532)]*
- Test bump version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ac89a3e](https://github.com/CleanroomMC/Cleanroom/commit/ac89a3ed28548a75abc66faf6329b3b020824a5f)]*
- Update foundation *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [745e2ab](https://github.com/CleanroomMC/Cleanroom/commit/745e2abcf28b8b7e799c7c50a5cc29d61b03d5d8)]*
- Uncomment lagging build script *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [330c0d2](https://github.com/CleanroomMC/Cleanroom/commit/330c0d21453a34b2ff5a91f25d0c36c1c5143bae)]*
- Update dependency *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [67d8957](https://github.com/CleanroomMC/Cleanroom/commit/67d89570169d263ec56a93cabd081671717ca420)]*
- Foundation check point *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [25f009b](https://github.com/CleanroomMC/Cleanroom/commit/25f009b258e7d4539b0c6a67c68f3862f5dd3f7c)]*
- Add imagine breaker to installer *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c8ae4c4](https://github.com/CleanroomMC/Cleanroom/commit/c8ae4c441651a6f830752d961d0becdb8c8e0d7f)]*
- Bump version to prevent misunderstanding *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [dfcdbb2](https://github.com/CleanroomMC/Cleanroom/commit/dfcdbb22614d08e553417ebdf2ba9e4ac759a331)]*
- Fix wrong jarLocation in FMLTweaker *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [de8ae56](https://github.com/CleanroomMC/Cleanroom/commit/de8ae566b26d79bdac5758ea5b539808a02396a1)]*
- Fix transformer system broken *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [66c9daa](https://github.com/CleanroomMC/Cleanroom/commit/66c9daac409a8d79749c887dee6d8d325815e69e)]*
- Fix deobf transformer, trying to fix event broken *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8f7e403](https://github.com/CleanroomMC/Cleanroom/commit/8f7e40383195a9d348cf9daefb1faf670c269867)]*
- Check point 2 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [38cf9e4](https://github.com/CleanroomMC/Cleanroom/commit/38cf9e402a64293dda5b0254ed4a70070a275644)]*
- Foundation check point 1 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [16be299](https://github.com/CleanroomMC/Cleanroom/commit/16be299c6d22647d98eb0093457aa19296c8b990)]*

**Full Changelog**: https://github.com/CleanroomMC/Cleanroom/compare/0.1.0...0.2.0

## 0.1.0 - 2024-05-04

### Feature

- Add forge early config *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [894e303](https://github.com/CleanroomMC/Cleanroom/commit/894e303f7d4dcf259ed60fd5dd533cc741aa247b)]*
- Simplify bash syntax *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [265cc62](https://github.com/CleanroomMC/Cleanroom/commit/265cc62c672ecbb06e559ad90dca678f4712f5e7)]*
- Bleeding Edge indicator *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [768fa41](https://github.com/CleanroomMC/Cleanroom/commit/768fa414a1f240c01a325380ed67203356f58a24)]*
- Now can choose preivous world type by right click *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [01233dd](https://github.com/CleanroomMC/Cleanroom/commit/01233dd416fa1e72c61721901135e0cc199d061c)]*

### Bug Fix

- Dont trigger dispatch event when pr happens *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [d51e9d1](https://github.com/CleanroomMC/Cleanroom/commit/d51e9d126e7a59aa6606636c3f78c918e5d9acd0)]*
- Move early config later *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [31b27b3](https://github.com/CleanroomMC/Cleanroom/commit/31b27b3643cf3e0b37c1de4f2920bc552b3800a3)]*
- Wrong delimiter *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [f36dace](https://github.com/CleanroomMC/Cleanroom/commit/f36dace9cd5c7d882268ef67d34dcdc23494050a)]*
- GITHUB_REF_NAME not exist in PR context *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [82bde2a](https://github.com/CleanroomMC/Cleanroom/commit/82bde2a6df0b1691373dabc501369208a1d3c569)]*
- Assign eval require no space between *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [a14ab11](https://github.com/CleanroomMC/Cleanroom/commit/a14ab11face69c18ba080d2be0f477efc703acd2)]*
- Fetch correct value for branch feat *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [1f01f72](https://github.com/CleanroomMC/Cleanroom/commit/1f01f725ebad7085f0243d6b8a242243e2981840)]*
- Fix config any time broken when not providing config name *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [45ed7e7](https://github.com/CleanroomMC/Cleanroom/commit/45ed7e7f4eb566da65c375ee22cef2423de9a2d2)]*
- Update ASMTransformerWrapper visitor class version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2d935d4](https://github.com/CleanroomMC/Cleanroom/commit/2d935d410f73c76aed9742c23964f80b536ec3d6)]*
- Fix configanytime container not having a file *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [daa0154](https://github.com/CleanroomMC/Cleanroom/commit/daa0154253c6637d5e35a49fc3c592cb8cb90c23)]*
- Fix cleanroom not loading as a mod on non-dev, so its capabilities are *[commit by [@PrototypeTrousers](https://github.com/PrototypeTrousers) in [bb471c7](https://github.com/CleanroomMC/Cleanroom/commit/bb471c797dc8232d8bf3c01edfaa48e80701a157)]*

### Refactor

- Use defineClass from bouncepad *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9341b3f](https://github.com/CleanroomMC/Cleanroom/commit/9341b3f3ce5722afde2e7a06e707557c21f03cb8)]*

### Documentation

- Add link to cleanroom wiki in readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3126e30](https://github.com/CleanroomMC/Cleanroom/commit/3126e302d3a695626f0b500dab774b283d771ca2)]*

### Testing

- Test *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a076f4c](https://github.com/CleanroomMC/Cleanroom/commit/a076f4ccf38d3ad3b388da1348fb88207e0d224c)]*

### CI

- Add actor to dispatch *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a7495c7](https://github.com/CleanroomMC/Cleanroom/commit/a7495c7772fa5d64e3338e5839955e0fc7434caa)]*

### Other

- Versoning *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [cd37590](https://github.com/CleanroomMC/Cleanroom/commit/cd375901b837b77a17f2abadeddfcbf7f8a6b5c0)]*
- Fast item stack copy *[PR by [@friendlyhj](https://github.com/friendlyhj) in [#132](https://github.com/CleanroomMC/Cleanroom/pull/132)]*
- Pass EntityPlayer in AnvilUpdateEvent *[PR by [@friendlyhj](https://github.com/friendlyhj) in [#133](https://github.com/CleanroomMC/Cleanroom/pull/133)]*
- Disable useless eventhandler, use commit message as runname *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [687c037](https://github.com/CleanroomMC/Cleanroom/commit/687c0377ba7549c7968e2e750a475c5c6938d16f)]*
- Better action naming *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c654288](https://github.com/CleanroomMC/Cleanroom/commit/c654288063413dea7b580b7d1bb6243a19eb2c1d)]*
- Patch implementation of NonConflictKeys *[PR by [@VulpesStella](https://github.com/VulpesStella) in [#125](https://github.com/CleanroomMC/Cleanroom/pull/125)]*
- Add warning about login before downloading action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [df71d41](https://github.com/CleanroomMC/Cleanroom/commit/df71d4161c7a07119d9d3342d54e1513d1750c76)]*
- Update action version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [361f403](https://github.com/CleanroomMC/Cleanroom/commit/361f403225124bded0a51711139a318e02f876ec)]*
- Update icu4j *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c0513b8](https://github.com/CleanroomMC/Cleanroom/commit/c0513b8a85d138da9eea3f18fa95086aeecf7ca4)]*
- Add run number to dispatch json *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7124ed2](https://github.com/CleanroomMC/Cleanroom/commit/7124ed25038fa587749b1a1bb84a3764da98b06a)]*
- Fix spinning glitch *[commit by [@VulpesStella](https://github.com/VulpesStella) in [64edf72](https://github.com/CleanroomMC/Cleanroom/commit/64edf72324b24dd480c2a0cd762f287c6ab19afe)]*
- Update test action *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a0f232a](https://github.com/CleanroomMC/Cleanroom/commit/a0f232a5fa3e40e02b008f5d16454b86d2ec61b1)]*
- Add all constants from lwjgl2 AL10 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7685ce9](https://github.com/CleanroomMC/Cleanroom/commit/7685ce9679aa2b8b327ceb27dd1e451ff90a81a6)]*
- Add more check in issue template *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [93ddeb1](https://github.com/CleanroomMC/Cleanroom/commit/93ddeb1082d284f9d3ed0a81cbade7d868b1724f)]*
- Update readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [29acf54](https://github.com/CleanroomMC/Cleanroom/commit/29acf54193b749c25b6cfb9e346d215fe498e634)]*
- Fixes Display::setTitle behaviour after window is created. *[PR by [@KasumiNova](https://github.com/KasumiNova) in [#102](https://github.com/CleanroomMC/Cleanroom/pull/102)]*
- Fix text wrapping (well tested this time) *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e1b6ad5](https://github.com/CleanroomMC/Cleanroom/commit/e1b6ad56e7e32472a6b25464d816b3b0734a82cd)]*
- Actually apply ASCII font when "forceUnicode" is off *[PR by [@ZZZank](https://github.com/ZZZank) in [#100](https://github.com/CleanroomMC/Cleanroom/pull/100)]*
- [experimental] test ci 7 💀 *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [53d47a8](https://github.com/CleanroomMC/Cleanroom/commit/53d47a814f70ddfdad5a227ff72135df78f7408b)]*
- [experimental] test ci 6 *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [40e6ed6](https://github.com/CleanroomMC/Cleanroom/commit/40e6ed69190e938fd76b0d9929552fc847df7145)]*
- [experimental] test ci 5 💀 *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [08493c9](https://github.com/CleanroomMC/Cleanroom/commit/08493c93481bd768fc39453b41d0df8592cbe572)]*
- [experimental] test ci 4 *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [7cde053](https://github.com/CleanroomMC/Cleanroom/commit/7cde053dea55d4fe3223cd5f818bd21e83a0272c)]*
- [experimental] test ci 3 *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [37b61b5](https://github.com/CleanroomMC/Cleanroom/commit/37b61b58f05fa01f3e101f4f7a4d4f08e55f24bf)]*
- [experimental] test ci 2 *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [5b459c7](https://github.com/CleanroomMC/Cleanroom/commit/5b459c7cd147cc61872635e6dc6d8f4e0c9e89e0)]*
- Make EntitySlime fire `LivingJumpEvent` *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#93](https://github.com/CleanroomMC/Cleanroom/pull/93)]*
- [experimental] test ci *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [f82269e](https://github.com/CleanroomMC/Cleanroom/commit/f82269ee89f6242269c62a2b81c12ccd36c12fa5)]*
- Fixes #88 *[commit by [@Rongmario](https://github.com/Rongmario) in [91598a8](https://github.com/CleanroomMC/Cleanroom/commit/91598a89f3d9c88d4b6f3c5a5ddbc0cc1dd10fa2), issue by [@tuca1234567890](https://github.com/tuca1234567890)]*
- Don't need to check Cleanroom version status as we not initial any versioning yet *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [8eed207](https://github.com/CleanroomMC/Cleanroom/commit/8eed207aeb3126b8e1f389ea9eac18e020af0f74)]*
- Don't need to add lwjgl3 to mod discover *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [c66c917](https://github.com/CleanroomMC/Cleanroom/commit/c66c91714f6eee33e043f58a71f8757a3ccd71b2)]*
- Update installer 2.1.x => 2.2.x to fix Mojang sunsetting session server *[commit by [@Rongmario](https://github.com/Rongmario) in [29e48a9](https://github.com/CleanroomMC/Cleanroom/commit/29e48a994d1988e4ed5512072a058f195dbe219d)]*
- Integrate config any time *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#82](https://github.com/CleanroomMC/Cleanroom/pull/82)]*
- Mention Fugue to fix old mod incompatibility *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [2ef257b](https://github.com/CleanroomMC/Cleanroom/commit/2ef257bfe3b1a024344e4c98754abab796f0afa3)]*
- Draft an Issue Template *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [a9d1a72](https://github.com/CleanroomMC/Cleanroom/commit/a9d1a7206553fe5e18da95fe2256b19d79def9b1)]*
- Make Ender Dragon's XP drop fire LivingExperienceDropEvent for each "batch" of XP *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#71](https://github.com/CleanroomMC/Cleanroom/pull/71)]*
- Make horses and llama's fire LivingFallEvent. *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#28](https://github.com/CleanroomMC/Cleanroom/pull/28)]*
    *[Fixes issue #13 by [@VulpesStella](https://github.com/VulpesStella)]*
- Label Nullable and return null at IFMLLoadingPlugin *[PR by [@Ecdcaeb](https://github.com/Ecdcaeb) in [#59](https://github.com/CleanroomMC/Cleanroom/pull/59)]*
- Cleanroom Feature Proposal *[commit by [@Rongmario](https://github.com/Rongmario) in [382f70d](https://github.com/CleanroomMC/Cleanroom/commit/382f70d06cf7da6ff8533150716c738921d62e7b)]*
- Comments for the future lwjglx cleanup *[commit by [@Rongmario](https://github.com/Rongmario) in [b9ea9bd](https://github.com/CleanroomMC/Cleanroom/commit/b9ea9bdba27b2810136695cb7f90a1a49d92fd00)]*
- Fix broken IME input *[commit by [@LasmGratel](https://github.com/LasmGratel) in [ea32e0c](https://github.com/CleanroomMC/Cleanroom/commit/ea32e0cd44022faa1a9aa2a64553276179a975df)]*
- Update imagine breaker again *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0f506f4](https://github.com/CleanroomMC/Cleanroom/commit/0f506f47169fa1995a6606b6861210809ba41726)]*
- Update to imagine breaker 2.0 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b9ea2fd](https://github.com/CleanroomMC/Cleanroom/commit/b9ea2fdb94366dae426db49aa2495a57d62ec918)]*
- Move break iterator outside of patch *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7796b5b](https://github.com/CleanroomMC/Cleanroom/commit/7796b5b7b3fcbb5d5c347a45244e65e7f48f9d96)]*
- MacOSX Rejoice *[commit by [@Rongmario](https://github.com/Rongmario) in [5b930f0](https://github.com/CleanroomMC/Cleanroom/commit/5b930f094509c64c3ea729cccc050817e156ef92)]*
- Add download and installation instructions in readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2e8e13d](https://github.com/CleanroomMC/Cleanroom/commit/2e8e13d632c78e256a1c240d51e1fcc2b07cc3c2)]*
- Fix ALT key preventing movement when held. *[commit by [@Nessiesson](https://github.com/Nessiesson) in [a43d895](https://github.com/CleanroomMC/Cleanroom/commit/a43d8954993f94900d901c086de33ae78ac24459)]*
- Testing with other plugin *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [c4a70f3](https://github.com/CleanroomMC/Cleanroom/commit/c4a70f39e8eda02b5f5d91b451772dfbd64e1df0)]*
- Refactor abit *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [babfe94](https://github.com/CleanroomMC/Cleanroom/commit/babfe945e122e70e30b7aa002fca25c1f85f1e21)]*
- Fix issue with GHA script *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [dc1fabd](https://github.com/CleanroomMC/Cleanroom/commit/dc1fabd76b91b1e4fc2cb99387ab63ab1d943761)]*
- Added hook to dispatch event: trigger GHA on CleanroomMMC repo. *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [fafa865](https://github.com/CleanroomMC/Cleanroom/commit/fafa865074c1cfa78e51a50ba028f9b33c2f5cfd)]*
- Add fastutil to classloading exclusion *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4fe707b](https://github.com/CleanroomMC/Cleanroom/commit/4fe707b96c6effc03f56b6be7a5d4cf9e8dc82ec)]*
- Correctly set window's starting posX + posY, give hint for refresh rate *[commit by [@Rongmario](https://github.com/Rongmario) in [37de34b](https://github.com/CleanroomMC/Cleanroom/commit/37de34bbca7aba949789ff6b66cecb170ba5d3a7)]*
- Set UTF-8 for compiler encoding *[commit by [@Rongmario](https://github.com/Rongmario) in [f5b73dc](https://github.com/CleanroomMC/Cleanroom/commit/f5b73dc8426f07909cb28b49c987cf7a305ea316)]*
- Add JNA so oshi works *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [60d7a30](https://github.com/CleanroomMC/Cleanroom/commit/60d7a309263dfbdc08a90db8edf1aaf65f00e779)]*
- Update bouncepad, make reflection transformer normal again *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [06aba57](https://github.com/CleanroomMC/Cleanroom/commit/06aba57a8e311b084a9f559d2133ffe3d3105bec)]*
- Remove unnecessary check in FontRenderer *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ff7455a](https://github.com/CleanroomMC/Cleanroom/commit/ff7455af7d885abf439acb7017c5a38c9333b247)]*
- Experimentally rewrote text wrapping *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4d525dc](https://github.com/CleanroomMC/Cleanroom/commit/4d525dc9ebaa898e54e3c92adc8cdb410c92a0e8)]*
- Remove jinput dependency *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [16a16d6](https://github.com/CleanroomMC/Cleanroom/commit/16a16d6d1c731f988426d088480daebd74d9afad)]*
- Experimental raw input from lwjgl3 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [09ee654](https://github.com/CleanroomMC/Cleanroom/commit/09ee654ffc756ff7ad7d0fc4494199b6410332d3)]*
- Add jinput natives correctly. That's a bad idea *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e385179](https://github.com/CleanroomMC/Cleanroom/commit/e385179e28b3bf90c6662e5ea94894dbff7015a5)]*
- Add jinput natives *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d9f21d0](https://github.com/CleanroomMC/Cleanroom/commit/d9f21d08d9550de6a259a5302b610d473df1a2c7)]*
- Add jinput manually *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [23e9d2e](https://github.com/CleanroomMC/Cleanroom/commit/23e9d2e6a8fb0142d01c635abf592599108a7c2f)]*
- Fix return type of alcCloseDevice *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9e04899](https://github.com/CleanroomMC/Cleanroom/commit/9e04899e84229473a3f6cf9a85392c636b94ab66)]*
- Add alcCloseDevice to ALC10 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8860cd0](https://github.com/CleanroomMC/Cleanroom/commit/8860cd0c0d6dd634f877458ed48ea431747153da)]*
- Update bouncepad exclusion *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1f4f7f7](https://github.com/CleanroomMC/Cleanroom/commit/1f4f7f757cb2b837cb54dab5b6b408ae8a44b510)]*
- Fix wrong super transformer *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [38947b1](https://github.com/CleanroomMC/Cleanroom/commit/38947b18d64c24e2135de88b16991d493b4f61eb)]*
- Update bouncepad and use super transformer *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8cd42e3](https://github.com/CleanroomMC/Cleanroom/commit/8cd42e3cc683b84532d6ccfeefeae87a3f262903)]*
- Update caches when adding Enum *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e772f7f](https://github.com/CleanroomMC/Cleanroom/commit/e772f7f2e93bdb097b81a219c90d1287521aa87f)]*
- Bump version for Fugue *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [eb42f0d](https://github.com/CleanroomMC/Cleanroom/commit/eb42f0d043a61fecb8045efe5e734b364c5445aa)]*
- We don't need to test Windows now, that's a waste of action time *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2ab1219](https://github.com/CleanroomMC/Cleanroom/commit/2ab121900a61bbe196075cb1f69380eda33ebec4)]*
- Update to experimental ModifyArgs fixed Mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [634468d](https://github.com/CleanroomMC/Cleanroom/commit/634468d14d8b306c129a1c2cfcb6bc2b6ddd4d4d)]*
- Update a lot libraries. Always newer! *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7bb4745](https://github.com/CleanroomMC/Cleanroom/commit/7bb4745a7336514b9b93d7485b4f652b6b04ed5a)]*
- Update lwkglx OpenAL, fix incompat with SoundPhycsic *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [159412a](https://github.com/CleanroomMC/Cleanroom/commit/159412acb83e44c4229b74b8ea4abf49268e0899)]*
- Update MixinExtra *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3b5303d](https://github.com/CleanroomMC/Cleanroom/commit/3b5303dee5bbd2984b7f956050eff56e7fe87fd5)]*
- Add information about fugue *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1e1206f](https://github.com/CleanroomMC/Cleanroom/commit/1e1206f8c4e39531f113552c7e065035f48586c9)]*
- Andriod support thanks to @ShirosakiMio *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c9dca44](https://github.com/CleanroomMC/Cleanroom/commit/c9dca4411ae7ea66d1e503547f3f201e7b24457e)]*
- Fix ASMUpper mistake in bouncepad *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [012fb36](https://github.com/CleanroomMC/Cleanroom/commit/012fb3612ea1ee0b55a33c5ba256d09636b94c87)]*
- Fix stupid error in bouncepad *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [eea0c81](https://github.com/CleanroomMC/Cleanroom/commit/eea0c81e33783a86dd54be01afd5dd1ef26611b6)]*
- Should fix TickCentral ... but why? *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1eb0524](https://github.com/CleanroomMC/Cleanroom/commit/1eb052492032dee352dbe0ec4ba1dc0c33eb6b88)]*
- Exclude lwjgl3 in bouncepad *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [32590a9](https://github.com/CleanroomMC/Cleanroom/commit/32590a92f0b772d77e7565a7937663312492080a)]*
- Update oshi *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a40ed10](https://github.com/CleanroomMC/Cleanroom/commit/a40ed1076142c4b2be7cd3a3780e7538ab0577ea)]*
- Cleanup FontRenderer, revert certain methods to fix crash with TiC *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [69cac13](https://github.com/CleanroomMC/Cleanroom/commit/69cac1371cb85d3aa7bc615c6072ba2402795d9f)]*
- Update readme *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [cf59ba1](https://github.com/CleanroomMC/Cleanroom/commit/cf59ba1080dc2bf7eb3f60e4ae5cff82639cb042)]*
- Prevent mods calling Mixin init *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0d95a3f](https://github.com/CleanroomMC/Cleanroom/commit/0d95a3fb7260c5ce363350d523673e9c798511e7)]*
- Add alcCloseDevice to lwjglx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5c42b8e](https://github.com/CleanroomMC/Cleanroom/commit/5c42b8ec55d16de0669f35f3085ea69a0a8b18fb)]*
- Use up-to-date bouncepad with better ASMUpper *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [86a1cb6](https://github.com/CleanroomMC/Cleanroom/commit/86a1cb6408563fc4a2763bd500c2002a1265c29b)]*
- Remove var to make srg2source happier *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b0eb820](https://github.com/CleanroomMC/Cleanroom/commit/b0eb820577dd43f9efd99a451b3713e2d0a4f122)]*
- Bump version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [32cf675](https://github.com/CleanroomMC/Cleanroom/commit/32cf675dafa1a13e2f7cc0c561891655a092b5a8)]*
- Map glGetActiveUniform(III)String to glGetActiveUniformName(III)String *[commit by [@Rongmario](https://github.com/Rongmario) in [1e11ed7](https://github.com/CleanroomMC/Cleanroom/commit/1e11ed7bdd6bae579a48850b8fedc82f91a17d38)]*
- Add missing method in lwjglx, roll back bouncepad *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [fe3e25b](https://github.com/CleanroomMC/Cleanroom/commit/fe3e25b07406208371c89123d2581daf30afdb8d)]*
- Add Mixin exclusion in bouncepad, Change var to make FG happy *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2d81f80](https://github.com/CleanroomMC/Cleanroom/commit/2d81f80182de0e44aa0fe8e323b0eab25c9989d1)]*
- Cleanup one(1) patch *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ad37502](https://github.com/CleanroomMC/Cleanroom/commit/ad3750276bc45f997ab207be0b896518c4eb6e69)]*
- Bump version for MDK *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [81a79fb](https://github.com/CleanroomMC/Cleanroom/commit/81a79fb55fe3a8c8e5f52f13b3821efd5e2f552a)]*
- Bump version *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [7b12613](https://github.com/CleanroomMC/Cleanroom/commit/7b12613ba2b7cc7c85fe6a622e27536a4b78b63b)]*
- Add sun.reflect.Reflection remapping for ... FutureMC? *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2352fa7](https://github.com/CleanroomMC/Cleanroom/commit/2352fa76165a182ffc5dc9f728718da7ed04205e)]*
- Add helper method for classloader casting ASM *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [de6d5e2](https://github.com/CleanroomMC/Cleanroom/commit/de6d5e2a0967e22b3aa5ef68c606b4d47dff0be6)]*
- Setup mapping in appClassLoader too for Mixin to gather *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [af0f60f](https://github.com/CleanroomMC/Cleanroom/commit/af0f60f4136a123f68aab31c960aff7545db7d2f)]*
- Add ASM for mods do ClassLoader casting *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [cc57e6b](https://github.com/CleanroomMC/Cleanroom/commit/cc57e6bab23ac04e686df0405da9e276c1245884)]*
- Move Mixin init to Launch Classloader to fix LocalRef not found *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e00d63b](https://github.com/CleanroomMC/Cleanroom/commit/e00d63becd33cd74a690bafa66e1bd7ff6187dc2)]*
- Fix yet another crash in typesetting *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [aa46ceb](https://github.com/CleanroomMC/Cleanroom/commit/aa46ceb1cd4e91645b28c52d60b96ae82cc60bae)]*
- [Feature] Mouse improvement *[PR by [@KorewaLidesu](https://github.com/KorewaLidesu) in [#22](https://github.com/CleanroomMC/Cleanroom/pull/22)]*
- Add missing type to Unsafe helper *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [e025f1c](https://github.com/CleanroomMC/Cleanroom/commit/e025f1c5a20e7014c7e9da63d62b46514bb70549)]*
- Update mcpconfig *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [67cbedb](https://github.com/CleanroomMC/Cleanroom/commit/67cbedbc0338ea7bfb3726892644fd873d14df79)]*
- Switch decompiler to Vineflower *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#21](https://github.com/CleanroomMC/Cleanroom/pull/21)]*
- Dependency change: Scala (2.12 -> 2.11) *[PR by [@KorewaLidesu](https://github.com/KorewaLidesu) in [#20](https://github.com/CleanroomMC/Cleanroom/pull/20)]*
- Remove type hardcode (hope this fix weird issue with type casting) *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [f27dc6d](https://github.com/CleanroomMC/Cleanroom/commit/f27dc6d2990ce12c44bdce73c038a3cf370f3ec9)]*
- Enable Mixin debug *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [7b9557a](https://github.com/CleanroomMC/Cleanroom/commit/7b9557a4987d219f9cb2a3fc0447c2fd07d6ad5a)]*
- Fix missing method on Display.java (VintageFix compat) *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [9884758](https://github.com/CleanroomMC/Cleanroom/commit/988475863d77b433cd488dd824ab4c543db40a38)]*
- Revert "Comment out new Scala API" *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [fc6ac92](https://github.com/CleanroomMC/Cleanroom/commit/fc6ac92e2984cf26d75ba18f03566e8cccd7af4b)]*
- Comment out new Scala API *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [0332731](https://github.com/CleanroomMC/Cleanroom/commit/0332731526df7d4da9d759b5951bf841a8eff2a8)]*
- Update bouncepad to fix nashorn *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0562904](https://github.com/CleanroomMC/Cleanroom/commit/05629041c8ec59872a0dfa39557296b1e84b8fab)]*
- Use good ol' Nashorn instead, keep compatibility with existing syntax *[commit by [@Rongmario](https://github.com/Rongmario) in [f219f40](https://github.com/CleanroomMC/Cleanroom/commit/f219f405c0bb6df68988772a4768de858fe4906f)]*
- Deal with old malformed UUID's being invalid after Java 8 *[commit by [@Rongmario](https://github.com/Rongmario) in [020ace2](https://github.com/CleanroomMC/Cleanroom/commit/020ace297f2ce1c11cc96530831ad47e0df27dce)]*
- ScriptEngineTransformer *[commit by [@Rongmario](https://github.com/Rongmario) in [1e58062](https://github.com/CleanroomMC/Cleanroom/commit/1e58062704da4efb39321fd66955c73a12c05cf1)]*
- Add our own loader package to exempt from being transformed *[commit by [@Rongmario](https://github.com/Rongmario) in [d166c95](https://github.com/CleanroomMC/Cleanroom/commit/d166c95d21e8e8d43acd0b7dacbb8976d5d6a340)]*
- Only write bytes if class is transformed for ReflectionFieldTransformer *[commit by [@Rongmario](https://github.com/Rongmario) in [835895e](https://github.com/CleanroomMC/Cleanroom/commit/835895eb16596bb67b86aae1073d60f916b02277)]*
- Add rhino runtime *[commit by [@Rongmario](https://github.com/Rongmario) in [691b4ed](https://github.com/CleanroomMC/Cleanroom/commit/691b4ed269502613e47d04bb8d1592e5ad1e5d1c)]*
- New bouncepad, add nashorn *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1a45675](https://github.com/CleanroomMC/Cleanroom/commit/1a45675d4964cd7e1537760304361cf8de6e0330)]*
- Add Mozilla Rhino as the default JavaScript ScriptEngine *[commit by [@Rongmario](https://github.com/Rongmario) in [e722f29](https://github.com/CleanroomMC/Cleanroom/commit/e722f2909f47223b55cb937c2f7082f0d876dcdf)]*
- Revert back to regular sorting index value *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b3d6837](https://github.com/CleanroomMC/Cleanroom/commit/b3d6837ce470ef7cce8dfeb116bcd5b57f0cde01)]*
- Update MixinExtra *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ce69a04](https://github.com/CleanroomMC/Cleanroom/commit/ce69a044a0cc88c3e16688599772c41536b546b4)]*
- Transformer now supports Field primitive sets and gets + cleanup *[commit by [@Rongmario](https://github.com/Rongmario) in [91b220f](https://github.com/CleanroomMC/Cleanroom/commit/91b220fec6ffa69aa79051dcbbc5f816cd81a978)]*
- Unsafe reflect from Nigh Hells *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e88d0d2](https://github.com/CleanroomMC/Cleanroom/commit/e88d0d23138c0b23bf3579bca8f9ea9fd3b96ad6)]*
- Add kotlin to exclude *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [cd3475f](https://github.com/CleanroomMC/Cleanroom/commit/cd3475ff42ccce858b3aa2afcbcd5a68e6b9c20c)]*
- Forgot to genPatch... *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [95adb40](https://github.com/CleanroomMC/Cleanroom/commit/95adb40c485f3eae7a97d1a4b23c4e910b4bad15)]*
- Fix bugs in new transformer. Bump to 3019 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3551099](https://github.com/CleanroomMC/Cleanroom/commit/3551099b7a64a7c66e6d944bbb147421598f92c5)]*
- Lwjgl 3.3.4 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b854eea](https://github.com/CleanroomMC/Cleanroom/commit/b854eeaba7cb9d80dd1f75b0d7343d960086040f)]*
- Update to Java 21, fix a lot of bug in reflection redirect, fix CrashReport still using reflection, introduce more bugs *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f544d1f](https://github.com/CleanroomMC/Cleanroom/commit/f544d1fdc6ba9e7be5cfbd256e054eea93507e50)]*
- Add javax remapper and setField hack (now disabled) *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [32f6eee](https://github.com/CleanroomMC/Cleanroom/commit/32f6eeed5a36b749ca198bef3d672d932a1df226)]*
- Fix crash report details calling OpenGL on the server thread *[commit by [@Rongmario](https://github.com/Rongmario) in [f2ea2e2](https://github.com/CleanroomMC/Cleanroom/commit/f2ea2e2b037a5a3b08e7b58a3477d05e7e17cd7d)]*
- Add jakarta libs *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ee5d030](https://github.com/CleanroomMC/Cleanroom/commit/ee5d030aecbdbbaf9fcd66165df9a18affb00289)]*
- Fix Mixin launch before obf map init, fix Mixin uses refmap on dev *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [337a4a5](https://github.com/CleanroomMC/Cleanroom/commit/337a4a50f02b6d0890036f708728a048fc2b95de)]*
- FontRender failsafe *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [625485f](https://github.com/CleanroomMC/Cleanroom/commit/625485f17fc1fdaef988433a62e53d31f62e974c)]*
- A checkpoint. *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b82952b](https://github.com/CleanroomMC/Cleanroom/commit/b82952b5fa7f56769a299e04eeea222bb46f6adb)]*
- Should be the last fix for this *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2ff4367](https://github.com/CleanroomMC/Cleanroom/commit/2ff4367ee77941f3b8991e9e1bb950e225b7f101)]*
- More unsafe tricks *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9f70e5c](https://github.com/CleanroomMC/Cleanroom/commit/9f70e5c0d44c6793912b97242923865f92cafb58)]*
- Should have a way to trigger cinit *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [821cc5e](https://github.com/CleanroomMC/Cleanroom/commit/821cc5ee1a68073771704b2920e2735c751cb57e)]*
- Fix test. Why did we use jdk.internal.misc instead of sun.misc though *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [e4344e3](https://github.com/CleanroomMC/Cleanroom/commit/e4344e3b211fc5bcd51a0ae5603241b91085527b)]*
- Trigger <cinit> before unsafe *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [08ebc83](https://github.com/CleanroomMC/Cleanroom/commit/08ebc83e987e53287e81c38c4f49959166c6e4cc)]*
- Gradle8.4, librarianlib fix from Trousers *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [53538d2](https://github.com/CleanroomMC/Cleanroom/commit/53538d2319ab5ec285e5c0f341a6cf02641ad03e)]*
- 3009 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [b53f582](https://github.com/CleanroomMC/Cleanroom/commit/b53f5829d661c34ef840d0f744a8b61377a4634e)]*
- Fixed most of thing *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [df60bb4](https://github.com/CleanroomMC/Cleanroom/commit/df60bb4e6396677e75b9ef7c933437901ae1f34c)]*
- Should fix Object Holder *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [2654c43](https://github.com/CleanroomMC/Cleanroom/commit/2654c4370f19e7ad489803f4e85897bf602a9bd5)]*
- Fix setFailsafeFieldValue *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [31f0b6c](https://github.com/CleanroomMC/Cleanroom/commit/31f0b6cbaca6dab35a3e01651010beff75ceb756)]*
- Should fix enum. Worth a blog post or wiki page? *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [28f04d7](https://github.com/CleanroomMC/Cleanroom/commit/28f04d74eff41e5fce7a190bd2237c43bb987a1d)]*
- Mixinbooter from future *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8fd4cb4](https://github.com/CleanroomMC/Cleanroom/commit/8fd4cb4bb7d9ca98e0fb62fa95d7d55711cb4cad)]*
- WHAT? *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4ed7cec](https://github.com/CleanroomMC/Cleanroom/commit/4ed7cecc45e1e8968505c2a7f4bf143720d897ed)]*
- Just to suffer? *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [84d0952](https://github.com/CleanroomMC/Cleanroom/commit/84d09525e3ed18accec2ef4e82c249508b453f7a)]*
- Why are we still here *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [11f7a50](https://github.com/CleanroomMC/Cleanroom/commit/11f7a50b34490192baea463cbcac40fc01ebba22)]*
- Update Mixin again *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [85895bb](https://github.com/CleanroomMC/Cleanroom/commit/85895bb4a28e39fea71d389da93d33ecd8dd2bb1)]*
- Update mixin *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c0520ec](https://github.com/CleanroomMC/Cleanroom/commit/c0520ec8c7b3cf0c80ce651adae5fbba8a5c3bc2)]*
- Recompile cursed bouncepad with java 17 *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [922f2f9](https://github.com/CleanroomMC/Cleanroom/commit/922f2f979878a5c7f3bce76e8b315e9a7844d639)]*
- Revert "Java version problem?" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [41bdad2](https://github.com/CleanroomMC/Cleanroom/commit/41bdad29b0676479a8c2972d8debee9284cb781a)]*
- Java version problem? *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d554610](https://github.com/CleanroomMC/Cleanroom/commit/d554610cfd207e7ee8450771c59e897f2e39007c)]*
- Enum, it just works (Todd Face) *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [ca30a29](https://github.com/CleanroomMC/Cleanroom/commit/ca30a29394f64d11de8e094e26bbab8a98177ee1)]*
- FG6, update dependencies *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8b92ea6](https://github.com/CleanroomMC/Cleanroom/commit/8b92ea6a3b3eb5bbf7665fd7ab5b9613371ae6c4)]*
- Update Mixin, add hacky IEarly *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5e4cb05](https://github.com/CleanroomMC/Cleanroom/commit/5e4cb0582a5487fd45d2f5ecdffb17ee1578e9c1)]*
- Gradle 8.1.1 and etc *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [33db0ef](https://github.com/CleanroomMC/Cleanroom/commit/33db0ef2160bdf3d7d9c0a8f3475c00329bbf109)]*
- Some attempts to get dependency right *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [8dcce71](https://github.com/CleanroomMC/Cleanroom/commit/8dcce719f91c0b2d85bbe9188686fdbd6d6e7794)]*
- He's gonna take you back to the past *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [9383be0](https://github.com/CleanroomMC/Cleanroom/commit/9383be084cff00bba9481b6404b830c6404d28d5)]*
- Rebase *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4b9f728](https://github.com/CleanroomMC/Cleanroom/commit/4b9f72872cc421cd4a7eef7c841988e10ca27eee)]*
- Use custom lwjgl lib *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer), Korewa_Li, Rongmario in [#12](https://github.com/CleanroomMC/Cleanroom/pull/12)]*
- Update README *[commit by [@Rongmario](https://github.com/Rongmario) in [e2172d8](https://github.com/CleanroomMC/Cleanroom/commit/e2172d841b3d1fabe69a48a026a0a91cefc1e5cc)]*
- Roll back Chat wrapping logic. I fxxking hate TextComponent now *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [a8ae694](https://github.com/CleanroomMC/Cleanroom/commit/a8ae6941ff4799b9a55d89e7514832beba9a59fb)]*
- Revert "Update build.gradle" *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [4ed7cb2](https://github.com/CleanroomMC/Cleanroom/commit/4ed7cb2b1352bbf6cdc5f2077d09850c72fbdfed)]*
- Update build.gradle *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [4c2c039](https://github.com/CleanroomMC/Cleanroom/commit/4c2c039ca09dd8969fa59b942c3a2458d4f8da48)]*
- Add deprecated libraries, better way to check lib repo, small typo stuff *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [f739077](https://github.com/CleanroomMC/Cleanroom/commit/f73907770fdbf894d4051aa6199536c56882d1b0)]*
- Gracefully exit game when display close requested while in-world *[PR by [@Rongmario](https://github.com/Rongmario) in [#9](https://github.com/CleanroomMC/Cleanroom/pull/9)]*
- Update README.md *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [87137bb](https://github.com/CleanroomMC/Cleanroom/commit/87137bb118baa277dfa6899a991b406e3aa6eb67)]*
- Fix GuiUtilRenderComponents logic again *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c95bda1](https://github.com/CleanroomMC/Cleanroom/commit/c95bda1f4c57dd72e55f66e99e1a5bef6142c9aa)]*
- Replace § with \u00a7 for non-unicode environment *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [50d7897](https://github.com/CleanroomMC/Cleanroom/commit/50d789744af11fef9a585ae1d9254160506039cb)]*
- Rework GuiClientRenderComponents. Why Mojang wrote two sets of line *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [054fb3a](https://github.com/CleanroomMC/Cleanroom/commit/054fb3ab94a056e4f4cf42048b098f04d2aaf876)]*
- Rename local var to improve readibility *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [717f503](https://github.com/CleanroomMC/Cleanroom/commit/717f503eb1b77c9f7449478c212e8daa3ff6f038)]*
- Make lwjglx to fully simulate lwjgl3 key event, make inner calsses *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [5ba4392](https://github.com/CleanroomMC/Cleanroom/commit/5ba439227c274cce03a5cb653daa726f4d0441c3)]*
- Add well-tested line breaking algorithm to FontRenderer *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0c90043](https://github.com/CleanroomMC/Cleanroom/commit/0c900438646df44e69f7d2df6a484110d77cc175)]*
- Add IME wrapper to handle IME input *[PR by [@kappa-maintainer](https://github.com/kappa-maintainer) in [#7](https://github.com/CleanroomMC/Cleanroom/pull/7)]*
- Bump Bouncepad version up to fix natives issue *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [7d644c4](https://github.com/CleanroomMC/Cleanroom/commit/7d644c4c853c915f4bc5606d2706c0b1859f4630)]*
- Use ReflectionHackery to not cache their own field lookup *[commit by [@Rongmario](https://github.com/Rongmario) in [094c61c](https://github.com/CleanroomMC/Cleanroom/commit/094c61c6149eb78a3de6f66bcb99c18e03342a1c)]*
- ModClassVisitor with ASM9 *[commit by [@Rongmario](https://github.com/Rongmario) in [789aa12](https://github.com/CleanroomMC/Cleanroom/commit/789aa1264a90d1d42554ba2fb7e55a39e96f3cbc)]*
- Revert to the old way to get modifiers field *[commit by [@Rongmario](https://github.com/Rongmario) in [3f651e8](https://github.com/CleanroomMC/Cleanroom/commit/3f651e870e10d22bf0567e1c6c0bee97f982750b)]*
- Remove UnsafeHackery, useless as we access jdk.internal.misc.Unsafe *[commit by [@Rongmario](https://github.com/Rongmario) in [a124e84](https://github.com/CleanroomMC/Cleanroom/commit/a124e84c36bde82ec43baf6e99efe79f5f212d5a)]*
- Give compiler arguments + use idea-ext plugin to resolve them for IDEA *[commit by [@Rongmario](https://github.com/Rongmario) in [ac3bf7e](https://github.com/CleanroomMC/Cleanroom/commit/ac3bf7ee65d10a9196419eb4cbe171806e4d7cfe)]*
- Bouncepad 0.3 - stop using add opens/exports for launch *[commit by [@Rongmario](https://github.com/Rongmario) in [1f0029f](https://github.com/CleanroomMC/Cleanroom/commit/1f0029f4e037cf30a31284e1229f30536f16c591)]*
- Good bye FMLSecurityManager, long gone *[PR by [@KorewaLidesu](https://github.com/KorewaLidesu) in [#5](https://github.com/CleanroomMC/Cleanroom/pull/5)]*
- Walk the stack without retaining class references + one less map lookup *[commit by [@Rongmario](https://github.com/Rongmario) in [a50a68f](https://github.com/CleanroomMC/Cleanroom/commit/a50a68f85f74d34fdfb92da84d5b4bbf91a553ae)]*
- Obliterated FMLSecurityManager *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [f7fda0f](https://github.com/CleanroomMC/Cleanroom/commit/f7fda0f752a827b2608e8089de1ad39533af745f)]*
- GuiOverlayDebug will have LWJGL version show up *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [3a87fbf](https://github.com/CleanroomMC/Cleanroom/commit/3a87fbf1e53c38426b9679f44ae53e28e89cb856)]*
- Patches contain wrong stuff in it, so we add a new one in instead *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [4cf9b08](https://github.com/CleanroomMC/Cleanroom/commit/4cf9b0893236c043fb94ed2621bccb9a0e1e9343)]*
- Stop the libraries from getting comsumed by the Cleanroom, small code tweak, fix natives typo *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [d982eb8](https://github.com/CleanroomMC/Cleanroom/commit/d982eb88ac11bb275c11c3dac507a39f7d89c44e)]*
- Fixed error "Encountered problem runtime patching class" due to those don't have anything to patch *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [4396da1](https://github.com/CleanroomMC/Cleanroom/commit/4396da1d1344932fcd5b50a3a7b15c53a2e76eaa)]*
- Added preliminary hackery APIs for Reflection, Unsafe and Enums + Tests *[PR by [@Rongmario](https://github.com/Rongmario), Korewa_Li in [#3](https://github.com/CleanroomMC/Cleanroom/pull/3)]*
- Fixed srg mapping stage, change from lzma to tsrg, a bit of String edit *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [74682d1](https://github.com/CleanroomMC/Cleanroom/commit/74682d1f7e2f473002ba44c5f76f6670533fa7dd)]*
- Fixed natives order, use Apache Maven as main repo for LWJGL *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [7b8083d](https://github.com/CleanroomMC/Cleanroom/commit/7b8083d79a04c18063bec907097454ce126bc216)]*
- Added checker due to some natives not hosted on Mojang end *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [96d192e](https://github.com/CleanroomMC/Cleanroom/commit/96d192e1e8629356b726aaafb2b08ff1d838cd43)]*
- LWJGL3 Native now packed properly this time! *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [91dedbc](https://github.com/CleanroomMC/Cleanroom/commit/91dedbc0dc387ba9afbb598570dff6598cd29236)]*
- LWJGL3 Native now packed in launch json when installing *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [9209c0c](https://github.com/CleanroomMC/Cleanroom/commit/9209c0c457994ec8e91ae389fb208d3b128c6b8f)]*
- Fixed class patching issue *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [f93d552](https://github.com/CleanroomMC/Cleanroom/commit/f93d5522805cfa9c9aa6fdecb9527ef417818d8b)]*
- Aid patch for Profile Installer + small fix that may require someday *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [3012b17](https://github.com/CleanroomMC/Cleanroom/commit/3012b17b94e879139b5d9f36a263885a762803de)]*
- Revert "Try updating installer package" *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [1dd5035](https://github.com/CleanroomMC/Cleanroom/commit/1dd503557b0a2e2f77a7bddf05364486868ea804)]*
- Try updating installer package *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c755a03](https://github.com/CleanroomMC/Cleanroom/commit/c755a03203764f144b4e214596f1614d2c02c08a)]*
- Github Action - BuildTest.yml: Remove unnecessary steps *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [0271329](https://github.com/CleanroomMC/Cleanroom/commit/02713293bb64e45a8d2af1783e2530c92eba91ce)]*
- Fix issue with checksum mismatch, fix issue with Forge dependency *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [e64f9d2](https://github.com/CleanroomMC/Cleanroom/commit/e64f9d2cda11d612d9a4b561cf37e61fd5f2b9ec)]*
- Hope this will fix the crash issue *[commit by [@KorewaLidesu](https://github.com/KorewaLidesu) in [f3a6e64](https://github.com/CleanroomMC/Cleanroom/commit/f3a6e643c16872a8f4aba3ae5ae4ef9ba3f2ea94)]*
- Fixed patch not applied correctly *[commit by Li in [477e38f](https://github.com/CleanroomMC/Cleanroom/commit/477e38f26d740b44c03decffbc4fa2451b686ac1)]*
- Updated Github Action *[commit by Li in [97a7679](https://github.com/CleanroomMC/Cleanroom/commit/97a767904bac2778bf7e43c5495261a833ab94fc)]*
- Update BuildTest.yml *[commit by Li in [31df2c3](https://github.com/CleanroomMC/Cleanroom/commit/31df2c3d0caeeb6b389ebf7c6fd99741139498e8)]*
- Fix typo (again) *[commit by Li in [11ff48f](https://github.com/CleanroomMC/Cleanroom/commit/11ff48fe42b10434d696e7fe3705dfc7fa785ac4)]*
- Fix typo *[commit by Li in [6576d90](https://github.com/CleanroomMC/Cleanroom/commit/6576d901a5d08c3cef78b49e2886e544aff2d5a6)]*
- Github Action for build Test Installer *[commit by Li in [85368e8](https://github.com/CleanroomMC/Cleanroom/commit/85368e83da117a6ea0300ca9966c78c7d5699707)]*
- Load coremods + AT, does not fix forge resources loading yet *[commit by [@Rongmario](https://github.com/Rongmario) in [0b44c90](https://github.com/CleanroomMC/Cleanroom/commit/0b44c908778a1bc942c158574084f80923ffc87a)]*
- Run tests with add opens jvm args as well *[commit by [@Rongmario](https://github.com/Rongmario) in [f269485](https://github.com/CleanroomMC/Cleanroom/commit/f2694856a7ff9a28e251bf2886b8ed45f7664740)]*
- Some config optimization from Desoroxxx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [3b1ee17](https://github.com/CleanroomMC/Cleanroom/commit/3b1ee17f6f8b7ead77f66e89a72d7d54ff9c0994)]*
- Move lwjglx to src *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [c007208](https://github.com/CleanroomMC/Cleanroom/commit/c007208941b5f2b6cd54dc04eb251ce53f957212)]*
- Add universalJar to installerJar's dependsOn *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [d12d489](https://github.com/CleanroomMC/Cleanroom/commit/d12d489ce5ece10d7d8e65c43075e7e9505d1fd7)]*
- New patches added, Fix lwjgl transformer *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [0373c63](https://github.com/CleanroomMC/Cleanroom/commit/0373c63937d103e95bbdcbe086597b86e739821a)]*
- Comment realms to prevent crash, add lwjgl to F3 Overlay *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [4a63409](https://github.com/CleanroomMC/Cleanroom/commit/4a634098e94a4b48e7006feaeb030db50ede27ca)]*
- Build instruction update to account for tests failing *[commit by [@Rongmario](https://github.com/Rongmario) in [f86118b](https://github.com/CleanroomMC/Cleanroom/commit/f86118b7ea9282d3163bb1343de22d447179daae)]*
- Switched to lwjglx *[commit by [@kappa-maintainer](https://github.com/kappa-maintainer) in [f80a664](https://github.com/CleanroomMC/Cleanroom/commit/f80a66430f7f7aed92ad032ac599a88bf2637ab7)]*
- Try finding artifacts in Mojang's maven, our maven, then Forge's maven *[commit by [@Rongmario](https://github.com/Rongmario) in [637b320](https://github.com/CleanroomMC/Cleanroom/commit/637b3207911c32e0b8d2e85db61f75040655bd6f)]*
- Reformat patch prefixes for legibility purposes *[commit by [@Rongmario](https://github.com/Rongmario) in [565a29d](https://github.com/CleanroomMC/Cleanroom/commit/565a29ddb12c4b0cba3a91e0275d1f3f5e8da48a)]*
- Read non-legacy binpatches *[commit by [@Rongmario](https://github.com/Rongmario) in [8799c71](https://github.com/CleanroomMC/Cleanroom/commit/8799c71e2f9282061c002a5a63c33308c3da7431)]*
- Fixed crashes when building installer jar *[commit by [@Rongmario](https://github.com/Rongmario) in [a45a686](https://github.com/CleanroomMC/Cleanroom/commit/a45a68662793250c3d988ef47ad9d1311e8fc03c)]*
- Retrieve classpaths properly in test's classloader *[commit by [@Rongmario](https://github.com/Rongmario) in [22343fd](https://github.com/CleanroomMC/Cleanroom/commit/22343fd72fc85af825ae65e5a47a2211b1085317)]*
- Missed one *[commit by [@Rongmario](https://github.com/Rongmario) in [3b97b50](https://github.com/CleanroomMC/Cleanroom/commit/3b97b5045d5493723b7251a31a0006a34f283320)]*
- Enabled tests and corrected their syntax to stable-39 mcp mappings *[commit by [@Rongmario](https://github.com/Rongmario) in [62b6485](https://github.com/CleanroomMC/Cleanroom/commit/62b6485548e1c6c29fd91e772e3953b6cc6b3919)]*
- Temp fix EnumHelper reflection hackery *[commit by [@Rongmario](https://github.com/Rongmario) in [0d133ae](https://github.com/CleanroomMC/Cleanroom/commit/0d133aeac143f50ac1e8e1ed45ed7c5b0062fac2)]*
- Temp fix modifiers field failed retrieval: part 2 *[commit by [@Rongmario](https://github.com/Rongmario) in [8160b26](https://github.com/CleanroomMC/Cleanroom/commit/8160b263da496f8e66adf441f99c780b96e83d73)]*
- Use getObjectType for internal names rather than getType *[commit by [@Rongmario](https://github.com/Rongmario) in [96e0a3c](https://github.com/CleanroomMC/Cleanroom/commit/96e0a3c5c67197efaac78a13560b2668a0413240)]*
- Open java.lang, java.lang.reflect modules *[commit by [@Rongmario](https://github.com/Rongmario) in [7ac68e2](https://github.com/CleanroomMC/Cleanroom/commit/7ac68e253b90e0cc07655d76d595fd4ee9d6c735)]*
- Temp fix modifiers field failed retrieval *[commit by [@Rongmario](https://github.com/Rongmario) in [2aba36f](https://github.com/CleanroomMC/Cleanroom/commit/2aba36fae7997028e1e0009f0e8079191875a464)]*
- Read method as-is after splitting *[commit by [@Rongmario](https://github.com/Rongmario) in [6dab805](https://github.com/CleanroomMC/Cleanroom/commit/6dab80598ffc17630e3f547c2d60bc8c379db83a)]*
- Open jdk.internal.loader module *[commit by [@Rongmario](https://github.com/Rongmario) in [d423ebe](https://github.com/CleanroomMC/Cleanroom/commit/d423ebe28e574663ba1c20571aeb7faf1d3f9f9e)]*
- Bouncepad as main class to launch *[commit by [@Rongmario](https://github.com/Rongmario) in [33da0f4](https://github.com/CleanroomMC/Cleanroom/commit/33da0f49ae2c5741e82331c599dafaf5e7475fc7)]*
- Include LegacyDev logging.properties *[commit by [@Rongmario](https://github.com/Rongmario) in [90baa46](https://github.com/CleanroomMC/Cleanroom/commit/90baa46c238faa2d4197a61fa12225e6a629eb03)]*
- Don't reset major spec tag from version string *[commit by [@Rongmario](https://github.com/Rongmario) in [a4a42b0](https://github.com/CleanroomMC/Cleanroom/commit/a4a42b0f3e9f2dca3670aee6133015af0605607a)]*
- Fix pure gradle bullshit *[commit by [@Rongmario](https://github.com/Rongmario) in [f9119e7](https://github.com/CleanroomMC/Cleanroom/commit/f9119e73b2992e83cf26d188b6f329442106e59a)]*
- Gradle 7.4 => Gradle 7.6 *[commit by [@Rongmario](https://github.com/Rongmario) in [d32bccd](https://github.com/CleanroomMC/Cleanroom/commit/d32bccd31bc1b36f8688e3355624038c0288aee5)]*
- Forge source+tests+resources *[commit by [@Rongmario](https://github.com/Rongmario) in [9eac361](https://github.com/CleanroomMC/Cleanroom/commit/9eac3613978d0b19aa92596c736a647ef2508c44)]*
- Missed out test client task *[commit by [@Rongmario](https://github.com/Rongmario) in [2de7621](https://github.com/CleanroomMC/Cleanroom/commit/2de762112f51442180698dd04d8b022fa9dcb454)]*
- Exclude intellij logs folder *[commit by [@Rongmario](https://github.com/Rongmario) in [ae58af4](https://github.com/CleanroomMC/Cleanroom/commit/ae58af413032ec0da72d96f593c2857371cbf991)]*
- Added server and test run tasks + forge version properties *[commit by [@Rongmario](https://github.com/Rongmario) in [43bdca3](https://github.com/CleanroomMC/Cleanroom/commit/43bdca3395a84089f2c2f6aa001be9c918b4d896)]*
- Preliminary README *[commit by [@Rongmario](https://github.com/Rongmario) in [2d8c9a4](https://github.com/CleanroomMC/Cleanroom/commit/2d8c9a4f3dc594e7f36dd8873061ca1207219bf2)]*
- Add existing Forge patches *[commit by [@Rongmario](https://github.com/Rongmario) in [16435d6](https://github.com/CleanroomMC/Cleanroom/commit/16435d6a016b50d74fe15f509f7817d0ed0228eb)]*
- Remove ASM from main classpath as it exists in buildSrc's already *[commit by [@Rongmario](https://github.com/Rongmario) in [7abb7be](https://github.com/CleanroomMC/Cleanroom/commit/7abb7bef73a9860b53aaeafd5964f9b30d578762)]*
- Userdev + buildSrc *[commit by [@Rongmario](https://github.com/Rongmario) in [74fffd6](https://github.com/CleanroomMC/Cleanroom/commit/74fffd643d9b43ff96f4d05e4e8755188aacb17c)]*
- Fix task typo *[commit by [@Rongmario](https://github.com/Rongmario) in [8eda604](https://github.com/CleanroomMC/Cleanroom/commit/8eda604f9b3d94bb5164f4e9e75758a7597953ee)]*
- Give setup task a group for easier identification *[commit by [@Rongmario](https://github.com/Rongmario) in [ad26851](https://github.com/CleanroomMC/Cleanroom/commit/ad26851d6812f7c3123e4864b86340b28b64544e)]*
- Add ASM to buildscript's classpath *[commit by [@Rongmario](https://github.com/Rongmario) in [faaa325](https://github.com/CleanroomMC/Cleanroom/commit/faaa325bae331db06d24030d148c4d9bd3b6aa9c)]*
- Basic buildscript *[commit by [@Rongmario](https://github.com/Rongmario) in [344a305](https://github.com/CleanroomMC/Cleanroom/commit/344a3053e6cfb4e5ca2379ad94266b57cb159738)]*
- Initial commit *[commit by [@Rongmario](https://github.com/Rongmario) in [f45360e](https://github.com/CleanroomMC/Cleanroom/commit/f45360e5e02bb8a407f0d7831b41d6f902769085)]*

### First-time Contributors

- **[@friendlyhj](https://github.com/friendlyhj) made their first contribution!**
- **[@VulpesStella](https://github.com/VulpesStella) made their first contribution!**
- **[@KorewaLidesu](https://github.com/KorewaLidesu) made their first contribution!**
- **[@KasumiNova](https://github.com/KasumiNova) made their first contribution!**
- **[@ZZZank](https://github.com/ZZZank) made their first contribution!**
- **[@Ecdcaeb](https://github.com/Ecdcaeb) made their first contribution!**
- **[@LasmGratel](https://github.com/LasmGratel) made their first contribution!**
- **[@Nessiesson](https://github.com/Nessiesson) made their first contribution!**
- **[@PrototypeTrousers](https://github.com/PrototypeTrousers) made their first contribution!**


