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

import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.getMethodDescriptor;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.Event;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class EventSubscriptionTransformer implements IClassTransformer, Opcodes
{
    private static final Type LISTENER_LIST_TYPE = Type.getObjectType("net/minecraftforge/fml/common/eventhandler/ListenerList");
    private static final String LISTENER_LIST_INTERNAL_NAME = LISTENER_LIST_TYPE.getInternalName();
    private static final String LISTENER_LIST_DESC = LISTENER_LIST_TYPE.getDescriptor();
    private static final String LISTENER_LIST_METHOD_DESC = getMethodDescriptor(LISTENER_LIST_TYPE);
    private static final String VOID_METHOD_DESC = getMethodDescriptor(VOID_TYPE);
    private static final String BOOLEAN_METHOD_DESC = getMethodDescriptor(BOOLEAN_TYPE);
    private static final String LISTENER_LIST_CTR_DESC = getMethodDescriptor(VOID_TYPE, LISTENER_LIST_TYPE);
    private static final String CANCELABLE_ANNOTATION_DESC = "Lnet/minecraftforge/fml/common/eventhandler/Cancelable;";
    private static final String HAS_RESULT_ANNOTATION_DESC = "Lnet/minecraftforge/fml/common/eventhandler/Event$HasResult;";

    public EventSubscriptionTransformer()
    {
        new Event(); // make sure the base event class loaded and initialized.
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        if (bytes == null || name.equals("net.minecraftforge.fml.common.eventhandler.Event") || name.startsWith("net.minecraft.") || name.indexOf('.') == -1)
        {
            return bytes;
        }
        // ClassReader's constructor only indexes the constant pool, just enough for getSuperName()
        ClassReader cr = new ClassReader(bytes);
        String superName = cr.getSuperName();
        if (superName == null)
        {
            return bytes;
        }

        try
        {
            // Yes, this recursively loads classes until we get this base class. THIS IS NOT A ISSUE. Coremods should handle re-entry just fine.
            // If they do not this a COREMOD issue NOT a Forge/LaunchWrapper issue.
            Class<?> parent = this.getClass().getClassLoader().loadClass(superName.replace('/', '.'));
            if (!Event.class.isAssignableFrom(parent))
            {
                return bytes;
            }
        }
        catch (ClassNotFoundException ex)
        {
            // Discard silently- it's just noise
            return bytes;
        }

        try
        {
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            EventClassVisitor visitor = new EventClassVisitor(cw, superName);
            cr.accept(visitor, 0);
            return visitor.isEdited() ? cw.toByteArray() : bytes;
        }
        catch (Exception e)
        {
            FMLLog.log.error("Error building events.", e);
        }

        return bytes;
    }
    
    private static final class EventClassVisitor extends ClassVisitor
    {
        private final String superName;

        private String className;
        private boolean edited;

        private boolean hasSetup;
        private boolean hasGetListenerList;
        private boolean hasDefaultCtr;
        private boolean hasCancelable;
        private boolean hasResult;
        private boolean cancelableAnnotation;
        private boolean hasResultAnnotation;

        EventClassVisitor(ClassVisitor classVisitor, String superName)
        {
            super(ASM9, classVisitor);
            this.superName = superName;
        }

        boolean isEdited()
        {
            return this.edited;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
        {
            this.className = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible)
        {
            if (visible)
            {
                if (HAS_RESULT_ANNOTATION_DESC.equals(descriptor)) this.hasResultAnnotation = true;
                else if (CANCELABLE_ANNOTATION_DESC.equals(descriptor)) this.cancelableAnnotation = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
        {
            if (name.equals("setup") && descriptor.equals(VOID_METHOD_DESC) && (access & ACC_PROTECTED) == ACC_PROTECTED) this.hasSetup = true;
            if ((access & ACC_PUBLIC) == ACC_PUBLIC)
            {
                if (name.equals("getListenerList") && descriptor.equals(LISTENER_LIST_METHOD_DESC)) this.hasGetListenerList = true;
                if (name.equals("isCancelable")    && descriptor.equals(BOOLEAN_METHOD_DESC))       this.hasCancelable = true;
                if (name.equals("hasResult")       && descriptor.equals(BOOLEAN_METHOD_DESC))       this.hasResult = true;
            }
            if (name.equals("<init>") && descriptor.equals(VOID_METHOD_DESC)) this.hasDefaultCtr = true;

            // Returning the writer's own MethodVisitor is what lets ASM copy this method verbatim.
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        @Override
        public void visitEnd()
        {
            if (this.hasResultAnnotation && !this.hasResult)
            {
                /* Add:
                 *      public boolean hasResult()
                 *      {
                 *            return true;
                 *      }
                 */
                this.addConstantTrueMethod("hasResult");
            }

            if (this.cancelableAnnotation && !this.hasCancelable)
            {
                /* Add:
                 *      public boolean isCancelable()
                 *      {
                 *            return true;
                 *      }
                 */
                this.addConstantTrueMethod("isCancelable");
            }

            if (this.hasSetup)
            {
                if (!this.hasGetListenerList)
                    throw new RuntimeException("Event class defines setup() but does not define getListenerList! " + this.className);

                super.visitEnd();
                return;
            }

            //Add private static ListenerList LISTENER_LIST
            super.visitField(ACC_PRIVATE | ACC_STATIC, "LISTENER_LIST", LISTENER_LIST_DESC, null, null).visitEnd();

            if (!this.hasDefaultCtr)
            {
                /*Add:
                 *      public <init>()
                 *      {
                 *              super();
                 *      }
                 */
                MethodVisitor mv = super.visitMethod(ACC_PUBLIC, "<init>", VOID_METHOD_DESC, null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, this.superName, "<init>", VOID_METHOD_DESC, false);
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            /*Add:
             *      protected void setup()
             *      {
             *              super.setup();
             *              if (LISTENER_LIST != NULL)
             *              {
             *                      return;
             *              }
             *              LISTENER_LIST = new ListenerList(super.getListenerList());
             *      }
             */
            MethodVisitor mv = super.visitMethod(ACC_PROTECTED, "setup", VOID_METHOD_DESC, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, this.superName, "setup", VOID_METHOD_DESC, false);
            mv.visitFieldInsn(GETSTATIC, this.className, "LISTENER_LIST", LISTENER_LIST_DESC);
            Label initListener = new Label();
            mv.visitJumpInsn(IFNULL, initListener);
            mv.visitInsn(RETURN);
            mv.visitLabel(initListener);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            mv.visitTypeInsn(NEW, LISTENER_LIST_INTERNAL_NAME);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, this.superName, "getListenerList", LISTENER_LIST_METHOD_DESC, false);
            mv.visitMethodInsn(INVOKESPECIAL, LISTENER_LIST_INTERNAL_NAME, "<init>", LISTENER_LIST_CTR_DESC, false);
            mv.visitFieldInsn(PUTSTATIC, this.className, "LISTENER_LIST", LISTENER_LIST_DESC);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            /*Add:
             *      public ListenerList getListenerList()
             *      {
             *              return this.LISTENER_LIST;
             *      }
             */
            mv = super.visitMethod(ACC_PUBLIC, "getListenerList", LISTENER_LIST_METHOD_DESC, null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, this.className, "LISTENER_LIST", LISTENER_LIST_DESC);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            this.edited = true;
            super.visitEnd();
        }

        private void addConstantTrueMethod(String name)
        {
            MethodVisitor mv = super.visitMethod(ACC_PUBLIC, name, BOOLEAN_METHOD_DESC, null, null);
            mv.visitCode();
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            this.edited = true;
        }
    }
}
