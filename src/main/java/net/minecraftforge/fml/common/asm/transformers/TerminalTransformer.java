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

package net.minecraftforge.fml.common.asm.transformers;

import org.objectweb.asm.*;

import net.minecraft.launchwrapper.IClassTransformer;

public class TerminalTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        return basicClass;
    }

    public static class ExitVisitor extends ClassVisitor
    {
        private String clsName = null;
        private boolean dirty;
        private static final String callbackOwner = org.objectweb.asm.Type.getInternalName(ExitVisitor.class);

        private ExitVisitor(ClassVisitor cv)
        {
            super(Opcodes.ASM9, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
        {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int mAccess, final String mName, final String mDesc, String mSignature, String[] mExceptions)
        {
            return null;
        }

        // Intercept System.exit, and check if the caller is allowed to use it, if not wrap it in a ExitTrappedException
        public static void systemExitCalled(int status)
        {
        }
        // Intercept Runtime.getRuntime().exit, and check if the caller is allowed to use it, if not wrap it in a ExitTrappedException
        public static void runtimeExitCalled(Runtime runtime, int status)
        {
        }

        // Intercept Runtime.getRuntime().halt, and check if the caller is allowed to use it, if not wrap it in a ExitTrappedException
        public static void runtimeHaltCalled(Runtime runtime, int status)
        {
        }

        private static void checkAccess()
        {
        }
    }
}