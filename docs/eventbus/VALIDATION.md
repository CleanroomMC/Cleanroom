# EventBus performance protocol

The unchanged baseline is `f3e4f5fe2a9c07da35bd8ae09bc6937a3f638dc0`. Run it with its event transformer and the scanner merged in #618. Use the same Java 25 JVM, fork count, warmup, measurement settings and listener work on the candidate. Save the JMH JSON under build output.

- Key legacy measurements are steady synchronous posting with 0, 1, 4, 16 and 64 listeners, and inherited listeners. Measure cancellation recovery and generic selection separately.
- A repeatable legacy regression above 5% outside measurement noise triggers investigation. Sub-nanosecond differences need absolute measurements and uncertainty alongside percentages.
- A primary improvement must reproduce in class loading, cold registration, or retained/dispatched allocation before claiming a legacy speedup. Report native and native-with-owner-wrapper independently.
- Each callback contributes to a value consumed by the benchmark. Allocate fresh events when a phase marker may run. Report allocation separately from dispatch where the event contract permits reuse.

## Build and behavior verification

Windows 11 x64, Intel Core i5-9300H (4 cores, 8 threads), Temurin 25.0.5+8 and Gradle 9.7.0. All commands below ran in isolated worktrees with the pinned Kirino submodule initialized and Java 25 selected through `JAVA_HOME`.

| Checkout | Command | Observed result |
| --- | --- | --- |
| Unchanged HEAD | `gradlew.bat setup test --tests net.minecraftforge.fml.common.eventhandler.EventBusTest` | Build and five original EventBus cases passed |
| Unchanged HEAD | `pwsh -NoProfile -File ../eventbus7-validation/run-baseline-network.ps1` | The real original event transformer defined the packet event getter. The same Netty test observed the exact reply on the posting thread before `fireRead` returned. |
| Candidate | `gradlew.bat setup projects tasks --all` | Build setup applied all 541 Minecraft patches |
| Candidate | `gradlew.bat test --tests 'net.minecraftforge.fml.common.eventhandler.*' --tests '*NativeEventBusArtifactSmokeTest'` | 26 pass, including the actual two-transformer historical fixture and native test under Foundation |
| Candidate | `gradlew.bat test isolatedTest writeInstallProfile` | 178 pass in `test` and `isolatedTest`. Installer metadata initially stopped on TLS downloading the Mojang Linux text-to-speech native. |
| Candidate | `gradlew.bat dependencyInsight --dependency net.minecraftforge:eventbus --configuration distributionLibraries` | Resolves 7.0.6 in the distribution runtime variant |
| Candidate | `gradlew.bat runCleanroomClient --offline` | Probe passed in pre-init, 9 mods loaded, main menu marker, clean exit |
| Candidate | `gradlew.bat runCleanroomServer --offline` | Probe passed in pre-init, 9 mods loaded, `Done`, `FMLServerStartedEvent`, clean shutdown |
| Candidate | `gradlew.bat test --tests '*ManagedEventBusLifecycleTest' -PeventbusLogContext --offline -I ../eventbus7-validation/init-eventbus.gradle --no-configuration-cache` | Five pass with `fml.LogContext=true` in a fresh worker, including exact legacy batch removal and an abstract event's explicit hand-written list |
| Candidate, final behavior | `gradlew.bat build --offline --continue --no-configuration-cache -I ../eventbus7-validation/init-mojang-staging.gradle` | Gradle reported `BUILD SUCCESSFUL`. `test` and `isolatedTest` total 180 passed, 1 skipped and 0 failures. `CacheTest.symbolicLinksAreRejected()` skipped because the Windows account could not create symbolic links. Checkstyle, FormatJ and ClearSkies passed. The installer, universal, userdev and MMC artifacts built. |
| Candidate | `gradlew.bat writeInstallProfile --offline --no-configuration-cache -I ../eventbus7-validation/init-mojang-staging.gradle` | Generated installer metadata. `version.json` lists one external Forge EventBus 7.0.6 JAR with the expected download hash. |
| Candidate | `gradlew.bat generatePomFileForMavenPublication --offline --no-configuration-cache` | The generated loader POM lists `net.minecraftforge:eventbus:7.0.6` as a runtime dependency. The universal JAR contains `NativeEventChannel` and zero embedded Forge 7 or event transformer classes. |
| Candidate | `gradlew.bat assemble --offline --no-configuration-cache -I ../eventbus7-validation/init-mojang-staging.gradle` | Initial assembly produced universal, userdev and MMC outputs. The installer runtime was uncached. A subsequent online build ran `installerJar` and `assemble` successfully. |
| Candidate | `java -jar build/libs/<version>-installer.jar --help` | The assembled Java 25 installer displayed its client, server and MMC install modes and exited successfully. |
| Candidate | `gradlew.bat formatJavaCheck --offline` | Passed after formatting the new event code, tests and benchmark sources. |
| Candidate | `gradlew.bat compileBenchmarkJava clearSkiesCheck -x compileJava --offline --no-configuration-cache -I ../eventbus7-validation/init-jmh-staging.gradle` | Passed against the verified candidate classes and the staged official JMH dependencies. |

The full build wrote `BUILD SUCCESSFUL` just before the outer command deadline. The wrapper reported a timeout. A cached retry lost its Gradle daemon during Minecraft reobfuscation. The recorded build and test outcomes come from the completed Gradle tasks, build log and JUnit XML.

## Startup reproduction

From the local validation directory, compile and run the candidate probe. The launch hook uses the original EventBus JAR and captures the class-load logs inside each probe game directory.

```powershell
pwsh -NoProfile -File build-startup-probe.ps1
pwsh -NoProfile -File run-eventbus-startup.ps1
```

The matched startup comparison uses the same legacy-only probe mod in both worktrees. The JVM uptime is printed at the client menu or `FMLServerStartedEvent`. Both servers use loopback with a fresh flat world and the same seed and view distance on a new run directory.

```powershell
pwsh -NoProfile -File build-startup-probe.ps1 -Comparison
pwsh -NoProfile -File run-eventbus-startup.ps1 -Compare
```

| Readiness marker | Unchanged baseline | Candidate |
| --- | ---: | ---: |
| Client main menu | 268.452 s | 208.095 s |
| Dedicated `FMLServerStartedEvent` | 78.451 s | 51.180 s |

Each measurement comes from one development launch on a shared workstation with other verification activity. The original server reached its marker and began orderly shutdown before the outer comparison command timed out. The startup performance comparison remains inconclusive.

The launch commands additionally used `--no-configuration-cache -I ../eventbus7-validation/init-eventbus.gradle` to stage a small probe mod in separate games and turn on `-Xlog:class+load=info`. The dedicated test used a loopback-only temporary world and the operator's EULA acceptance. Both launchers defined the probe and EventBus 7 API under Foundation. The probe verified automatic package-private static subscription, protected instance subscription, legacy event cancellation and recovery, native posting and exact token close, and owner restoration. Both class-load logs identify the original Maven EventBus JAR and have no entries for either event transformer. These runs used the development entry points with production transformer configuration.

JOL 0.17 `internals` recorded a 24-byte `Event` and a 32-byte `ASMEventHandler` in both the baseline and the final build. The first metadata reference had grown `Event` to 32 bytes. Byte-sized phase and property fields restored the 24-byte layout. The workload GC profiler reports separately for event subclasses. `pwsh -NoProfile -File ../eventbus7-validation/run-subscription-reachability.ps1` kept both closed handles and both channel scopes live. After GC, the weak references to the owner and the native and legacy callback receivers were clear, including before a subsequent post. The list tree retains listener tables for the life of its defining loader. Classloader unloading and heap-wide retention remain unmeasured.

The Mojang staging hook uses the original text-to-speech native JARs matched by the cached Minecraft 1.12.2 manifest. During the first release build DNS prevented resolution of the installer and Checkstyle. Both recovered on the subsequent online build, which produced the installer and resolved the Checkstyle dependencies. Standalone JMH uses the official 1.37 JARs.

## JMH reproduction

The benchmarks live under `module/cleanroom/src/benchmark/java/com/cleanroommc/cleanroom/event/benchmark`. The baseline uses the same legacy workload sources with the unchanged runtime and its actual event transformer. The Foundation loader asserts the transformed fixture accessor on the baseline and the ordinary inherited accessor on the candidate. Native results include the bare artifact and Cleanroom's owner wrapper.

In each worktree, export its test and compile classpaths with `gradlew.bat eventbusExportClasspath --offline --no-configuration-cache -I ../eventbus7-validation/init-eventbus.gradle`. The local runner compiles the fixtures on Java 25, passes the classpath to JMH through a manifest JAR on Windows and writes JSON to `build/eventbus-jmh` in each worktree. Run the following from the validation directory, with the same JDK in both worktrees.

```powershell
pwsh -NoProfile -File run-eventbus-jmh.ps1 -Tree baseline
pwsh -NoProfile -File run-eventbus-jmh.ps1 -Tree candidate -ResultTag final
pwsh -NoProfile -File run-eventbus-jmh.ps1 -Tree candidate -Suite native
pwsh -NoProfile -File run-eventbus-jmh.ps1 -Tree baseline -Suite access -AccessLoader local
pwsh -NoProfile -File run-eventbus-jmh.ps1 -Tree candidate -Suite access
```

The posting runs use one thread, three forks, three 1-second warmup iterations and five 1-second measured iterations with the GC profiler. Each callback contributes to a value consumed by JMH. Phased legacy events are freshly allocated. The empty legacy and native reuse cases measure posting separately. Cold first-use measurements capture class loading or first registration once per fork. Registration and removal use batches of 16. Use `roundTrip` for paired allocation, since setup or teardown of the separate register and unregister cases can appear in their GC counters.

## Forge 7 native posting

Forge 7.0.6 uses its typed `EventBus` in both cases. The managed channel switches and restores the registered owner for each callback. Both cases use ordinary record listeners from the first candidate. The final edit to native registration affects the conditional-cancellation overload. The table shows JMH averages with the reported error in ns/op.

| Listeners | Upstream, fresh | Managed, fresh | Upstream, reused | Managed, reused |
| ---: | ---: | ---: | ---: | ---: |
| 0 | 12.190 ± 4.068 | 19.716 ± 5.917 | 17.965 ± 3.974 | 16.039 ± 5.037 |
| 1 | 24.730 ± 15.037 | 39.475 ± 14.847 | 14.801 ± 3.434 | 46.655 ± 14.513 |
| 4 | 30.261 ± 13.743 | 133.299 ± 47.202 | 34.381 ± 11.433 | 109.184 ± 36.760 |
| 16 | 79.597 ± 21.001 | 464.287 ± 194.057 | 68.931 ± 20.876 | 359.566 ± 90.791 |
| 64 | 319.313 ± 109.470 | 1842.700 ± 573.787 | 183.844 ± 61.911 | 2720.556 ± 1205.189 |

Fresh native posts with managed listeners allocated about 16 B/op. The upstream cases and the managed case with zero listeners measured under 0.01 B/op. Reused events measured at most 0.25 B/op across both paths. The managed callback cost is visible even with event reuse. Raw measurements are in `build/eventbus-jmh/candidate-native.json`.

## Legacy performance investigation

The first candidate measured 16.123 ± 7.010 ns to construct a plain event and 64.043 ± 19.319 ns to post the generic fixture. The baseline measured 5.777 ± 2.010 ns and 26.114 ± 4.821 ns respectively. A separate short JVM inlining run showed the generic predicate and the user callback inlined in both builds. The original default `isCancelable()` returned a constant. The candidate reads an instance flag derived from the event class and keeps the legacy virtual method call.

The revised implementation loads the canonical base list once from its event shape on the warm path. Generic filtering keeps its `Type` selector on `ASMEventHandler`, which evaluates it after cancellation and calls the subscriber directly. The first candidate's raw data remains in `build/eventbus-jmh/candidate-legacy-initial.json`.

## Legacy baseline and final candidate

The table pairs the unchanged `f3e4f5fe` runtime with the final candidate. Timing cells show the mean and JMH error in the indicated unit. Allocation is the GC profiler mean in bytes per operation.

| Workload | Timing unit | Baseline | Candidate | Bytes per op, baseline → candidate |
| --- | --- | ---: | ---: | ---: |
| Fresh post, 0 | ns/op | 13.367 ± 7.633 | 12.519 ± 5.713 | 0.000 → 0.000 |
| Fresh post, 1 | ns/op | 27.229 ± 8.453 | 33.673 ± 6.265 | 0.000 → 0.000 |
| Fresh post, 4 | ns/op | 195.884 ± 35.651 | 72.634 ± 25.744 | 32.001 → 0.001 |
| Fresh post, 16 | ns/op | 457.201 ± 108.924 | 120.784 ± 39.274 | 32.003 → 0.001 |
| Fresh post, 64 | ns/op | 1291.373 ± 517.285 | 466.744 ± 105.361 | 32.009 → 0.003 |
| Reused empty event | ns/op | 10.108 ± 5.501 | 12.057 ± 2.387 | 0.000 → 0.000 |
| Inherited listeners | ns/op | 91.688 ± 45.473 | 62.165 ± 17.384 | 32.001 → 0.000 |
| Cancel and restore | ns/op | 111.231 ± 40.235 | 119.852 ± 32.875 | 32.001 → 32.001 |
| Generic selection | ns/op | 26.114 ± 4.821 | 48.529 ± 8.290 | 0.000 → 0.000 |
| Owner context | ns/op | 141.089 ± 93.224 | 220.275 ± 36.070 | 32.001 → 32.002 |
| Construct a plain event | ns/op | 5.777 ± 2.010 | 14.430 ± 2.391 | 32.000 → 32.000 |
| Cold load and construct | ms/op | 1.854 ± 11.097 | 1.614 ± 4.784 | 26910690.7 → 25157061.3 |
| Cold registration | ms/op | 106.124 ± 716.976 | 120.264 ± 319.689 | 34762000.0 → 32471416.0 |
| Register one listener | µs/op | 1192.682 ± 628.491 | 75.177 ± 32.454 | 39325.5 → 2335.8 |
| Unregister one listener | µs/op | 20.111 ± 32.320 | 10.792 ± 5.960 | 39325.6 → 2336.1 |
| Register and unregister | µs/op | 824.702 ± 199.435 | 97.374 ± 63.116 | 39325.4 → 2336.1 |

Every candidate fork was faster than every baseline fork for fresh posting with 4, 16 and 64 listeners and the register/unregister round trip. Their mean times fell by 63%, 74%, 64% and 88% respectively. The round trip allocated about 2.34 kB per registration pair, down from 39.33 kB. In the fresh multi-listener fixtures the candidate JVM could eliminate the event allocation. The separate construction benchmark still reports 32 B/op for an escaping plain event.

Generic posting and plain event construction remained slower than the baseline in every fork. The generic fixture posts fresh events and includes the class-metadata and canonical-list lookup that class transformation avoids during construction. Both variants of the generic callback inlined during the short diagnostic run. The per-listener property check cost remains unquantified. The owner and one-listener means rose with overlapping measurement intervals. Cold-start estimates have wide intervals and the GC counters include setup.

Raw results are `build/eventbus-jmh/baseline-legacy.json` in the baseline worktree and `build/eventbus-jmh/candidate-legacy-final.json` in the candidate.

## Cross-loader legacy callback

The separate access fixture posts a fresh root `Event` to four public subscribers. It loads the same subscriber bytecode in the posting classloader or a child of it. Loading from the child exercises the candidate's adapted method handle, since a lookup crossing unnamed modules cannot define the lambda factory. The original child-loader case failed registration with `LambdaConversionException: Invalid caller`.

| Runtime and subscriber loader | ns/op | B/op |
| --- | ---: | ---: |
| Baseline, same loader | 216.361 ± 143.659 | 24.002 |
| Candidate, same loader | 62.458 ± 6.992 | <0.001 |
| Candidate, child loader | 197.125 ± 49.209 | 24.001 |

The report uses the candidate's same-loader case as the performance reference for its fallback. Both worktrees retain `build/eventbus-jmh/*-access.json`.

## Local evidence

- Build and test evidence is in `../eventbus7-validation/final-java25-build.log` and `build/test-results/{test,isolatedTest}`. The installer profile and generated loader POM are under `build/cleanroom_gradle/dist` and `build/publications/maven`.
- The final client and server logs are `../eventbus7-validation/final-launch-{client,server}.log`. The per-side game directories contain `eventbus-classload.log` with the Forge JAR origin.
- The original runtime, final candidate and Forge 7 measurement JSON files are in the corresponding worktree's `build/eventbus-jmh` directory. Matched startup, object layout and reachability output lives in `../eventbus7-validation`.

## FML lifecycle dispatch on Forge 7

The lifecycle follow-up uses real Guava subscriber snapshots on both sides of the differential. The candidate executes the selected callbacks through Forge 7. The 11 focused cases passed on Temurin 25 with `fml.LogContext=true`, including the original exception context, subscriber synchronization, reentrant registration changes, queue cleanup after an error, exact FML event-class matching and owner restoration.

```powershell
gradlew.bat test --tests '*ManagedGuavaEventBusTest' --tests '*ManagedEventBusLifecycleTest' --tests '*FMLModLifecycleTest' -PeventbusLogContext --offline --no-configuration-cache -I ../eventbus7-validation/init-eventbus.gradle
```

The Java 25 development client and dedicated server reached their readiness markers and exited normally. In each launcher, a real mod's `FMLPreInitializationEvent` callback observed two Forge EventBus 7.0.6 `post` frames, one for the master bus and one for its local bus. The probe also checked the active mod and the shared Guava runtime package. FormatJ, ClearSkies, license verification and the bridge's Checkstyle check passed.

```powershell
pwsh -NoProfile -File build-startup-probe.ps1 -Lifecycle
pwsh -NoProfile -File run-eventbus-startup.ps1 -Lifecycle
```

Run these probe commands from `../eventbus7-validation`. The new startup logs are `fml-forge7-launch-client.log` and `fml-forge7-launch-server.log`.

The earlier posting and startup timing tables predate this FML backend change.
