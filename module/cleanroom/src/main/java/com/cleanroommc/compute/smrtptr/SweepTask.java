package com.cleanroommc.compute.smrtptr;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class SweepTask implements Runnable {

    public final ReentrantLock spinlock = new ReentrantLock();
    static final AtomicBoolean running = new AtomicBoolean(true);

    @Override
    public void run() {
        running.compareAndExchangeRelease(false, true);
        do {
            while (!spinlock.tryLock()) {
                if (running.getAcquire())
                    return;
            }
            GarbageCollector.INSTANCE.sweep();
        } while (running.getAcquire());
    }

    public void unlock() {
        spinlock.unlock();
    }
}
