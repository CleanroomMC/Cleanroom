package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author ZZZank
 */
public class NonPublicListeners {

    @SubscribeEvent
    private static void privateStatic(ExampleEvent event) {
        throw new AssertionError("private (static) method should not be registered");
    }

    @SubscribeEvent
    static void packagedStatic(ExampleEvent event) {
        event.sink.add("packaged static");
    }

    @SubscribeEvent
    protected static void protectedStatic(ExampleEvent event) {
        event.sink.add("protected static");
    }

    @SubscribeEvent
    private void privateInstance(ExampleEvent event) {
        throw new AssertionError("private method should not be registered");
    }

    @SubscribeEvent
    void packagedInstance(ExampleEvent event) {
        event.sink.add("packaged");
    }

    @SubscribeEvent
    protected void protectedInstance(ExampleEvent event) {
        throw new AssertionError("impl by subclass");
    }

    public static class OverrideWithNoSub extends NonPublicListeners {

        @Override
        protected void protectedInstance(ExampleEvent event) {
            event.sink.add("protected (subclass)");
        }
    }
}
