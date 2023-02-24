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
 * The entity that represents the players ship
 *
 * @author Kevin Glass
 * @author Brian Matzon
 */
public class ShipEntity extends Entity {

    /** Right border at which to disallow further movement */
    private static final int RIGHT_BORDER = 750;

    /** Left border at which to disallow further movement */
    private static final int LEFT_BORDER = 10;

    /** The game in which the ship exists */
    private Game game;

    /**
     * Create a new entity to represent the players ship
     *
     * @param game The game in which the ship is being created
     * @param ref  The reference to the sprite to show for the ship
     * @param x    The initial x location of the player's ship
     * @param y    The initial y location of the player's ship
     */
    public ShipEntity(Game game, String ref, int x, int y) {
        super(game.getSprite(ref), x, y);

        this.game = game;
    }

    /**
     * Request that the ship move itself based on an elapsed ammount of time
     *
     * @param delta The time that has elapsed since last move (ms)
     */
    public void move(long delta) {
        // if we're moving left and have reached the left hand side
        // of the screen, don't move
        if ((dx < 0) && (x < LEFT_BORDER)) {
            return;
        }
        // if we're moving right and have reached the right hand side
        // of the screen, don't move
        if ((dx > 0) && (x > RIGHT_BORDER)) {
            return;
        }

        super.move(delta);
    }

    /**
     * Notification that the player's ship has collided with something
     *
     * @param other The entity with which the ship has collided
     */
    public void collidedWith(Entity other) {
        // if its an alien, notify the game that the player
        // is dead
        if (other instanceof AlienEntity) {
            game.notifyDeath();
        }
    }
}
