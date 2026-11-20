package net.minecraftforge.fml.common;

import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.eventhandler.FMLThrowingEventBus;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FMLModLifecycleTest {
    @TempDir
    Path source;

    @Test
    void loadControllerAppliesSharedEventInOrderAndModContainerDispatchesExactClass() throws ReflectiveOperationException {
        Loader loader = Loader.instance();
        LoadController controller = new LoadController(loader);
        Field loaderController = field(Loader.class, "modController");
        Object previousController = loaderController.get(loader);
        ModContainer previousOwner = ModEventContext.owner();
        String previousLog = ThreadContext.get("mod");
        ModContainer publisher = ManagedEventBusLifecycleTest.owner("publisher");
        var trace = new ArrayList<String>();
        var event = new FMLDerivedEvent(trace);
        Thread postingThread = Thread.currentThread();
        var firstMod = new FixtureMod(event, 0, postingThread);
        var secondMod = new FixtureMod(event, 1, postingThread);
        FMLModContainer first = mod("first", firstMod);
        FMLModContainer second = mod("second", secondMod);
        var failure = new IllegalStateException("mod lifecycle callback");
        try (var firstBus = modBus(controller, first); var secondBus = modBus(controller, second)) {
            // Bypass discovery and startup, then exercise the real controller and container dispatch.
            controller.getActiveModList().addAll(List.of(first, second));
            field(LoadController.class, "eventChannels").set(controller, ImmutableMap.of(
                first.getModId(), firstBus, second.getModId(), secondBus));
            loaderController.set(loader, controller);
            loader.setActiveModContainer(publisher);
            ThreadContext.put("mod", "publisher log");

            controller.propogateStateMessage(event);
            assertEquals(List.of("apply first", "listen first", "apply second", "listen second"), trace);
            assertSame(second, event.appliedOwner);
            assertEquals(2, event.dispatches);
            assertContext(publisher, "publisher log");

            firstBus.post(new FMLBaseEvent(trace));
            assertEquals("listen base", trace.getLast());
            assertContext(publisher, "publisher log");

            firstMod.failure = failure;
            var uncaught = assertThrows(LoaderExceptionModCrash.class, () -> firstBus.post(event));
            assertSame(failure, uncaught.getCause());
            assertEquals(LoaderState.ModState.ERRORED, controller.getModState(first));
            assertContext(publisher, "publisher log");
        } finally {
            loaderController.set(loader, previousController);
            loader.setActiveModContainer(previousOwner);
            ModEventContext.restoreLogMod(previousLog);
        }
    }

    private FMLModContainer mod(String id, FixtureMod instance) throws ReflectiveOperationException {
        var candidate = new ModCandidate(source.toFile(), source.toFile());
        var container = new FMLModContainer(FixtureMod.class.getName(), candidate, Map.of("modid", id, "modLanguage", "java"));
        var metadata = new ModMetadata();
        metadata.modId = id;
        metadata.name = "Mod " + id;
        field(FMLModContainer.class, "modMetadata").set(container, metadata);
        var gatherAnnotations = FMLModContainer.class.getDeclaredMethod("gatherAnnotations", Class.class);
        gatherAnnotations.setAccessible(true);
        gatherAnnotations.invoke(container, FixtureMod.class);
        field(FMLModContainer.class, "modInstance").set(container, instance);
        return container;
    }

    private static FMLThrowingEventBus modBus(LoadController controller, ModContainer container) {
        var bus = FMLThrowingEventBus.forLifecycle((failure, context) -> controller.errorOccurred(container, failure), container);
        assertTrue(container.registerBus(bus, controller));
        return bus;
    }

    private static Field field(Class<?> declaringType, String name) throws NoSuchFieldException {
        var field = declaringType.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void assertContext(ModContainer owner, String log) {
        assertSame(owner, ModEventContext.owner());
        assertSame(owner, Loader.instance().activeModContainer());
        assertEquals(log, ThreadContext.get("mod"));
    }

    public static class FMLBaseEvent extends FMLEvent {
        final List<String> trace;
        ModContainer appliedOwner;
        int dispatches;

        FMLBaseEvent(List<String> trace) {
            this.trace = trace;
        }

        @Override
        public void applyModContainer(ModContainer mod) {
            assertSame(mod, ModEventContext.owner());
            assertSame(mod, Loader.instance().activeModContainer());
            appliedOwner = mod;
            trace.add("apply " + mod.getModId());
        }
    }

    public static final class FMLDerivedEvent extends FMLBaseEvent {
        FMLDerivedEvent(List<String> trace) {
            super(trace);
        }
    }

    public static final class FixtureMod {
        private final FMLDerivedEvent expected;
        private final int previousDispatches;
        private final Thread postingThread;
        private RuntimeException failure;

        FixtureMod(FMLDerivedEvent expected, int previousDispatches, Thread postingThread) {
            this.expected = expected;
            this.previousDispatches = previousDispatches;
            this.postingThread = postingThread;
        }

        @Mod.EventHandler
        public void receiveBase(FMLBaseEvent event) {
            event.trace.add("listen base");
        }

        @Mod.EventHandler
        public void receiveDerived(FMLDerivedEvent event) {
            if (failure != null) throw failure;
            assertSame(expected, event);
            assertSame(postingThread, Thread.currentThread());
            assertEquals(previousDispatches, event.dispatches++);
            assertContext(event.appliedOwner, event.appliedOwner.getModId());
            event.trace.add("listen " + event.appliedOwner.getModId());
        }
    }
}
