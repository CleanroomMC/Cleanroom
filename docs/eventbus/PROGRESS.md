# Event bus implementation progress

## P0

- Isolated `eventbus7` worktree from `f3e4f5fe2a9c07da35bd8ae09bc6937a3f638dc0`. The detached baseline uses the same commit and the pinned Kirino submodule.
- Current source locations are `module/forge` and `module/cleanroom`. The current checkout already omits `EventSubscriberTransformer` after #618. The differential fixture uses both historical transformers from `7695d38a08f398f8732f3a40d56b580cb6a2d610`. The current #617 head is `63408d0eb3bdafc4620909eccbda65f72ee7e6fe`.
- Verified the Forge Maven checksums for EventBus 7.0.6 binary and sources. The JAR targets classfile version 65 and requires jspecify 1.0.1.
- Gradle 9.7.0 and Temurin 25.0.5+8 configured. The detached baseline passed `setup` and all five original EventBus tests. The unmodified Forge artifact passed the Foundation record, mutable event and exact-token smoke test. See [UPSTREAM.md](UPSTREAM.md).

## P1–P3

- Pinned actual historical source blobs and replayed both transformers with an isolated loader. Compared concrete traces for inherited metadata, custom lists, priority, cancellation, generic selection, canonical arrays, exception boundaries, subscriber access and owner context.
- Corrected two differential fixture registration keys with mutable hashes. The corrected legacy/Foundation suite passed all 26 tests.
- Switched production event properties and list selection to `ClassValue` metadata. Posting walks the canonical array. Removed the remaining event transformer and its Mixin exclusion. Both game launch logs confirm the transformer classes are absent.

## P4–P5

- Implemented typed legacy/Forge 7 channels and owner-scoped subscription groups. Native registration uses the exact upstream listener token, including conditional cancellation and monitors.
- Connected FML's active mod accessors to the managed context. Per-mod lifecycle buses keep Guava and retire their tracked subscribers on disable. The project verified the native, nested owner and real Netty reply cases. The separate log-enabled run passed five tests.

## P6

- The first metadata-backed `Event` used 32 bytes versus 24 in the original. The final event uses a byte for the phase ordinal, a byte for default property flags, and the canonical list reference. JOL confirmed 24-byte instances in both the old and final builds.
- The historical JMH workload smoke passed with the baseline's event transformer applied to the fixture classes under Foundation.
- Gradle completed the Java 25 build with the verified Mojang native staging. Checkstyle, FormatJ and ClearSkies passed. The test tasks combined for 180 passed, one skipped for Windows symlink permissions and zero failed. The installer, universal, userdev and MMC artifacts are available.
- The final Java 25 development client reached its menu and the dedicated server reached `FMLServerStartedEvent`. Both runs passed the mod probe, loaded Forge EventBus 7.0.6 from its original JAR, loaded zero event transformers and exited successfully.
- Completed matched three-fork JMH for unchanged legacy and final legacy, plus separate Forge 7 and managed owner measurements. The candidate improved 4-, 16- and 64-listener legacy posting and the registration round trip in every fork. Generic posting and standalone construction remain slower than the baseline.
- The unchanged runtime passed the synchronous Netty reply trace. Its public subscriber in a child loader failed registration with `LambdaConversionException: Invalid caller`. The candidate's adapted handle passed and its dedicated JMH case recorded the fallback overhead.
- The reachability probe held both closed handles and their bus scopes live. The owner and both callback receivers were collected.
- The 45-case compatibility matrix, reproduction commands and PR draft are in this directory. The three patch layers are delivered in `../eventbus7-validation/patches`. Git committer identity was unavailable.

## FML lifecycle follow-up

- `LoadController` constructs Forge-backed master and per-mod buses with the existing Guava ABI. A same-package bridge supplies actual Guava subscribers to ordered Forge 7 routes.
- The 11 focused Java 25 tests passed with `fml.LogContext=true`, including differential queue and concurrent subscriber cases, lifecycle event reuse and exception identity. The client and dedicated server probes passed and saw both Forge dispatch frames in the real launcher.
- The lifecycle change is the fourth patch layer in `../eventbus7-validation/patches`.
