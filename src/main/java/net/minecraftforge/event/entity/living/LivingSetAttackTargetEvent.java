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

package net.minecraftforge.event.entity.living;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * LivingSetAttackTargetEvent is fired when an Entity sets a target to attack.<br>
 * This event is fired whenever an Entity sets a target to attack in
 * {@link EntityLiving#setAttackTarget(EntityLivingBase)}.<br>
 * <br>
 * This event is fired via the {@link ForgeHooks#onLivingSetAttackTarget(EntityLiving, EntityLivingBase)}.<br>
 * <br>
 * {@link #originalTarget} contains the newly targeted Entity.<br>
 * <br>
 * {@link #redirectedTarget} contains the redirected Targeted Entity.<br>
 * <br>
 * This event is {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
@Cancelable
public class LivingSetAttackTargetEvent extends LivingEvent{
    private final EntityLivingBase originalTarget;
    private EntityLivingBase newTarget;
    private boolean isModified;
    
    public LivingSetAttackTargetEvent(EntityLivingBase entity, EntityLivingBase target)
    {
        super(entity);
        this.originalTarget = target;
        this.newTarget = null;
        this.isModified = false;
    }

    /**
    * Get the target that will be actually applied
    **/
    public EntityLivingBase getTarget()
    {
        return isModified ? newTarget : originalTarget;
    }

    /**
    * return the original attack target
    **/
    public EntityLivingBase getOriginalTarget(){
        return originalTarget;
    }

    /**
    * Set the attack target of the living's, null if remove it
    **/
    public void setNewTarget(EntityLivingBase living){
        this.redirectedTarget = living;
        this.isModified = true;
    }

    /**
    * Is the attack target is modified
    **/
    public boolean isModified(){
        return this.isModified;
    }
}
