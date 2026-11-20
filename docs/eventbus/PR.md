# EventBus 7 integration and transformer-free legacy dispatch

## Changes

- Read legacy event annotations and override structure at runtime. Ordinary registrations resolve the canonical list directly. Hand-written `setup` and `getListenerList` chains retain virtual dispatch.
- Keep the three Forge scopes, custom buses and synchronous dispatch. Each post walks its canonical array, including phase markers and edits to pending slots. Registry mutations publish a fresh array for later and nested posts.
- Remove the event-class transformer from FML and CleanMix. The current base had already removed the subscriber transformer. Run both historical transformers only in the compatibility fixture.
- Ship Forge EventBus 7.0.6 as an external runtime dependency. `NativeEventChannel` uses upstream typed buses and exact tokens. `LegacyEventChannel` registers on the original Forge scope. `SubscriptionGroup` owns registration cleanup and diagnostic source information.
- Restore active owner and log context on exception and nested delivery. Keep the Guava signature of `ModContainer.registerBus`. Route FML's master and per-mod callback batches through Forge EventBus 7 with Guava subscriber selection, exception handling and FIFO reentrant delivery.

## Compatibility

Ordinary event registration skips constructor probes and supports abstract default events. Hand-written list hierarchies need complete `setup` and `getListenerList` pairs. A property annotation below an inherited hand-written method needs a local override. Review the per-scenario compatibility results and see the migration examples. [Compatibility](COMPATIBILITY.md)

## Verification

The original Java 25 delivery produced the installer, universal, userdev and MMC packages and recorded 180 passing tests with one Windows symlink skip. The FML backend follow-up passed 11 focused Java 25 tests with `fml.LogContext=true`. The client reached its main menu and the dedicated server reached `FMLServerStartedEvent`. In both new launches, the probe confirmed that the master and per-mod FML buses reached Forge EventBus 7.0.6. [Validation](VALIDATION.md) · [Dependency provenance](UPSTREAM.md)

## Performance

Against the unchanged Cleanroom runtime, three-fork JMH showed lower legacy posting time with 4, 16 and 64 listeners. Every candidate fork was faster than every baseline fork at those counts. The registration round trip fell from 825 to 97 µs and from 39.33 to 2.34 kB per operation. Plain event construction and the generic posting fixture remain slower. Forge 7 and the managed owner wrapper have separate measurements. At four native listeners, the fresh-post means were 30 ns upstream and 133 ns with owner handling. [Measurements and methodology](VALIDATION.md)

Rollback by reverting the change and restarting the JVM.
