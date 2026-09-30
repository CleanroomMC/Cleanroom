package net.minecraftforge.client.event;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Base class for client-side player input events fired by Forge.
 *
 * <p>Note: this class is unrelated to {@link net.minecraftforge.fml.common.gameevent.InputEvent}
 * (the FML key/mouse input events). The two share a simple name, so subscribers importing both
 * must fully qualify one of them.
 *
 * <p>All subclasses are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public abstract class InputEvent extends Event
{
    /**
     * MouseTurnEvent is fired when the mouse is about to rotate the local player's view, just before
     * {@link Entity#turn(float, float)} is called from {@code EntityRenderer.updateCameraAndRender}.
     *
     * <p>{@link #yaw} and {@link #pitch} are exactly the values vanilla passes to {@link Entity#turn(float, float)}:
     * already scaled by mouse sensitivity ({@code (sensitivity * 0.6 + 0.2)^3 * 8}), with the pitch already
     * multiplied by the invert-mouse sign, and, when smooth camera is active, already filtered.
     * {@link Entity#turn(float, float)} itself multiplies both by 0.15 to obtain degrees.
     *
     * <p>This event is fired once per rendered frame while {@code inGameHasFocus && Display.isActive()},
     * including frames with a zero mouse delta. {@link #frameDelta} is the duration of the current frame
     * in seconds, see {@link net.minecraftforge.client.ForgeHooksClient#getFrameDeltaSeconds()}.
     *
     * <p>Handlers may call {@link Entity#turn(float, float)} on the player themselves; the hook sits only at the
     * {@code EntityRenderer} call sites, so doing so does not re-fire this event.
     *
     * <p>This event is {@link Cancelable}. If this event is canceled, the player is not turned this frame.
     *
     * <p>This event does not have a result. {@link HasResult}
     *
     * <p>This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class MouseTurnEvent extends InputEvent
    {
        private final EntityPlayerSP player;
        private float yaw;
        private float pitch;
        private final float frameDelta;

        public MouseTurnEvent(EntityPlayerSP player, float yaw, float pitch, float frameDelta)
        {
            this.player = player;
            this.yaw = yaw;
            this.pitch = pitch;
            this.frameDelta = frameDelta;
        }

        public EntityPlayerSP getPlayer()
        {
            return player;
        }

        public float getYaw()
        {
            return yaw;
        }

        public void setYaw(float yaw)
        {
            this.yaw = yaw;
        }

        public float getPitch()
        {
            return pitch;
        }

        public void setPitch(float pitch)
        {
            this.pitch = pitch;
        }

        public float getFrameDeltaSeconds()
        {
            return frameDelta;
        }
    }
}
