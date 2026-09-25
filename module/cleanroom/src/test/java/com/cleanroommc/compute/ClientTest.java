package com.cleanroommc.compute;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.cmd.CommandQueue;
import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.kernels.params.KernelParameterList;
import com.cleanroommc.compute.programs.ComputeProgram;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ForgeTestRunner.Isolated
@ExtendWith(GLTestExtension.class)
@ExtendWith(OpenCLClientTest.class)
public class ClientTest {

    private static CommandQueue queue;
    private static ComputeProgram program;

    @BeforeAll
    public static void setup() {
        queue = Compute.instance().queueDispatch.dispatch("queue");
        Compute.instance().registerProgram(new ResourceLocation("cleanroom", "program"));
        Compute.instance().compilePrograms();
        program = Compute.instance().programs.get(new ResourceLocation("cleanroom", "program"));
    }

    @Test
    public void testSingleExecution() {
        final float[] values = new float[]{
            1.f,2.f,3.f,4.f,5.f,6.f,2.f,3.f,1.f,6.f,7.f
        };
        float[] results = new float[values.length];
        assertDoesNotThrow(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                GLBuffer glBuffer = new GLBuffer();
                GL20.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, glBuffer.bufferID);
                GL46.glBufferStorage(GL46.GL_SHADER_STORAGE_BUFFER, values, GL46.GL_DYNAMIC_STORAGE_BIT);
                Buffer buffer = new Buffer(glBuffer, BufferFlags.READ_WRITE);
                Kernel kernel = program.kernel("test");
                KernelParameterList paramList = new KernelParameterList(kernel);
                paramList.add(buffer);
                queue.dispatchKernel(kernel, paramList, null, new long[]{values.length})
                    .read(buffer, results).execute();
                buffer.close();
            }
        });
        for (int i = 0; i < values.length; i++)
            assertEquals(values[i]+1.f, results[i]);
    }

    @Test
    public void testConsequentExecution() {
        final float[] values = new float[]{
            1.f,2.f,3.f,4.f,5.f,6.f,2.f,3.f,1.f,6.f,7.f
        };
        float[] results = new float[values.length];
        assertDoesNotThrow(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                GLBuffer glBuffer = new GLBuffer();
                GL20.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, glBuffer.bufferID);
                GL46.glBufferStorage(GL46.GL_SHADER_STORAGE_BUFFER, values, GL46.GL_DYNAMIC_STORAGE_BIT);
                Buffer buffer = new Buffer(glBuffer, BufferFlags.READ_WRITE);
                Kernel kernel = program.kernel("test");
                KernelParameterList paramList = new KernelParameterList(kernel);
                paramList.add(buffer);
                queue.dispatchKernel(kernel, paramList, null, new long[]{values.length})
                    .next(kernel, paramList, null, new long[]{values.length})
                    .next(kernel, paramList, null, new long[]{values.length})
                    .read(buffer, results).execute();
                buffer.close();
            }
        });
        for (int i = 0; i < values.length; i++)
            assertEquals(values[i]+3.f, results[i]);
    }

    @Test
    public void testSingleExecutionStackless() {
        final float[] values = new float[]{
            1.f,2.f,3.f,4.f,5.f,6.f,2.f,3.f,1.f,6.f,7.f
        };
        GLBuffer buffer = new GLBuffer();
        GL20.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, buffer.bufferID);
        GL46.glBufferStorage(GL46.GL_SHADER_STORAGE_BUFFER, values, GL46.GL_DYNAMIC_STORAGE_BIT);
        try (Buffer clBuffer = new Buffer(buffer, BufferFlags.READ_WRITE)) {
            Kernel kernel = program.kernel("test");
            KernelParameterList paramList = new KernelParameterList(kernel);
            paramList.add(clBuffer);
            float[] results = new float[values.length];
            queue.dispatchKernel(kernel, paramList, null, new long[]{values.length})
                .read(clBuffer, results).execute();
            for (int i = 0; i < values.length; i++)
                assertEquals(values[i]+1.f, results[i]);
        }
    }

    @Test
    public void testConsequentExecutionStackless() {
        final float[] values = new float[]{
            1.f,2.f,3.f,4.f,5.f,6.f,2.f,3.f,1.f,6.f,7.f
        };
        float[] results = new float[values.length];
        assertDoesNotThrow(() -> {
            GLBuffer glBuffer = new GLBuffer();
            GL20.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, glBuffer.bufferID);
            GL46.glBufferStorage(GL46.GL_SHADER_STORAGE_BUFFER, values, GL46.GL_DYNAMIC_STORAGE_BIT);
            Buffer buffer = new Buffer(glBuffer, BufferFlags.READ_WRITE);
            Kernel kernel = program.kernel("test");
            KernelParameterList paramList = new KernelParameterList(kernel);
            paramList.add(buffer);
            queue.dispatchKernel(kernel, paramList, null, new long[]{values.length})
                .next(kernel, paramList, null, new long[]{values.length})
                .next(kernel, paramList, null, new long[]{values.length})
                .read(buffer, results).execute();
            buffer.close();
        });
        for (int i = 0; i < values.length; i++)
            assertEquals(values[i]+3.f, results[i]);
    }

    @Test
    public void testByteArguments2D() {
        final byte[] vals1 = new byte[] {
            1,2,3,2,4,5,1,3,2,6,7,1,8,9,10
        };
        final byte[] vals2 = new byte[] {
            3,2,1,4,5,0,1,4,3,2,1,0
        };
        byte[] results = new byte[vals1.length*vals2.length];
        assertDoesNotThrow(() -> {
            GLBuffer glBuffer = new GLBuffer();
            GL20.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, glBuffer.bufferID);
            GL46.glBufferStorage(GL46.GL_SHADER_STORAGE_BUFFER, vals1.length + vals2.length, GL46.GL_DYNAMIC_STORAGE_BIT);
            Buffer parent = new Buffer(glBuffer, BufferFlags.READ_WRITE);
            Buffer v1 = new Buffer(parent, 0, vals1.length, BufferFlags.READ_WRITE);
            Buffer v2 = new Buffer(parent, vals1.length, vals2.length, BufferFlags.READ_WRITE);
            Buffer output = new Buffer(results.length, BufferFlags.READ_WRITE);
            Kernel kernel = program.kernel("byteTest");
            KernelParameterList paramList = new KernelParameterList(kernel);
            paramList.add((long) vals1.length);
            paramList.add((long) vals2.length);
            paramList.add(v1);
            paramList.add(v2);
            paramList.add(output);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer bv1 = stack.bytes(vals1);
                ByteBuffer bv2 = stack.bytes(vals2);
                ByteBuffer out = stack.malloc(results.length);
                CommandQueue.Event wv1 = queue.bufferWrite(v1, 0, bv1);
                CommandQueue.Event wv2 = queue.bufferWrite(v2, 0, bv2);
                queue.dispatchKernel(kernel, paramList, null, new long[]{vals1.length, vals2.length}, wv1, wv2)
                    .read(output, out).execute();
                out.rewind();
                for (int i = 0; i < results.length; i++)
                    results[i] = out.get(i);
            }

            parent.close();
            output.close();
        });

        for (int x = 0; x < vals1.length; x++)
            for (int y = 0; y < vals2.length; y++)
                assertEquals(vals1[x]+vals2[y], results[(x*vals2.length)+y]);
    }
}
