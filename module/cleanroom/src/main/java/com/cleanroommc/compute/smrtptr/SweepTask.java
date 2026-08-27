package com.cleanroommc.compute.smrtptr;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class SweepTask implements Runnable {

    public final ReentrantLock spinlock = new ReentrantLock();
    static final AtomicBoolean running = new AtomicBoolean(true);

    @Override
    public void run() {
        do {
            while (!spinlock.tryLock()) {}
            GarbageCollector.INSTANCE.sweep();
        } while (running.get());
    }

    public void unlock() {
        spinlock.unlock();
    }
}
