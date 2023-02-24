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

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;

import org.lwjglx.Sys;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import org.lwjglx.opengl.Display;
import org.lwjglx.opengl.DisplayMode;

/**
 * The main hook of our game. This class with both act as a manager for the display and central mediator for the game
 * logic.
 *
 * Display management will consist of a loop that cycles round all entities in the game asking them to move and then
 * drawing them in the appropriate place. With the help of an inner class it will also allow the player to control the
 * main ship.
 *
 * As a mediator it will be informed when entities within our game detect events (e.g. alient killed, played died) and
 * will take appropriate game actions.
 *
 * <p>
 * NOTE:<br>
 * This game is a LWJGLized implementation of the Space Invaders game by Kevin Glass. The original implementation is
 * renderer agnostic and supports other OpenGL implementations as well as Java2D. This version has been made specific
 * for LWJGL, and has added input control as well as sound (which the original doesn't, at the time of writing). You can
 * find the original article here:<br>
 * <a href="http://www.cokeandcode.com/" target="_blank">http://www.cokeandcode.com</a>
 * </p>
 *
 * @author Kevin Glass
 * @author Brian Matzon
 */
public class Game {

    /** The normal title of the window */
    private String WINDOW_TITLE = "Space Invaders 104 (for LWJGL)";

    /** The width of the game display area */
    private int width = 800;

    /** The height of the game display area */
    private int height = 600;

    /** The loader responsible for converting images into OpenGL textures */
    private TextureLoader textureLoader;

    /** The list of all the entities that exist in our game */
    private ArrayList<Entity> entities = new ArrayList<>();

    /** The list of entities that need to be removed from the game this loop */
    private ArrayList<Entity> removeList = new ArrayList<>();

    /** The entity representing the player */
    private ShipEntity ship;

    /** List of shots */
    private ShotEntity[] shots;

    /** The message to display which waiting for a key press */
    private Sprite message;

    /** The sprite containing the "Press Any Key" message */
    private Sprite pressAnyKey;

    /** The sprite containing the "You win!" message */
    private Sprite youWin;

    /** The sprite containing the "You lose!" message */
    private Sprite gotYou;

    /** Last shot index */
    private int shotIndex;

    /** The speed at which the player's ship should move (pixels/sec) */
    private float moveSpeed = 300;

    /** The time at which last fired a shot */
    private long lastFire;

    /** The interval between our players shot (ms) */
    private long firingInterval = 500;

    /** The number of aliens left on the screen */
    private int alienCount;

    /** True if we're holding up game play until a key has been pressed */
    private boolean waitingForKeyPress = true;

    /** True if game logic needs to be applied this loop, normally as a result of a game event */
    private boolean logicRequiredThisLoop;

    /** The time at which the last rendering looped started from the point of view of the game logic */
    private long lastLoopTime = getTime();

    /** True if the fire key has been released */
    private boolean fireHasBeenReleased;

    /** The time since the last record of fps */
    private long lastFpsTime;

    /** The recorded fps */
    private int fps;

    private static long timerTicksPerSecond = Sys.getTimerResolution();

    /** True if the game is currently "running", i.e. the game loop is looping */
    public static boolean gameRunning = true;

    /** SoundManager to make sound with */
    private SoundManager soundManager;

    /** Whether we're running in fullscreen mode */
    private boolean fullscreen;

    /** ID of shot effect */
    private int SOUND_SHOT;

    /** ID of hit effect */
    private int SOUND_HIT;

    /** ID of start sound */
    private int SOUND_START;

    /** ID of win sound */
    private int SOUND_WIN;

    /** ID of loose sound */
    private int SOUND_LOOSE;

    /** Mouse movement on x axis */
    private int mouseX;

    /** Is this an application or applet */
    private static boolean isApplication;

    /**
     * Construct our game and set it running.
     * 
     * @param fullscreen
     *
     */
    public Game(boolean fullscreen) {
        this.fullscreen = fullscreen;
        initialize();
    }

    /**
     * Get the high resolution time in milliseconds
     *
     * @return The high resolution time in milliseconds
     */
    public static long getTime() {
        // we get the "timer ticks" from the high resolution timer
        // multiply by 1000 so our end result is in milliseconds
        // then divide by the number of ticks in a second giving
        // us a nice clear time in milliseconds
        return (Sys.getTime() * 1000) / timerTicksPerSecond;
    }

    /**
     * Sleep for a fixed number of milliseconds.
     *
     * @param duration The amount of time in milliseconds to sleep for
     */
    public static void sleep(long duration) {
        try {
            Thread.sleep((duration * timerTicksPerSecond) / 1000);
        } catch (InterruptedException inte) {}
    }

    /**
     * Intialise the common elements for the game
     */
    public void initialize() {
        // initialize the window beforehand
        setDisplayMode();
        Display.setTitle(WINDOW_TITLE);
        Display.setFullscreen(fullscreen);
        Display.create();

        // grab the mouse, dont want that hideous cursor when we're playing!
        if (isApplication) {
            Mouse.setGrabbed(true);
        }

        // enable textures since we're going to use these for our sprites
        glEnable(GL_TEXTURE_2D);

        // disable the OpenGL depth test since we're rendering 2D graphics
        glDisable(GL_DEPTH_TEST);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glViewport(0, 0, Display.getFramebufferWidth(), Display.getFramebufferHeight());

        textureLoader = new TextureLoader();

        // create our sound manager, and initialize it with 7 channels
        // 1 channel for sounds, 6 for effects - this should be enough
        // since we have a most 4 shots on screen at any one time, which leaves
        // us with 2 channels for explosions.
        soundManager = new SoundManager();
        soundManager.initialize(8);

        // load our sound data
        SOUND_SHOT = soundManager.addSound("shot.wav");
        SOUND_HIT = soundManager.addSound("hit.wav");
        SOUND_START = soundManager.addSound("start.wav");
        SOUND_WIN = soundManager.addSound("win.wav");
        SOUND_LOOSE = soundManager.addSound("loose.wav");

        // get our sprites
        gotYou = getSprite("gotyou.gif");
        pressAnyKey = getSprite("pressanykey.gif");
        youWin = getSprite("youwin.gif");

        message = pressAnyKey;

        // setup 5 shots
        shots = new ShotEntity[5];
        for (int i = 0; i < shots.length; i++) {
            shots[i] = new ShotEntity(this, "shot.gif", 0, 0);
        }

        // setup the initial game state
        startGame();
    }

    /**
     * Sets the display mode for fullscreen mode
     */
    private boolean setDisplayMode() {
        try {
            // get modes
            DisplayMode[] dm = org.lwjglx.util.Display.getAvailableDisplayModes(width, height, -1, -1, -1, -1, 60, 60);

            org.lwjglx.util.Display.setDisplayMode(
                    dm,
                    new String[] { "width=" + width, "height=" + height, "freq=" + 60,
                            "bpp=" + org.lwjglx.opengl.Display.getDisplayMode().getBitsPerPixel() });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to enter fullscreen, continuing in windowed mode");
        }

        return false;
    }

    /**
     * Start a fresh game, this should clear out any old data and create a new set.
     */
    private void startGame() {
        // clear out any existing entities and intialise a new set
        entities.clear();
        initEntities();
    }

    /**
     * Initialise the starting state of the entities (ship and aliens). Each entitiy will be added to the overall list
     * of entities in the game.
     */
    private void initEntities() {
        // create the player ship and place it roughly in the center of the screen
        ship = new ShipEntity(this, "ship.gif", 370, 550);
        entities.add(ship);

        // create a block of aliens (5 rows, by 12 aliens, spaced evenly)
        alienCount = 0;
        for (int row = 0; row < 5; row++) {
            for (int x = 0; x < 12; x++) {
                Entity alien = new AlienEntity(this, 100 + (x * 50), (50) + row * 30);
                entities.add(alien);
                alienCount++;
            }
        }
    }

    /**
     * Notification from a game entity that the logic of the game should be run at the next opportunity (normally as a
     * result of some game event)
     */
    public void updateLogic() {
        logicRequiredThisLoop = true;
    }

    /**
     * Remove an entity from the game. The entity removed will no longer move or be drawn.
     *
     * @param entity The entity that should be removed
     */
    public void removeEntity(Entity entity) {
        removeList.add(entity);
    }

    /**
     * Notification that the player has died.
     */
    public void notifyDeath() {
        if (!waitingForKeyPress) {
            soundManager.playSound(SOUND_LOOSE);
        }
        message = gotYou;
        waitingForKeyPress = true;
    }

    /**
     * Notification that the player has won since all the aliens are dead.
     */
    public void notifyWin() {
        message = youWin;
        waitingForKeyPress = true;
        soundManager.playSound(SOUND_WIN);
    }

    /**
     * Notification that an alien has been killed
     */
    public void notifyAlienKilled() {
        // reduce the alient count, if there are none left, the player has won!
        alienCount--;

        if (alienCount == 0) {
            notifyWin();
        }

        // if there are still some aliens left then they all need to get faster, so
        // speed up all the existing aliens
        for (Entity entity : entities) {
            if (entity instanceof AlienEntity) {
                // speed up by 2%
                entity.setHorizontalMovement(entity.getHorizontalMovement() * 1.02f);
            }
        }

        soundManager.playEffect(SOUND_HIT);
    }

    /**
     * Attempt to fire a shot from the player. Its called "try" since we must first check that the player can fire at
     * this point, i.e. has he/she waited long enough between shots
     */
    public void tryToFire() {
        // check that we have waiting long enough to fire
        if (System.currentTimeMillis() - lastFire < firingInterval) {
            return;
        }

        // if we waited long enough, create the shot entity, and record the time.
        lastFire = System.currentTimeMillis();
        ShotEntity shot = shots[shotIndex++ % shots.length];
        shot.reinitialize(ship.getX() + 10, ship.getY() - 30);
        entities.add(shot);

        soundManager.playEffect(SOUND_SHOT);
    }

    /**
     * Run the main game loop. This method keeps rendering the scene and requesting that the callback update its screen.
     */
    private void gameLoop() {
        while (Game.gameRunning) {
            // clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            // let subsystem paint
            frameRendering();

            // update window contents
            Display.update();
        }

        // clean up
        soundManager.destroy();
        Display.destroy();
    }

    /**
     * Notification that a frame is being rendered. Responsible for running game logic and rendering the scene.
     */
    public void frameRendering() {
        // SystemTimer.sleep(lastLoopTime+10-SystemTimer.getTime());
        Display.sync(60);

        // work out how long its been since the last update, this
        // will be used to calculate how far the entities should
        // move this loop
        long delta = getTime() - lastLoopTime;
        lastLoopTime = getTime();
        lastFpsTime += delta;
        fps++;

        // update our FPS counter if a second has passed
        if (lastFpsTime >= 1000) {
            Display.setTitle(WINDOW_TITLE + " (FPS: " + fps + ")");
            lastFpsTime = 0;
            fps = 0;
        }

        // cycle round asking each entity to move itself
        if (!waitingForKeyPress && !soundManager.isPlayingSound()) {
            for (Entity entity : entities) {
                entity.move(delta);
            }
        }

        // cycle round drawing all the entities we have in the game
        for (Entity entity : entities) {
            entity.draw();
        }

        // brute force collisions, compare every entity against
        // every other entity. If any of them collide notify
        // both entities that the collision has occured
        for (int p = 0; p < entities.size(); p++) {
            for (int s = p + 1; s < entities.size(); s++) {
                Entity me = entities.get(p);
                Entity him = entities.get(s);

                if (me.collidesWith(him)) {
                    me.collidedWith(him);
                    him.collidedWith(me);
                }
            }
        }

        // remove any entity that has been marked for clear up
        entities.removeAll(removeList);
        removeList.clear();

        // if a game event has indicated that game logic should
        // be resolved, cycle round every entity requesting that
        // their personal logic should be considered.
        if (logicRequiredThisLoop) {
            for (Entity entity : entities) {
                entity.doLogic();
            }

            logicRequiredThisLoop = false;
        }

        // if we're waiting for an "any key" press then draw the
        // current message
        if (waitingForKeyPress) {
            message.draw(325, 250);
        }

        // resolve the movemfent of the ship. First assume the ship
        // isn't moving. If either cursor key is pressed then
        // update the movement appropraitely
        ship.setHorizontalMovement(0);

        // get mouse movement on x axis. We need to get it now, since
        // we can only call getDX ONCE! - secondary calls will yield 0, since
        // there haven't been any movement since last call.
        mouseX = Mouse.getDX();

        // we delegate input checking to submethod since we want to check
        // for keyboard, mouse & controller
        boolean leftPressed = hasInput(Keyboard.KEY_LEFT);
        boolean rightPressed = hasInput(Keyboard.KEY_RIGHT);
        boolean firePressed = hasInput(Keyboard.KEY_SPACE);

        if (!waitingForKeyPress && !soundManager.isPlayingSound()) {
            if ((leftPressed) && (!rightPressed)) {
                ship.setHorizontalMovement(-moveSpeed);
            } else if ((rightPressed) && (!leftPressed)) {
                ship.setHorizontalMovement(moveSpeed);
            }

            // if we're pressing fire, attempt to fire
            if (firePressed) {
                tryToFire();
            }
        } else {
            if (!firePressed) {
                fireHasBeenReleased = true;
            }
            if ((firePressed) && (fireHasBeenReleased) && !soundManager.isPlayingSound()) {
                waitingForKeyPress = false;
                fireHasBeenReleased = false;
                startGame();
                soundManager.playSound(SOUND_START);
            }
        }

        // if escape has been pressed, stop the game
        if ((Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) && isApplication) {
            Game.gameRunning = false;
        }
    }

    /**
     * @param direction
     * @return
     */
    private boolean hasInput(int direction) {
        return switch (direction) {
            case Keyboard.KEY_LEFT -> Keyboard.isKeyDown(Keyboard.KEY_LEFT) || mouseX < 0;
            case Keyboard.KEY_RIGHT -> Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || mouseX > 0;
            case Keyboard.KEY_SPACE -> Keyboard.isKeyDown(Keyboard.KEY_SPACE) || Mouse.isButtonDown(0);
            default -> false;
        };
    }

    /**
     * The entry point into the game. We'll simply create an instance of class which will start the display and game
     * loop.
     *
     * @param argv The arguments that are passed into our game
     */
    public static void main(String argv[]) {
        isApplication = true;
        System.out.println("Use -fullscreen for fullscreen mode");
        new Game((argv.length > 0 && "-fullscreen".equalsIgnoreCase(argv[0]))).execute();
        System.exit(0);
    }

    /**
     *
     */
    public void execute() {
        gameLoop();
    }

    /**
     * Create or get a sprite which displays the image that is pointed to in the classpath by "ref"
     *
     * @param ref A reference to the image to load
     * @return A sprite that can be drawn onto the current graphics context.
     */
    public Sprite getSprite(String ref) {
        return new Sprite(textureLoader, ref);
    }
}
