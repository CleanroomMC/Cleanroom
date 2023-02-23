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

/**
 * An entity representing a shot fired by the player's ship
 *
 * @author Kevin Glass
 * @author Brian Matzon
 */
public class ShotEntity extends Entity {

    /** Top border at which shots are outside */
    private static final int TOP_BORDER = -100;

    /** The vertical speed at which the players shot moves */
    private float moveSpeed = -300;

    /** The game in which this entity exists */
    private Game game;

    /** True if this shot has been "used", i.e. its hit something */
    private boolean used;

    /**
     * Create a new shot from the player
     *
     * @param game   The game in which the shot has been created
     * @param sprite The sprite representing this shot
     * @param x      The initial x location of the shot
     * @param y      The initial y location of the shot
     */
    public ShotEntity(Game game, String sprite, int x, int y) {
        super(game.getSprite(sprite), x, y);

        this.game = game;
        dy = moveSpeed;
    }

    /**
     * Reinitializes this entity, for reuse
     *
     * @param x new x coordinate
     * @param y new y coordinate
     */
    public void reinitialize(int x, int y) {
        this.x = x;
        this.y = y;
        used = false;
    }

    /**
     * Request that this shot moved based on time elapsed
     *
     * @param delta The time that has elapsed since last move
     */
    public void move(long delta) {
        // proceed with normal move
        super.move(delta);

        // if we shot off the screen, remove ourselfs
        if (y < TOP_BORDER) {
            game.removeEntity(this);
        }
    }

    /**
     * Notification that this shot has collided with another entity
     *
     * @param other The other entity with which we've collided
     */
    public void collidedWith(Entity other) {
        // prevents double kills, if we've already hit something,
        // don't collide
        if (used) {
            return;
        }

        // if we've hit an alien, kill it!
        if (other instanceof AlienEntity) {
            // remove the affected entities
            game.removeEntity(this);
            game.removeEntity(other);

            // notify the game that the alien has been killed
            game.notifyAlienKilled();
            used = true;
        }
    }
}
