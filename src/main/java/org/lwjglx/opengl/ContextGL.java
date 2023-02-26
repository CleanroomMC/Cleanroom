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
package org.lwjglx.opengl;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjglx.LWJGLException;
import org.lwjglx.PointerBuffer;

/**
 * <p/>
 * Context encapsulates an OpenGL context.
 * <p/>
 * <p/>
 * This class is thread-safe.
 *
 * @author elias_naur <elias_naur@users.sourceforge.net>
 * @version $Revision$ $Id$
 */
public final class ContextGL implements Context {

    public long glfwWindow = Long.MIN_VALUE;
    public final boolean shared;

    public ContextGL(long glfwWindow, boolean shared) {
        this.glfwWindow = glfwWindow;
        this.shared = shared;
    }

    public void releaseCurrent() throws LWJGLException {
        GLFW.glfwMakeContextCurrent(0);
        GL.setCapabilities(null);
    }

    public synchronized void releaseDrawable() throws LWJGLException {}

    public synchronized void update() {}

    public static void swapBuffers() throws LWJGLException {
        GLFW.glfwSwapBuffers(Display.getWindow());
    }

    public synchronized void makeCurrent() throws LWJGLException {
        GLFW.glfwMakeContextCurrent(glfwWindow);
        GL.createCapabilities();
    }

    public synchronized boolean isCurrent() throws LWJGLException {
        return GLFW.glfwGetCurrentContext() == glfwWindow;
    }

    public static void setSwapInterval(int value) {
        GLFW.glfwSwapInterval(value);
    }

    public synchronized void forceDestroy() throws LWJGLException {
        destroy();
    }

    public synchronized void destroy() throws LWJGLException {
        if (shared && glfwWindow > 0) {
            GLFW.glfwDestroyWindow(glfwWindow);
            glfwWindow = -1;
        }
    }

    public synchronized void setCLSharingProperties(final PointerBuffer properties) throws LWJGLException {}
}
