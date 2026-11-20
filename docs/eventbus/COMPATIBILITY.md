# Legacy and native compatibility

`EXACT` requires an observed trace from both the baseline and candidate. `DEFINED_CHANGE` and `UNSUPPORTED` describe separately tested behavior and structural limits. `BLOCKED` records any scenario without the required evidence.

The 45 cases contain 29 `EXACT`, 13 `DEFINED_CHANGE` and 3 `UNSUPPORTED` primary results. Mixed rows also name any separately tested behavior change.

Behavior references are the two-transformer Cleanroom revision `7695d38a08f398f8732f3a40d56b580cb6a2d610` and current Cleanroom `f3e4f5fe2a9c07da35bd8ae09bc6937a3f638dc0`. The latter uses the subscriber scanner from #618, with the event-class transformer still present. The #617 checkout is a comparison for regressions, separate from the unchanged build and performance baseline.

## Legacy behavior

| ID | Status | Evidence or migration |
| --- | --- | --- |
| M01 | EXACT | Both implementations inherit `Cancelable` and `HasResult`. The real instance controls cancellation and the post result. |
| M02 | EXACT | Real `RenderGameOverlayEvent.Pre`, `Post` and `ConfigChangedEvent.OnConfigChanged` give matching results. |
| M03 | EXACT | Dynamic property getters are called on the posted instance and keep their short-circuit order. |
| M04 | EXACT | The two hand-written `super` chains preserve call counts and inherited defaults. |
| M05 | EXACT | A hand-written method takes precedence over the same class's annotation. |
| M06 | DEFINED_CHANGE | Default registration skips construction. Concurrent resolution publishes one canonical array source for the type. |
| M07 | EXACT | A concrete child inherits abstract-parent annotations. Direct registration of an abstract event with a default list is a separately tested `DEFINED_CHANGE`. |
| M08 | UNSUPPORTED | A property annotation below an inherited hand-written method requires a local override. The diagnostic names the class and property. |
| M09 | UNSUPPORTED | Arbitrary helper `super` calls and reflection over injected members fall outside the default property contract. Use an explicit override or call the public getter virtually. |
| L01 | EXACT | Continuous hand-written list hierarchies use independent tables with the same inherited listener order and unregister behavior. |
| L02 | UNSUPPORTED | A missing `setup` or getter layer breaks a hand-written list chain. Declare both methods at each level. |
| L03 | EXACT | Instance getters select the post table. A null or throwing post getter fails outside the dispatch handler. Invalid registration getters now give a class-specific `DEFINED_CHANGE` diagnostic. |
| L04 | EXACT | Public `register`, `unregister` and `unregisterAll` invalidate subsequent snapshots. |
| D01 | EXACT | Phase, priority, child-before-parent and registration order match the historical trace. |
| D02 | EXACT | A receiving listener can clear cancellation and allow later listeners to run. |
| D03 | EXACT | The historical `Event.class` subscriber trace matches the candidate. Programmatic `LegacyEventChannel<Event>` also supports the same filter and recovery semantics. |
| D04 | EXACT | Generic filtering compares the same `Type` object after the cancellation filter. |
| D05 | EXACT | Empty and shut-down cases keep their distinct return behavior. Dispatch passes through unchecked failures and wraps checked failures. |
| D06 | EXACT | Nested post sees a fresh array after register/unregister. The outer post finishes on the array it already holds. |
| D07 | EXACT | Mutating a later slot changes the current post. A null slot reports its array index and original exception. |
| D08 | EXACT | Phase or callback exceptions report the bus, event, original array, index and cause. |
| D09 | EXACT | Equal legacy registration keys still deduplicate and unregister one another. Use stable hashes while registered. |
| D10 | DEFINED_CHANGE | Concurrent bus IDs, table publication, capacity and mutations are serialized and tested. |
| S01 | EXACT | Static registration uses the class's declared methods. Instance registration preserves declared and inherited instance subscriptions. |
| S02 | EXACT | The inherited annotation configures the subscriber. Virtual invocation still reaches the overriding implementation. |
| S03 | EXACT | Different-package package-private ancestor registration selects the same child implementation as the historical publicization. This corrects a difference introduced by #618. |
| S04 | DEFINED_CHANGE | Public, protected and package-private callbacks work. Legacy registration logs and skips private methods, matching the #618 scanner behavior. Managed legacy registration rejects them at registration. |
| S05 | EXACT | Interface default, diamond and non-void callbacks work. Covariant bridge deduplication is a `DEFINED_CHANGE` compared with the duplicate historical invocation and keeps the #618 behavior. |
| S06 | EXACT | The reflective legacy registration signature accepts an `Object` callback associated with an explicit event class. |
| S07 | DEFINED_CHANGE | Public callbacks across loader boundaries use a legal adapted handle when the lookup cannot define an LMF class. The candidate test verifies invocation with the original event and receiver. |
| S08 | DEFINED_CHANGE | The legal access path also supports an exported public subscriber in an unopened named module. A supplied `Lookup` can grant additional module access. |
| S09 | EXACT | Separate instances bind separate receivers even when they reuse a method factory. |

Ordinary event registration no longer needs a public no-arg constructor. For a hand-written event, make a public no-arg constructor available to the old registration path or pass the actual `ListenerList` using `withListenerList`. An abstract hand-written event needs the explicit list. The class still needs a continuous complete hand-written hierarchy.

The registry lock serializes legacy list publication and mutation. Dispatch uses the original array. Registering while a post is in progress can affect a subsequent or nested post. Other threads must coordinate any writes they make directly to an exposed array. The framework continues to hold the list tree for the life of its classloader. Closing subscriptions releases their callback references after already-running posts release their snapshots.

## Managed owner and Forge 7 APIs

Managed subscriptions capture the owner at group creation and restore prior owner and logging state after nested dispatch. Closing a group removes its subscriptions on their own buses. The owner of a private `BusGroup` controls disposal. Direct upstream registration and posting use the upstream context contract.

FML supplies a Guava `EventBus` to `ModContainer.registerBus`. The master and per-mod buses created by `LoadController` use Forge 7 to execute callbacks. Guava supplies the `@Subscribe` registry, type matching, per-subscriber synchronization and exception context. Each FML bus drains its own thread-local queue in posting order. A queued delivery keeps the selected Forge route alive until the delivery runs or an escaped exception discards it.

The registry bridge shares a runtime package with the pinned Guava 33.6.0 library under Foundation. Forge 7 runs in the game loader. Ordinary Guava buses and buses made with the public `FMLThrowingEventBus` constructors use Guava's dispatcher.

```java
static final LegacyEventChannel<ModEvent> LEGACY = LegacyEventChannel.of(ModEvent.class);
static final NativeEventChannel<Notice> NOTICES = NativeEventChannel.record(BusGroup.DEFAULT, Notice.class);

record Notice(int count) implements RecordEvent {}

// Store the group in the mod or component that owns these registrations.
SubscriptionGroup owned = new SubscriptionGroup();
Subscription legacy = LEGACY.subscribe(owned, "example:legacy", this::onLegacy);
Subscription nativeListener = NOTICES.subscribe(owned, Priority.NORMAL, "example:notice", this::onNotice);
NOTICES.post(new Notice(4));
legacy.close();
owned.close();
```

Use `LegacyEventChannel.of(bus, EventType.class)` for a custom or alternate Forge scope. `withGenericType(selector)` limits the registered listener to matching `IGenericEvent#getGenericType()` identities. New native cancellation is expressed with `subscribeCancelling`, `subscribeAlwaysCancelling` and `monitor`. Use a stable channel instance per event and scope. `registerLegacy` and `registerNative` accept a `MethodHandles.Lookup` and the subscriber instance or static subscriber class. A native annotation batch validates its methods before registration and removes installed tokens if a later installation fails. Concurrent posts may observe tokens while a batch is being installed or rolled back.

If a mod's build exposes loader runtime libraries separately from its compile classpath, add `net.minecraftforge:eventbus:7.0.6` as `compileOnly` from Forge Maven. Cleanroom supplies that same JAR at runtime.

| ID | Status | Evidence |
| --- | --- | --- |
| C01 | DEFINED_CHANGE | Nested managed legacy, native and Guava dispatch restores the publisher owner, including when a nested exception propagates. |
| C02 | DEFINED_CHANGE | The prior `mod` log key survives nesting and thrown exceptions. FML lifecycle logging uses mod IDs and legacy/native listener logging uses names when `fml.LogContext` is enabled. |
| C03 | EXACT | Synchronous `IContextSetter` and shared FML lifecycle events run in order with the active owner visible through `Loader`. The historical setter trace matches. |
| C04 | EXACT | Both runtimes execute `FMLEventChannel.fireRead` with real Netty channels and write the listener's exact reply before returning on the posting thread. |
| G01 | EXACT | The Forge-backed and ordinary Guava traces agree on parent type matching, FIFO reentrancy and the subscriber snapshot already queued when registrations change. |
| G02 | EXACT | Both backends produce the same `DeadEvent` and exception context, serialize ordinary subscribers, honor `AllowConcurrentEvents`, and discard queued posts on an escaped error. |
| G03 | EXACT | `FMLModContainer` dispatches the exact lifecycle type and reports the original cause through `FMLThrowingEventBus`. |
| N01 | DEFINED_CHANGE | The artifact and managed tests remove the exact upstream tokens. Repeated close and duplicate callback registration preserve independent subscriptions. |
| N02 | DEFINED_CHANGE | Explicit native scopes isolate the same event type. Closing a subscription group leaves other owners and `BusGroup.DEFAULT` usable. |
| N03 | DEFINED_CHANGE | Programmatic legacy subscriptions share the scoped old dispatch and preserve event identity, filtering and uncancellation. |
| N04 | DEFINED_CHANGE | Actual upstream record, mutable event, conditional cancellation, always-cancelling listener and monitoring contracts are exercised. |
| I01 | DEFINED_CHANGE | Both launch probes verify the event transformer classes are absent. Both JVM class load logs show Forge 7.0.6 loaded from the dependency JAR. |
| I02 | DEFINED_CHANGE | The development client reached the main menu and the dedicated server reached `FMLServerStartedEvent`. Both executed automatic, instance, legacy facade and native subscriptions through the real game loader. |
