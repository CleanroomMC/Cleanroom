package net.minecraftforge.fml.common.eventhandler.differential;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import javax.annotation.Nonnull;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/** Runs the pinned event implementation with its original transformers and the current FML environment. */
final class HistoricalEventBusFixture extends URLClassLoader {
    private static final String REVISION = "7695d38a08f398f8732f3a40d56b580cb6a2d610";
    private static final String RESOURCES = "/eventbus-baseline/" + REVISION + "/";
    private static final String TRANSFORMERS = "net.minecraftforge.fml.common.asm.transformers.";
    private static final String EVENT_HANDLER = "net.minecraftforge.fml.common.eventhandler.";
    private static final String FIXTURES = EVENT_HANDLER + "differential.fixtures.";
    private static final String OVERLAY = "net.minecraftforge.client.event.RenderGameOverlayEvent";
    private static final String CONFIG = "net.minecraftforge.fml.client.event.ConfigChangedEvent";
    private static final String EVENT = EVENT_HANDLER + "Event";

    private List<IClassTransformer> transformers = List.of();

    HistoricalEventBusFixture(Path output) throws Exception {
        super(new URL[] {output.toUri().toURL()}, HistoricalEventBusFixture.class.getClassLoader());
        try {
            // FMLCorePlugin installed the subscription transformer before the subscriber transformer.
            transformers = List.of(transformer("EventSubscriptionTransformer"), transformer("EventSubscriberTransformer"));
        } catch (Exception | Error failure) {
            try {
                close();
            } catch (IOException closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
    }

    static void compile(Path output) throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("The historical EventBus test needs a JDK with jdk.compiler");
        }
        var digest = MessageDigest.getInstance("SHA-1");
        var sources = new ArrayList<JavaFileObject>();
        for (String entry : new String(resource(RESOURCES + "sources.list"), StandardCharsets.UTF_8).lines().toList()) {
            if (entry.isBlank() || entry.startsWith("#")) {
                continue;
            }
            String[] fields = entry.split(" ", 2);
            byte[] source = resource(RESOURCES + fields[1]);
            digest.update(("blob " + source.length + "\0").getBytes(StandardCharsets.US_ASCII));
            if (!fields[0].equals(HexFormat.of().formatHex(digest.digest(source)))) {
                throw new IOException("Historical source differs from its pinned Git blob: " + fields[1]);
            }
            sources.add(new SimpleJavaFileObject(URI.create("string:///" + fields[1]), JavaFileObject.Kind.SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return new String(source, StandardCharsets.UTF_8);
                }
            });
        }

        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (var files = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            // Gradle's test worker keeps the project classpath in a separate loader.
            Class<?>[] dependencies = {Loader.class, FMLLog.class, ScaledResolution.class, BossInfoClient.class,
                IClassTransformer.class, Preconditions.class, Logger.class, Nonnull.class, NonNull.class,
                ClassReader.class, ClassNode.class};
            var classpath = new ArrayList<Path>();
            for (Class<?> type : dependencies) {
                classpath.add(Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()));
            }
            files.setLocationFromPaths(StandardLocation.CLASS_PATH, classpath.stream().distinct().toList());
            files.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(output));
            if (!compiler.getTask(null, files, diagnostics, List.of("-proc:none"), null, sources).call()) {
                throw new AssertionError("Could not compile historical EventBus " + REVISION + "\n"
                    + String.join("\n", diagnostics.getDiagnostics().stream().map(Object::toString).toList()));
            }
        }
    }

    static Object run(ClassLoader loader, Class<?> scenarios, String scenario) throws Throwable {
        try {
            return Class.forName(scenarios.getName(), true, loader).getMethod(scenario).invoke(null);
        } catch (InvocationTargetException invocation) {
            throw invocation.getCause();
        }
    }

    static byte[] resource(String path) throws IOException {
        try (var stream = HistoricalEventBusFixture.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Missing test resource " + path);
            }
            return stream.readAllBytes();
        }
    }

    private IClassTransformer transformer(String simpleName) throws ReflectiveOperationException {
        return (IClassTransformer) loadClass(TRANSFORMERS + simpleName).getConstructor().newInstance();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        String classFile = name.replace('.', '/') + ".class";
        URL snapshot = findResource(classFile);
        boolean scenario = name.startsWith(FIXTURES);
        if (snapshot == null && !scenario) {
            return super.loadClass(name, resolve);
        }
        synchronized (getClassLoadingLock(name)) {
            Class<?> type = findLoadedClass(name);
            if (type == null) {
                byte[] bytes;
                try {
                    if (scenario) {
                        bytes = resource("/" + classFile);
                    } else {
                        try (var stream = snapshot.openStream()) {
                            bytes = stream.readAllBytes();
                        }
                    }
                } catch (IOException e) {
                    throw new ClassNotFoundException(name, e);
                }
                if (!name.equals(EVENT) && (scenario || name.startsWith(EVENT_HANDLER)
                    || name.startsWith(OVERLAY) || name.startsWith(CONFIG))) {
                    for (IClassTransformer transformer : transformers) {
                        bytes = transformer.transform(name, name, bytes);
                    }
                }
                type = defineClass(name, bytes, 0, bytes.length);
            }
            if (resolve) {
                resolveClass(type);
            }
            return type;
        }
    }
}
