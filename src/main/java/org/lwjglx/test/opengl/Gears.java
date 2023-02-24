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

/*
 * 3-D gear wheels. Originally by Brian Paul
 */
package org.lwjglx.test.opengl;

import static org.lwjgl.opengl.ARBTransposeMatrix.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

import org.lwjgl.Version;
import org.lwjglx.BufferUtils;
import org.lwjglx.LWJGLException;
import org.lwjglx.LWJGLUtil;
import org.lwjglx.opengl.Display;
import org.lwjglx.opengl.DisplayMode;
import org.lwjglx.opengl.GLContext;

/**
 * <p>
 * This is the OpenGL "standard" Gears demo, originally by Brian Paul
 * </p>
 * 
 * @author Brian Matzon <brian@matzon.dk>
 * @version $Revision: 3418 $ $Id: Gears.java 3418 2010-09-28 21:11:35Z spasi $
 */
public class Gears {

    private float view_rotx = 20.0f;

    private float view_roty = 30.0f;

    private float view_rotz;

    private int gear1;

    private int gear2;

    private int gear3;

    private float angle;

    public static void main(String[] args) {
        new Gears().execute();
        System.exit(0);
    }

    /**
     *
     */
    private void execute() {
        try {
            init();
        } catch (LWJGLException le) {
            le.printStackTrace();
            System.out.println("Failed to initialize Gears.");
            return;
        }

        loop();

        destroy();
    }

    /**
     *
     */
    private void destroy() {
        Display.destroy();
    }

    /**
     *
     */
    private void loop() {
        long startTime = System.currentTimeMillis() + 5000;
        long fps = 0;

        while (!Display.isCloseRequested()) {
            angle += 2.0f;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glPushMatrix();
            glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
            glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
            glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);

            glPushMatrix();
            glTranslatef(-3.0f, -2.0f, 0.0f);
            glRotatef(angle, 0.0f, 0.0f, 1.0f);
            glCallList(gear1);
            glPopMatrix();

            glPushMatrix();
            glTranslatef(3.1f, -2.0f, 0.0f);
            glRotatef(-2.0f * angle - 9.0f, 0.0f, 0.0f, 1.0f);
            glCallList(gear2);
            glPopMatrix();

            glPushMatrix();
            glTranslatef(-3.1f, 4.2f, 0.0f);
            glRotatef(-2.0f * angle - 25.0f, 0.0f, 0.0f, 1.0f);
            glCallList(gear3);
            glPopMatrix();

            glPopMatrix();

            Display.update();
            if (startTime > System.currentTimeMillis()) {
                fps++;
            } else {
                long timeUsed = 5000 + (startTime - System.currentTimeMillis());
                startTime = System.currentTimeMillis() + 5000;
                System.out.println(fps + " frames in " + timeUsed / 1000f + " seconds = " + (fps / (timeUsed / 1000f)));
                fps = 0;
            }
        }
    }

    /**
     *
     */
    private void init() throws LWJGLException {
        // create Window of size 300x300
        Display.setLocation(
                (Display.getDisplayMode().getWidth() - 300) / 2,
                (Display.getDisplayMode().getHeight() - 300) / 2);
        Display.setDisplayMode(new DisplayMode(300, 300));
        Display.setTitle("Gears");
        Display.create();

        // setup ogl
        FloatBuffer pos = BufferUtils.createFloatBuffer(4).put(new float[] { 5.0f, 5.0f, 10.0f, 0.0f });
        FloatBuffer red = BufferUtils.createFloatBuffer(4).put(new float[] { 0.8f, 0.1f, 0.0f, 1.0f });
        FloatBuffer green = BufferUtils.createFloatBuffer(4).put(new float[] { 0.0f, 0.8f, 0.2f, 1.0f });
        FloatBuffer blue = BufferUtils.createFloatBuffer(4).put(new float[] { 0.2f, 0.2f, 1.0f, 1.0f });

        pos.flip();
        red.flip();
        green.flip();
        blue.flip();

        glLightfv(GL_LIGHT0, GL_POSITION, pos);
        glEnable(GL_CULL_FACE);
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_DEPTH_TEST);

        /* make the gears */
        gear1 = glGenLists(1);
        glNewList(gear1, GL_COMPILE);
        glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, red);
        gear(1.0f, 4.0f, 1.0f, 20, 0.7f);
        glEndList();

        gear2 = glGenLists(1);
        glNewList(gear2, GL_COMPILE);
        glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, green);
        gear(0.5f, 2.0f, 2.0f, 10, 0.7f);
        glEndList();

        gear3 = glGenLists(1);
        glNewList(gear3, GL_COMPILE);
        glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, blue);
        gear(1.3f, 2.0f, 0.5f, 10, 0.7f);
        glEndList();

        glEnable(GL_NORMALIZE);

        glMatrixMode(GL_PROJECTION);

        System.err.println("LWJGL: " + Version.getVersion() + " / " + LWJGLUtil.getPlatformName());
        System.err.println("GL_VENDOR: " + glGetString(GL_VENDOR));
        System.err.println("GL_RENDERER: " + glGetString(GL_RENDERER));
        System.err.println("GL_VERSION: " + glGetString(GL_VERSION));
        System.err.println();
        System.err.println(
                "glLoadTransposeMatrixfARB() supported: " + GLContext.getCapabilities().GL_ARB_transpose_matrix);
        if (!GLContext.getCapabilities().GL_ARB_transpose_matrix) {
            // --- not using extensions
            glLoadIdentity();
        } else {
            // --- using extensions
            final FloatBuffer identityTranspose = BufferUtils.createFloatBuffer(16)
                    .put(new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 });
            identityTranspose.flip();
            glLoadTransposeMatrixfARB(identityTranspose);
        }

        float h = (float) 300 / (float) 300;
        glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 60.0f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(0.0f, 0.0f, -40.0f);
    }

    /**
     * Draw a gear wheel. You'll probably want to call this function when building a display list since we do a lot of
     * trig here.
     *
     * @param inner_radius radius of hole at center
     * @param outer_radius radius at center of teeth
     * @param width        width of gear
     * @param teeth        number of teeth
     * @param tooth_depth  depth of tooth
     */
    private void gear(float inner_radius, float outer_radius, float width, int teeth, float tooth_depth) {
        int i;
        float r0, r1, r2;
        float angle, da;
        float u, v, len;

        r0 = inner_radius;
        r1 = outer_radius - tooth_depth / 2.0f;
        r2 = outer_radius + tooth_depth / 2.0f;

        da = 2.0f * (float) Math.PI / teeth / 4.0f;

        glShadeModel(GL_FLAT);

        glNormal3f(0.0f, 0.0f, 1.0f);

        /* draw front face */
        glBegin(GL_QUAD_STRIP);
        for (i = 0; i <= teeth; i++) {
            angle = i * 2.0f * (float) Math.PI / teeth;
            glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), width * 0.5f);
            glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), width * 0.5f);
            if (i < teeth) {
                glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), width * 0.5f);
                glVertex3f(
                        r1 * (float) Math.cos(angle + 3.0f * da),
                        r1 * (float) Math.sin(angle + 3.0f * da),
                        width * 0.5f);
            }
        }
        glEnd();

        /* draw front sides of teeth */
        glBegin(GL_QUADS);
        for (i = 0; i < teeth; i++) {
            angle = i * 2.0f * (float) Math.PI / teeth;
            glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), width * 0.5f);
            glVertex3f(r2 * (float) Math.cos(angle + da), r2 * (float) Math.sin(angle + da), width * 0.5f);
            glVertex3f(
                    r2 * (float) Math.cos(angle + 2.0f * da),
                    r2 * (float) Math.sin(angle + 2.0f * da),
                    width * 0.5f);
            glVertex3f(
                    r1 * (float) Math.cos(angle + 3.0f * da),
                    r1 * (float) Math.sin(angle + 3.0f * da),
                    width * 0.5f);
        }
        glEnd();

        /* draw back face */
        glBegin(GL_QUAD_STRIP);
        for (i = 0; i <= teeth; i++) {
            angle = i * 2.0f * (float) Math.PI / teeth;
            glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), -width * 0.5f);
            glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), -width * 0.5f);
            glVertex3f(r1 * (float) Math.cos(angle + 3 * da), r1 * (float) Math.sin(angle + 3 * da), -width * 0.5f);
            glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), -width * 0.5f);
        }
        glEnd();

        /* draw back sides of teeth */
        glBegin(GL_QUADS);
        for (i = 0; i < teeth; i++) {
            angle = i * 2.0f * (float) Math.PI / teeth;
            glVertex3f(r1 * (float) Math.cos(angle + 3 * da), r1 * (float) Math.sin(angle + 3 * da), -width * 0.5f);
            glVertex3f(r2 * (float) Math.cos(angle + 2 * da), r2 * (float) Math.sin(angle + 2 * da), -width * 0.5f);
            glVertex3f(r2 * (float) Math.cos(angle + da), r2 * (float) Math.sin(angle + da), -width * 0.5f);
            glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), -width * 0.5f);
        }
        glEnd();

        /* draw outward faces of teeth */
        glBegin(GL_QUAD_STRIP);
        for (i = 0; i < teeth; i++) {
            angle = i * 2.0f * (float) Math.PI / teeth;
            glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), width * 0.5f);
            glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), -width * 0.5f);
            u = r2 * (float) Math.cos(angle + da) - r1 * (float) Math.cos(angle);
            v = r2 * (float) Math.sin(angle + da) - r1 * (float) Math.sin(angle);
            len = (float) Math.sqrt(u * u + v * v);
            u /= len;
            v /= len;
            glNormal3f(v, -u, 0.0f);
            glVertex3f(r2 * (float) Math.cos(angle + da), r2 * (float) Math.sin(angle + da), width * 0.5f);
            glVertex3f(r2 * (float) Math.cos(angle + da), r2 * (float) Math.sin(angle + da), -width * 0.5f);
            glNormal3f((float) Math.cos(angle), (float) Math.sin(angle), 0.0f);
            glVertex3f(r2 * (float) Math.cos(angle + 2 * da), r2 * (float) Math.sin(angle + 2 * da), width * 0.5f);
            glVertex3f(r2 * (float) Math.cos(angle + 2 * da), r2 * (float) Math.sin(angle + 2 * da), -width * 0.5f);
            u = r1 * (float) Math.cos(angle + 3 * da) - r2 * (float) Math.cos(angle + 2 * da);
            v = r1 * (float) Math.sin(angle + 3 * da) - r2 * (float) Math.sin(angle + 2 * da);
            glNormal3f(v, -u, 0.0f);
            glVertex3f(r1 * (float) Math.cos(angle + 3 * da), r1 * (float) Math.sin(angle + 3 * da), width * 0.5f);
            glVertex3f(r1 * (float) Math.cos(angle + 3 * da), r1 * (float) Math.sin(angle + 3 * da), -width * 0.5f);
            glNormal3f((float) Math.cos(angle), (float) Math.sin(angle), 0.0f);
        }
        glVertex3f(r1 * (float) Math.cos(0), r1 * (float) Math.sin(0), width * 0.5f);
        glVertex3f(r1 * (float) Math.cos(0), r1 * (float) Math.sin(0), -width * 0.5f);
        glEnd();

        glShadeModel(GL_SMOOTH);

        /* draw inside radius cylinder */
        glBegin(GL_QUAD_STRIP);
        for (i = 0; i <= teeth; i++) {
            angle = i * 2.0f * (float) Math.PI / teeth;
            glNormal3f(-(float) Math.cos(angle), -(float) Math.sin(angle), 0.0f);
            glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), -width * 0.5f);
            glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), width * 0.5f);
        }
        glEnd();
    }
}
