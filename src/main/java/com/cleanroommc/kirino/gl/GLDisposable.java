package com.cleanroommc.kirino.gl;

public abstract class GLDisposable implements Comparable<GLDisposable> {
    public final String getName() {
        return getResourceIdentifier() + "@" + this.hashCode();
    }

    public String getResourceIdentifier() {
        return this.getClass().getSimpleName();
    }

    // bigger first
    public int disposePriority() {
        return 0;
    }

    public abstract void dispose();

    @Override
    public final int compareTo(GLDisposable other) {
        return -Integer.compare(this.disposePriority(), other.disposePriority());
    }
}
