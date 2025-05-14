package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author ZZZank
 */
public class AbnormalListeners {

    public static boolean nonVoid = false;
    public static boolean isPrivate = false;

    static class Parent {

        @SubscribeEvent
        public static void listenerParentOnly(ExampleEvent event) {
            throw new IllegalStateException("listener method only present in super class should not be registered");
        }

        @SubscribeEvent
        public static void listenerParentAndSub(ExampleEvent event) {
            throw new IllegalStateException(
                "listener method only registered in super class should not be registered in sub class");
        }
    }

    public static class Actual extends Parent {

        public static void listenerParentAndSub(ExampleEvent event) {
            throw new IllegalStateException(
                "listener method only registered in super class should not be registered in sub class");
        }

        /// really?
        @SubscribeEvent
        public static Object nonVoidReturn(ExampleEvent event) {
            nonVoid = true;
            return null;
        }

        @SubscribeEvent
        private static void privateListener(ExampleEvent event) {
            isPrivate = true;
            return null;
        }
    }
}
