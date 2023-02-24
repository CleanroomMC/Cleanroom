/*
 * Copyright (c) 2002-2008 LWJGL Project All rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions are met: * Redistributions of source code
 * must retain the above copyright notice, this list of conditions and the following disclaimer. * Redistributions in
 * binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. * Neither the name of 'LWJGL' nor the names of
 * its contributors may be used to endorse or promote products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.lwjglx.test.spaceinvaders;

import java.awt.Rectangle;

/**
 * An entity represents any element that appears in the game. The entity is responsible for resolving collisions and
 * movement based on a set of properties defined either by subclass or externally.
 *
 * Note that doubles are used for positions. This may seem strange given that pixels locations are integers. However,
 * using double means that an entity can move a partial pixel. It doesn't of course mean that they will be display half
 * way through a pixel but allows us not lose accuracy as we move.
 *
 * @author Kevin Glass
 */
public abstract class Entity {

    /** The current x location of this entity */
    protected float x;

    /** The current y location of this entity */
    protected float y;

    /** The sprite that represents this entity */
    protected Sprite sprite;

    /** The current speed of this entity horizontally (pixels/sec) */
    protected float dx;

    /** The current speed of this entity vertically (pixels/sec) */
    protected float dy;

    /** The rectangle used for this entity during collisions resolution */
    private Rectangle me = new Rectangle();

    /** The rectangle used for other entities during collision resolution */
    private Rectangle him = new Rectangle();

    /**
     * Construct a entity based on a sprite image and a location.
     *
     * @param sprite The reference to the image to be displayed for this entity
     * @param x      The initial x location of this entity
     * @param y      The initial y location of this entity
     */
    protected Entity(Sprite sprite, int x, int y) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
    }

    /**
     * Request that this entity move itself based on a certain ammount of time passing.
     *
     * @param delta The ammount of time that has passed in milliseconds
     */
    public void move(long delta) {
        // update the location of the entity based on move speeds
        x += (delta * dx) / 1000;
        y += (delta * dy) / 1000;
    }

    /**
     * Set the horizontal speed of this entity
     *
     * @param dx The horizontal speed of this entity (pixels/sec)
     */
    public void setHorizontalMovement(float dx) {
        this.dx = dx;
    }

    /**
     * Set the vertical speed of this entity
     *
     * @param dy The vertical speed of this entity (pixels/sec)
     */
    public void setVerticalMovement(float dy) {
        this.dy = dy;
    }

    /**
     * Get the horizontal speed of this entity
     *
     * @return The horizontal speed of this entity (pixels/sec)
     */
    public float getHorizontalMovement() {
        return dx;
    }

    /**
     * Get the vertical speed of this entity
     *
     * @return The vertical speed of this entity (pixels/sec)
     */
    public float getVerticalMovement() {
        return dy;
    }

    /**
     * Draw this entity to the graphics context provided
     */
    public void draw() {
        sprite.draw((int) x, (int) y);
    }

    /**
     * Do the logic associated with this entity. This method will be called periodically based on game events
     */
    public void doLogic() {}

    /**
     * Get the x location of this entity
     *
     * @return The x location of this entity
     */
    public int getX() {
        return (int) x;
    }

    /**
     * Get the y location of this entity
     *
     * @return The y location of this entity
     */
    public int getY() {
        return (int) y;
    }

    /**
     * Check if this entity collised with another.
     *
     * @param other The other entity to check collision against
     * @return True if the entities collide with each other
     */
    public boolean collidesWith(Entity other) {
        me.setBounds((int) x, (int) y, sprite.getWidth(), sprite.getHeight());
        him.setBounds((int) other.x, (int) other.y, other.sprite.getWidth(), other.sprite.getHeight());

        return me.intersects(him);
    }

    /**
     * Notification that this entity collided with another.
     *
     * @param other The entity with which this entity collided.
     */
    public abstract void collidedWith(Entity other);
}
