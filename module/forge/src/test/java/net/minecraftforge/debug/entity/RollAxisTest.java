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

package net.minecraftforge.debug.entity;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

/**
 * Tests the entity roll axis ({@link Entity#getRoll(float)}, {@link Entity#setRoll(float)}).
 * <ul>
 * <li>Client: holding the roll key (R) spins the render view entity; {@code CameraSetup} adds the
 * interpolated roll to the camera, so the view should rotate smoothly and cross &plusmn;180 without
 * spinning back. Pressing the key while sneaking logs {@code setRoll(540)} (expect -180) and
 * {@code setRoll(NaN)} (expect a warning and no change), then resets the roll to 0.</li>
 * <li>Server: cows within 32 blocks of a player are rolled every tick; even entity ids spin
 * continuously (seam crossing), odd ids follow a sine. Cows are drawn rolled on the client, which
 * checks S2C sync and tick-aligned interpolation on remote entities. A cow walking into view must not
 * animate from 0 to its current roll.</li>
 * </ul>
 */
@Mod(modid = RollAxisTest.MODID, name = "Roll Axis Test", version = "1.0", acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber(modid = RollAxisTest.MODID)
public class RollAxisTest
{
    static final String MODID = "rollaxistest";
    static final boolean ENABLED = false;
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    private static final double COW_RANGE = 32.0D;
    private static final float SPIN_PER_TICK = 12.0F;
    private static final float SINE_AMPLITUDE = 90.0F;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        if (ENABLED && event.getSide().isClient())
        {
            ClientEventHandler.registerKeyBinding();
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (!ENABLED || event.side != Side.SERVER || event.phase != TickEvent.Phase.END)
        {
            return;
        }
        World world = event.world;
        long time = world.getTotalWorldTime();
        Set<EntityCow> cows = new HashSet<>();
        for (EntityPlayer player : world.playerEntities)
        {
            cows.addAll(world.getEntitiesWithinAABB(EntityCow.class, player.getEntityBoundingBox().grow(COW_RANGE)));
        }
        for (EntityCow cow : cows)
        {
            if (cow.getEntityId() % 2 == 0)
            {
                cow.setRoll((time % 360L) * SPIN_PER_TICK);
            }
            else
            {
                cow.setRoll(SINE_AMPLITUDE * (float) Math.sin(time * 0.15D));
            }
        }
    }

    @Mod.EventBusSubscriber(value = Side.CLIENT, modid = MODID)
    public static class ClientEventHandler
    {
        private static final float KEY_ROLL_PER_TICK = 6.0F;
        private static KeyBinding rollKey;

        static void registerKeyBinding()
        {
            rollKey = new KeyBinding("key.rollaxistest.roll", Keyboard.KEY_R, "key.categories.rollaxistest");
            ClientRegistry.registerKeyBinding(rollKey);
        }

        // END phase: the entity tick has already snapshotted prevRoll, so the new value interpolates over the next frames
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event)
        {
            if (!ENABLED || rollKey == null || event.phase != TickEvent.Phase.END)
            {
                return;
            }
            Minecraft mc = Minecraft.getMinecraft();
            Entity view = mc.getRenderViewEntity();
            if (view == null || mc.isGamePaused())
            {
                return;
            }
            boolean sneaking = view.isSneaking();
            while (rollKey.isPressed())
            {
                if (sneaking)
                {
                    runSanityChecks(view);
                }
            }
            if (rollKey.isKeyDown() && !sneaking)
            {
                view.setRoll(view.getRoll() + KEY_ROLL_PER_TICK);
            }
        }

        private static void runSanityChecks(Entity view)
        {
            view.setRoll(540.0F);
            LOGGER.info("setRoll(540) read back as {} (expected -180.0)", view.getRoll());
            view.setRoll(Float.NaN);
            LOGGER.info("setRoll(NaN) read back as {} (expected -180.0 and a warning above)", view.getRoll());
            view.setRoll(0.0F);
            view.prevRoll = 0.0F;
        }

        @SubscribeEvent
        public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event)
        {
            if (ENABLED)
            {
                event.setRoll(event.getRoll() + event.getEntity().getRoll((float) event.getRenderPartialTicks()));
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onRenderLivingPre(RenderLivingEvent.Pre<?> event)
        {
            if (!ENABLED || !(event.getEntity() instanceof EntityCow))
            {
                return;
            }
            EntityLivingBase entity = event.getEntity();
            float partialTicks = event.getPartialRenderTick();
            float roll = entity.getRoll(partialTicks);
            float bodyYaw = entity.prevRenderYawOffset + MathHelper.wrapDegrees(entity.renderYawOffset - entity.prevRenderYawOffset) * partialTicks;
            double pivotY = event.getY() + entity.height / 2.0D;
            GlStateManager.pushMatrix();
            // Rotate about the body's forward axis through the middle of the entity
            GlStateManager.translate(event.getX(), pivotY, event.getZ());
            GlStateManager.rotate(-bodyYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(roll, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(bodyYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-event.getX(), -pivotY, -event.getZ());
        }

        @SubscribeEvent
        public static void onRenderLivingPost(RenderLivingEvent.Post<?> event)
        {
            if (ENABLED && event.getEntity() instanceof EntityCow)
            {
                GlStateManager.popMatrix();
            }
        }
    }
}
