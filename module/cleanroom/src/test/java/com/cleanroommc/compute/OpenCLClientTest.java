package com.cleanroommc.compute;

import com.cleanroommc.compute.smrtptr.GarbageCollector;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.*;
import org.lwjgl.system.Configuration;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class OpenCLClientTest implements BeforeAllCallback, AfterAllCallback, InvocationInterceptor {
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();
            try {
                Loader.instance();
                Bootstrap.register();
                Logger testLogger = LogManager.getLogger("TestLogger");
                Configuration.OPENCL_EXPLICIT_INIT.set(true);
                ComputeSetup.initOpenCL(testLogger, true);
                Loader.instance().setupTestHarness(new DummyModContainer(new ModMetadata()
                {{
                    modId = "accelerate";
                }}));
            } catch (Throwable _) {
            }
        });
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        GarbageCollector.INSTANCE.wash();
    }

    @Override
    public void interceptTestMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (!Compute.isAvailable()) {
            invocation.skip();
            return;
        }
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();
            try {
                invocation.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (!Compute.isAvailable()) {
            invocation.skip();
            return;
        }
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();
            try {
                invocation.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (!Compute.isAvailable()) {
            invocation.skip();
            return;
        }
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();
            try {
                invocation.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (!Compute.isAvailable()) {
            invocation.skip();
            return null;
        }
        final AtomicReference<T> res = new AtomicReference<>();
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();
            try {
                res.set(invocation.proceed());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        return res.get();
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (!Compute.isAvailable()) {
            invocation.skip();
            return;
        }
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();
            try {
                invocation.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void interceptDynamicTest(Invocation<@Nullable Void> invocation, DynamicTestInvocationContext invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (!Compute.isAvailable()) {
            invocation.skip();
            return;
        }
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();
            try {
                invocation.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void interceptAfterEachMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (!Compute.isAvailable()) {
            invocation.skip();
            return;
        }
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();
            try {
                invocation.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void interceptAfterAllMethod(Invocation<@Nullable Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (!Compute.isAvailable()) {
            invocation.skip();
            return;
        }
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();
            try {
                invocation.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }
}
