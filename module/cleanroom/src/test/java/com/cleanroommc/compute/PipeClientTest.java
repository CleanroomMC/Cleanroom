package com.cleanroommc.compute;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.kernels.params.KernelParameterList;
import com.cleanroommc.compute.pipes.Pipe;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ForgeTestRunner.Isolated
@ExtendWith(GLTestExtension.class)
@ExtendWith(OpenCLClientTest.class)
public class PipeClientTest extends PipeTestCases {

    @Test
    public void pipeOpenGLReadbackTest() {
        Kernel producer = pipeProgram().kernel("produce");
        Kernel consumer = pipeProgram().kernel("consume");
        GLBuffer storage = new GLBuffer();
        try {
            GL46.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, storage.bufferID);
            GL46.glBufferStorage(GL46.GL_SHADER_STORAGE_BUFFER, 8L * Integer.BYTES, GL46.GL_DYNAMIC_STORAGE_BIT);
            GL46.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, 0);
            assertEquals(GL46.GL_NO_ERROR, GL46.glGetError());
            GL46.glFinish();
            try (MemoryStack stack = MemoryStack.stackPush();
                 Pipe pipe = new Pipe(8, Integer.BYTES);
                 Buffer output = new Buffer(storage, BufferFlags.READ_WRITE);
                 Buffer status = new Buffer(stack, 16 * Integer.BYTES, BufferFlags.READ_WRITE)) {
                KernelParameterList write = new KernelParameterList(producer);
                write.add(pipe);
                write.add(8);
                write.add(status);
                KernelParameterList read = new KernelParameterList(consumer);
                read.add(pipe);
                read.add(8);
                read.add(output);
                read.add(status);
                IntBuffer statuses = stack.mallocInt(16);
                IntBuffer result = stack.mallocInt(8);
                queue.dispatchKernel(stack, producer, write, null, new long[]{1})
                        .next(consumer, read, null, new long[]{1})
                        .read(status, statuses).execute();
                GL46.glGetNamedBufferSubData(storage.bufferID, 0, result);
                assertEquals(GL46.GL_NO_ERROR, GL46.glGetError());
                for (int i = 0; i < 16; i++)
                    assertEquals(0, statuses.get(i));
                for (int i = 0; i < 8; i++)
                    assertEquals(i * 17 - 3, result.get(i));
            }
        } finally {
            GLResourceManager.disposeEarly(storage);
        }
    }
}
