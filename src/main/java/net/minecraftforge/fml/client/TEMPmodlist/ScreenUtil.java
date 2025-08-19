package net.minecraftforge.fml.client.TEMPmodlist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Author: MrCrayfish
 */
public class ScreenUtil {
    /**
     * Creates a scissor test using minecraft screen coordinates instead of pixel coordinates.
     * @param screenX
     * @param screenY
     * @param boxWidth
     * @param boxHeight
     */
    public static void scissor(int screenX, int screenY, int boxWidth, int boxHeight) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        int scale = scaledRes.getScaleFactor();

        int x = screenX * scale;
        int y = mc.displayHeight - (screenY * scale + boxHeight * scale);
        int width = Math.max(0, boxWidth * scale);
        int height = Math.max(0, boxHeight * scale);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, width, height);
    }

    public static boolean isMouseWithin(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static class Size2i {
        public final int width, height;

        public Size2i(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    /**
     * A backport of AbstractGUI.blit. Should not be overused.
     */
    public static void blit(int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        float minU = uOffset / textureWidth;
        float minV = vOffset / textureHeight;
        float maxU = (uOffset + uWidth) / textureWidth;
        float maxV = (vOffset + vHeight) / textureHeight;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0).tex(minU, maxV).endVertex();
        buffer.pos(x + width, y + height, 0).tex(maxU, maxV).endVertex();
        buffer.pos(x + width, y, 0).tex(maxU, minV).endVertex();
        buffer.pos(x, y, 0).tex(minU, minV).endVertex();
        tessellator.draw();
    }

    /**
     * A backport of AbstractGUI.blit. Should not be overused.
     */
    public static void blit(int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight) {
        blit(x, y, width, height, uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

}
