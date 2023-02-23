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
 * An entity which represents one of our space invader aliens.
 *
 * @author Kevin Glass
 * @author Brian Matzon
 */
public class AlienEntity extends Entity {

    /** Movement made downwards when a border is hit */
    private static final int DOWNWARD_MOVEMENT = 10;

    /** Border at which player dies */
    private static final int BOTTOM_BORDER = 570;

    /** Right border at which to shift direction */
    private static final int RIGHT_BORDER = 750;

    /** Left border at which to shift direction */
    private static final int LEFT_BORDER = 10;

    /** The speed at which the alient moves horizontally */
    private float moveSpeed = 75;

    /** The game in which the entity exists */
    private Game game;

    /** The animation frames */
    private Sprite[] frames = new Sprite[4];

    /** The time since the last frame change took place */
    private long lastFrameChange;

    /** The frame duration in milliseconds, i.e. how long any given frame of animation lasts */
    private long frameDuration = 250;

    /** The current frame of animation being displayed */
    private int frameNumber;

    /**
     * Create a new alien entity
     *
     * @param game The game in which this entity is being created
     * @param x    The intial x location of this alien
     * @param y    The intial y location of this alient
     */
    public AlienEntity(Game game, int x, int y) {
        super(game.getSprite("alien.gif"), x, y);

        // setup the animatin frames
        frames[0] = sprite;
        frames[1] = game.getSprite("alien2.gif");
        frames[2] = sprite;
        frames[3] = game.getSprite("alien3.gif");

        this.game = game;
        dx = -moveSpeed;
    }

    /**
     * Request that this alien moved based on time elapsed
     *
     * @param delta The time that has elapsed since last move
     */
    public void move(long delta) {
        // since the move tells us how much time has passed
        // by we can use it to drive the animation, however
        // its the not the prettiest solution
        lastFrameChange += delta;

        // if we need to change the frame, update the frame number
        // and flip over the sprite in use
        if (lastFrameChange > frameDuration) {
            // reset our frame change time counter
            lastFrameChange = 0;

            // update the frame
            frameNumber++;
            if (frameNumber >= frames.length) {
                frameNumber = 0;
            }

            sprite = frames[frameNumber];
        }

        // if we have reached the left hand side of the screen and
        // are moving left then request a logic update
        if ((dx < 0) && (x < LEFT_BORDER)) {
            game.updateLogic();
        }
        // and vice vesa, if we have reached the right hand side of
        // the screen and are moving right, request a logic update
        if ((dx > 0) && (x > RIGHT_BORDER)) {
            game.updateLogic();
        }

        // proceed with normal move
        super.move(delta);
    }

    /**
     * Update the game logic related to aliens
     */
    public void doLogic() {
        // swap over horizontal movement and move down the
        // screen a bit
        dx = -dx;
        y += DOWNWARD_MOVEMENT;

        // if we've reached the bottom of the screen then the player
        // dies
        if (y > BOTTOM_BORDER) {
            game.notifyDeath();
        }
    }

    /**
     * Notification that this alien has collided with another entity
     *
     * @param other The other entity
     */
    public void collidedWith(Entity other) {
        // collisions with aliens are handled elsewhere
    }
}
