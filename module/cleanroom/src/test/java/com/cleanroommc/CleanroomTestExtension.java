package com.cleanroommc;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class CleanroomTestExtension implements BeforeAllCallback, AfterAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(CleanroomTestExtension.class);

    private static final String CONTEXT_CLASS_LOADER =
            "contextClassLoader";

    @Override
    public void beforeAll(ExtensionContext context)
    {
        Thread thread = Thread.currentThread();

        context.getStore(NAMESPACE).put(
                CONTEXT_CLASS_LOADER,
                thread.getContextClassLoader()
        );

        System.setProperty(
                "forge.disableVanillaGameData",
                "false"
        );

        ClassLoader testClassLoader =
                context.getRequiredTestClass().getClassLoader();

        ClassLoader parent = testClassLoader.getParent();

        if (parent != null)
        {
            thread.setContextClassLoader(parent);
        }
    }

    @Override
    public void afterAll(ExtensionContext context)
    {
        ClassLoader previous =
                context.getStore(NAMESPACE).get(
                        CONTEXT_CLASS_LOADER,
                        ClassLoader.class
                );

        if (previous != null)
        {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }
}
