package com.cleanroommc.kirino.gl;

import com.cleanroommc.kirino.KirinoCore;

import java.util.PriorityQueue;

public final class GLResourceManager {
    private static final PriorityQueue<GLDisposable> disposables = new PriorityQueue<>();

    public static void addDisposable(GLDisposable disposable) {
        disposables.add(disposable);
    }

    public static void disposeEarly(GLDisposable disposable) {
        if (disposables.remove(disposable)) {
            disposable.dispose();
        } else {
            throw new RuntimeException("Argument \"disposable\" is not in the disposable queue.");
        }
    }

    public static void disposeAll() {
        KirinoCore.LOGGER.info("Starts disposing OpenGL resources.");
        while (!disposables.isEmpty()) {
            GLDisposable disposable = disposables.poll();
            KirinoCore.LOGGER.info("Disposing " + disposable.getName());
            disposable.dispose();
        }
        KirinoCore.LOGGER.info("Finished.");
    }
}
