/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.common.registry;

import com.google.common.collect.Sets;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Uses {@code ResettingClassLoader} to load the test class. Minecraft and Forge
 * classes are loaded using the separate class loader.
 *
 * Use of a separate class loader allows classes to be reloaded for each test
 * class, which is handy when you're testing frameworks that make use of static
 * members.
 *
 * The selective quarantining is required because if the test class and its
 * 'children' are all loaded by a different class loader, then the {@code Test}
 * annotations yield different {@code Class} instances. JUnit then thinks there
 * are no runnable methods, because it looks them up by Class.
 *
 * This is a simplified copy of https://github.com/BinaryTweed/quarantining-test-runner
 * tailored for Minecraft use.
 *
 */
public class ForgeTestRunner extends Runner
{

    private final Runner inner;

    public ForgeTestRunner(Class<?> testFileClass) throws InitializationError
    {
        Class<? extends Runner> delegateRunningTo = JUnit4.class;

        String testFileClassName = testFileClass.getName();
        String delegateRunningToClassName = delegateRunningTo.getName();

        ResettingClassLoader classLoader = new ResettingClassLoader(testFileClassName, delegateRunningToClassName);

        try
        {
            Class<?> innerRunnerClass = classLoader.loadClass(delegateRunningToClassName);
            Class<?> testClass = classLoader.loadClass(testFileClassName);
            inner = (Runner) innerRunnerClass.getConstructor(Class.class)
                .newInstance(testClass);
        }
        catch (Exception e)
        {
            throw new InitializationError(e);
        }
    }


    @Override
    public Description getDescription()
    {
        return inner.getDescription();
    }

    @Override
    public void run(RunNotifier notifier)
    {
        System.setProperty("forge.disableVanillaGameData", "false");
        inner.run(notifier);
    }

    /**
     * If a class name starts with any of the supplied patterns, it is loaded by
     * <em>this</em> classloader; otherwise it is loaded by the parent classloader.
     */
    private static class ResettingClassLoader extends URLClassLoader
    {
        private final Set<String> quarantinedClassNames;

        /**
         * @param quarantinedClassNames prefixes to match against when deciding how to load a class
         */
        public ResettingClassLoader(String... quarantinedClassNames)
        {
            super(getClassPathURLs().toArray(new URL[0]));
            this.quarantinedClassNames = Sets.newHashSet();
            Collections.addAll(this.quarantinedClassNames, quarantinedClassNames);
            Collections.addAll(this.quarantinedClassNames, "net.minecraft", "net.minecraftforge");
        }


        /**
         * If a class name starts with any of the supplied patterns, it is loaded by
         * <em>this</em> classloader; otherwise it is loaded by the parent classloader.
         *
         * @param name class to load
         */
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException
        {
            boolean quarantine = false;

            for (String quarantinedPattern : quarantinedClassNames)
            {
                if (name.startsWith(quarantinedPattern))
                {
                    quarantine = true;
                    break;
                }
            }

            if (quarantine)
            {
                return findClass(name);
            }

            return super.loadClass(name);
        }

        private static List<URL> getClassPathURLs() {
            String[] paths = System.getProperty("java.class.path").split(File.pathSeparator);
            List<URL> urls = new ArrayList<>(paths.length);
            try {
                for (String classpath : paths) {
                    urls.add(new File(classpath).toURI().toURL());
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            return urls;
        }
    }
}
