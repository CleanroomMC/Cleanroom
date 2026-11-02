package com.cleanroommc.compute;

import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.cmd.CommandQueue;
import com.cleanroommc.compute.images.*;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ForgeTestRunner.Isolated
@ExtendWith(GLTestExtension.class)
@ExtendWith(OpenCLClientTest.class)
public class ImageClientTest {

    private static CommandQueue queue;

    @BeforeAll
    public static void setup() {
        assumeTrue(Compute.isAvailable(), "OpenCL/OpenGL sharing is unavailable.");
        assumeTrue(Compute.instance().supportsImages, "Images are not supported.");
        queue = Compute.instance().queueDispatch.dispatch("image_test", true, false, false);
    }

    private static class SharedImage implements AutoCloseable {
        final GLTexture texture;
        final Image<Object> image;

        @SuppressWarnings("unchecked")
        SharedImage(GLTexture texture, Image<?> image) {
            this.texture = texture;
            this.image = (Image<Object>) image;
        }

        @Override
        public void close() {
            try {
                image.close();
            } finally {
                GLResourceManager.disposeEarly(texture);
            }
        }
    }

    SharedImage createImage(ImageTest.Shape shape) {
        GLTexture texture = switch (shape) {
            case IMAGE_1D -> GLTexture.newDsaTex1D(shape.width);
            case IMAGE_2D -> GLTexture.newDsaTex2D(shape.width, shape.height);
            case IMAGE_3D -> GLTexture.newDsaTex3D(shape.width, shape.height, shape.depth);
            case IMAGE_1D_BUFFER -> throw new IllegalArgumentException();
            case IMAGE_1D_ARRAY -> GLTexture.newDsaTex1DArray(shape.width, shape.height);
            case IMAGE_2D_ARRAY -> GLTexture.newDsaTex2DArray(shape.width, shape.height, shape.depth);
        };
        try {
            switch (shape) {
                case IMAGE_1D -> GL46.glTextureStorage1D(texture.textureID, 1, GL46.GL_RGBA32F, shape.width);
                case IMAGE_2D, IMAGE_1D_ARRAY -> GL46.glTextureStorage2D(texture.textureID, 1,
                    GL46.GL_RGBA32F, shape.width, shape.height);
                case IMAGE_3D, IMAGE_2D_ARRAY -> GL46.glTextureStorage3D(texture.textureID, 1,
                    GL46.GL_RGBA32F, shape.width, shape.height, shape.depth);
                case IMAGE_1D_BUFFER -> throw new IllegalArgumentException();
            }
            assertEquals(GL46.GL_NO_ERROR, GL46.glGetError());
            GL46.glFinish();
            Image<?> image = switch (shape) {
                case IMAGE_1D -> new Image1D(texture, BufferFlags.READ_WRITE);
                case IMAGE_2D -> new Image2D(texture, BufferFlags.READ_WRITE);
                case IMAGE_3D -> new Image3D(texture, BufferFlags.READ_WRITE);
                case IMAGE_1D_BUFFER -> throw new IllegalArgumentException();
                case IMAGE_1D_ARRAY -> new Image1DArray(texture, BufferFlags.READ_WRITE);
                case IMAGE_2D_ARRAY -> new Image2DArray(texture, BufferFlags.READ_WRITE);
            };
            return new SharedImage(texture, image);
        } catch (Throwable e) {
            GLResourceManager.disposeEarly(texture);
            throw e;
        }
    }

    @ParameterizedTest
    @EnumSource(value = ImageTest.Shape.class, names = "IMAGE_1D_BUFFER", mode = EnumSource.Mode.EXCLUDE)
    public void sharedImageCreationTest(ImageTest.Shape shape) {
        try (SharedImage resource = createImage(shape)) {
            assertNotNull(resource.image);
        }
    }
}
