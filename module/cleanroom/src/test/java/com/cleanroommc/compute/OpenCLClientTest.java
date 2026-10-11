package com.cleanroommc.compute;

import com.cleanroommc.compute.smrtptr.GarbageCollector;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.function.Executable;
import org.lwjgl.system.Configuration;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class OpenCLClientTest implements BeforeAllCallback, AfterAllCallback, InvocationInterceptor {

    private static void onGL(Executable action) throws Exception {
        GLTestExtension.assumeInitialized();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        GLTestExtension.submit(() -> {
            try {
                GLTestExtension.assumeGL46();
                action.execute();
            } catch (Throwable e) {
                failure.set(e);
            }
        }).join();
        if (failure.get() instanceof Error error)
            throw error;
        if (failure.get() instanceof Exception exception)
            throw exception;
        if (failure.get() != null)
            throw new RuntimeException(failure.get());
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        onGL(() -> {
            Loader.instance();
            Bootstrap.register();
            Configuration.OPENCL_EXPLICIT_INIT.set(true);
            ComputeSetup.initOpenCL(LogManager.getLogger("TestLogger"), true);
            assumeTrue(Compute.isAvailable(), "OpenCL/OpenGL sharing is unavailable.");
            Loader.instance().setupTestHarness(new DummyModContainer(new ModMetadata()
            {{
                modId = "cleanroom";
            }}));
        });
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (Compute.isAvailable() && GLTestExtension.isInitialized())
            onGL(() -> GarbageCollector.INSTANCE.wash());
    }

    @Override
    public void interceptTestMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        onGL(invocation::proceed);
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        onGL(invocation::proceed);
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        onGL(invocation::proceed);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        onGL(invocation::proceed);
    }

    @Override
    public void interceptAfterAllMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        onGL(invocation::proceed);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        onGL(invocation::proceed);
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        AtomicReference<T> result = new AtomicReference<>();
        onGL(() -> result.set(invocation.proceed()));
        return result.get();
    }

    @Override
    public void interceptDynamicTest(Invocation<@Nullable Void> invocation, DynamicTestInvocationContext invocationContext, ExtensionContext extensionContext) throws Throwable {
        onGL(invocation::proceed);
    }
}
