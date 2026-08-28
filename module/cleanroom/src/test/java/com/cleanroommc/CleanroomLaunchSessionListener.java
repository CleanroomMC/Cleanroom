package com.cleanroommc;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;

public final class CleanroomLaunchSessionListener implements LauncherSessionListener {

    private Thread sessionThread;
    private ClassLoader originalClassLoader;

    @Override
    public void launcherSessionOpened(LauncherSession session)
    {
        sessionThread = Thread.currentThread();
        originalClassLoader = sessionThread.getContextClassLoader();

        ClassLoader parent = originalClassLoader;

        if (parent == null)
        {
            parent = CleanroomLaunchSessionListener.class.getClassLoader();
        }

        sessionThread.setContextClassLoader(
                new RoutingClassLoader(parent)
        );
    }

    @Override
    public void launcherSessionClosed(LauncherSession session)
    {
        if (sessionThread != null)
        {
            sessionThread.setContextClassLoader(originalClassLoader);
        }
    }

    private static final class RoutingClassLoader extends ClassLoader
    {
        private static final URL[] CLASS_PATH = getClassPathURLs();

        private final ConcurrentHashMap<String, ResettingClassLoader>
                classLoaders = new ConcurrentHashMap<>();

        RoutingClassLoader(ClassLoader parent)
        {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException
        {
            Class<?> original = getParent().loadClass(name);

            String testClassName = findForgeTestClass(original);

            if (testClassName == null)
            {
                return original;
            }

            ResettingClassLoader classLoader =
                    classLoaders.computeIfAbsent(
                            testClassName,
                            this::createClassLoader
                    );

            return classLoader.load(name, resolve);
        }

        private ResettingClassLoader createClassLoader(String testClassName)
        {
            return new ResettingClassLoader(
                    CLASS_PATH,
                    getParent(),
                    testClassName
            );
        }

        private static String findForgeTestClass(Class<?> clazz)
        {
            if (clazz.isAnnotationPresent(CleanroomTest.class))
            {
                return clazz.getName();
            }

            Class<?> enclosingClass = clazz.getEnclosingClass();

            while (enclosingClass != null)
            {
                if (enclosingClass.isAnnotationPresent(CleanroomTest.class))
                {
                    return enclosingClass.getName();
                }

                enclosingClass = enclosingClass.getEnclosingClass();
            }

            return null;
        }
    }

    private static final class ResettingClassLoader extends URLClassLoader
    {
        private static final String[] QUARANTINED = new String[]{
                "com.cleanroommc"
        };

        private final String testClassName;

        ResettingClassLoader(
                URL[] urls,
                ClassLoader parent,
                String testClassName
        )
        {
            super(urls, parent);
            this.testClassName = testClassName;
        }

        Class<?> load(String name, boolean resolve)
                throws ClassNotFoundException
        {
            return loadClass(name, resolve);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException
        {
            if (!isQuarantined(name))
            {
                return super.loadClass(name, resolve);
            }

            synchronized (getClassLoadingLock(name))
            {
                Class<?> loaded = findLoadedClass(name);

                if (loaded == null)
                {
                    loaded = findClass(name);
                }

                if (resolve)
                {
                    resolveClass(loaded);
                }

                return loaded;
            }
        }

        private boolean isQuarantined(String name)
        {
            for (String prefix : QUARANTINED)
                if (name.startsWith(prefix))
                    return true;
            return name.startsWith(testClassName);
        }
    }

    private static URL[] getClassPathURLs()
    {
        String[] paths =
                System.getProperty("java.class.path")
                        .split(File.pathSeparator);

        URL[] urls = new URL[paths.length];

        try
        {
            for (int i = 0; i < paths.length; i++)
            {
                urls[i] = new File(paths[i]).toURI().toURL();
            }
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }

        return urls;
    }

}
