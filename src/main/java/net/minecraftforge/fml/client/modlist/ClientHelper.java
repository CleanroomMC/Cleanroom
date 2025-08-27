package net.minecraftforge.fml.client.modlist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

/**
 * Author: MrCrayfish
 */
public class ClientHelper {
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

    public static void blit(int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        Gui.drawScaledCustomSizeModalRect(x, y, uOffset, vOffset, uWidth, vHeight, width, height, textureWidth, textureHeight);
    }

    public static void blit(int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight) {
        Gui.drawModalRectWithCustomSizedTexture(x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

}
