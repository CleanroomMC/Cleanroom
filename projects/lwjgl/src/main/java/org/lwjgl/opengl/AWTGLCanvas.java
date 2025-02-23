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
package org.lwjgl.opengl;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;

public class AWTGLCanvas extends Canvas implements Drawable, ComponentListener, HierarchyListener {

    private static final long serialVersionUID = 1L;

    public void setPixelFormat(final PixelFormatLWJGL pf) throws LWJGLException {
        throw new UnsupportedOperationException();
    }

    public void setPixelFormat(final PixelFormatLWJGL pf, final ContextAttribs attribs) throws LWJGLException {
        throw new UnsupportedOperationException();
    }

    public PixelFormatLWJGL getPixelFormat() {
        return null;
    }

    public ContextGL getContext() {
        return null;
    }

    public ContextGL createSharedContext() throws LWJGLException {
        return null;
    }

    public void checkGLError() {}

    public void initContext(final float r, final float g, final float b) {}

    public AWTGLCanvas() throws LWJGLException {}

    public AWTGLCanvas(PixelFormat pixel_format) throws LWJGLException {}

    public AWTGLCanvas(GraphicsDevice device, PixelFormat pixel_format) throws LWJGLException {}

    public AWTGLCanvas(GraphicsDevice device, PixelFormat pixel_format, Drawable drawable) throws LWJGLException {}

    public AWTGLCanvas(GraphicsDevice device, PixelFormat pixel_format, Drawable drawable, ContextAttribs attribs)
            throws LWJGLException {}

    public void addNotify() {}

    public void removeNotify() {}

    public void setSwapInterval(int swap_interval) {}

    public void setVSyncEnabled(boolean enabled) {}

    public void swapBuffers() throws LWJGLException {}

    public boolean isCurrent() throws LWJGLException {
        return false;
    }

    public void makeCurrent() throws LWJGLException {}

    public void releaseContext() throws LWJGLException {}

    public final void destroy() {}

    public final void setCLSharingProperties(final PointerBuffer properties) throws LWJGLException {}

    protected void initGL() {}

    protected void paintGL() {}

    public final void paint(Graphics g) {}

    protected void exceptionOccurred(LWJGLException exception) {}

    public void update(Graphics g) {}

    public void componentShown(ComponentEvent e) {}

    public void componentHidden(ComponentEvent e) {}

    public void componentResized(ComponentEvent e) {}

    public void componentMoved(ComponentEvent e) {}

    public void setLocation(int x, int y) {}

    public void setLocation(Point p) {}

    public void setSize(Dimension d) {}

    public void setSize(int width, int height) {}

    public void setBounds(int x, int y, int width, int height) {}

    public void hierarchyChanged(HierarchyEvent e) {}
}
