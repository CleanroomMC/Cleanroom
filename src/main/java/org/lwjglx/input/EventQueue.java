package org.lwjglx.input;

/**
 * Internal utility class to keep track of event positions in an array. When the array is full the position will wrap to
 * the beginning.
 */
class EventQueue {

    private int maxEvents = 32;
    private int eventCount = 0;
    private int readEventPos = 0;
    private int writeEventPos = 1;
    private long lastDroppedMessageMs = 0;

    EventQueue(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    /**
     * add event to the queue
     */
    synchronized void add() {
        eventCount++; // increment event count
        if (eventCount > maxEvents) {
            eventCount = maxEvents; // cap max events
            long ms = System.currentTimeMillis();
            if (ms - lastDroppedMessageMs > 1000) {
                lastDroppedMessageMs = ms;
                System.out.println("Dropping LWJGL input events due to not frequent enough polling");
            }
        }

        writeEventPos++; // increment next event position
        if (writeEventPos >= maxEvents) writeEventPos = 0; // wrap next event position

        if (writeEventPos == readEventPos) {
            readEventPos++;
            eventCount--;
        } // skip oldest event is queue full
        if (readEventPos >= maxEvents) readEventPos = 0; // wrap current event position
    }

    /**
     * Increment the event queue
     * 
     * @return - true if there is an event available
     */
    synchronized boolean next() {
        if (eventCount == 0) return false;

        eventCount--; // decrement event count
        readEventPos++; // increment current event position
        if (readEventPos >= maxEvents) readEventPos = 0; // wrap current event position

        return true;
    }

    int getEventCount() {
        return eventCount;
    }

    int getMaxEvents() {
        return maxEvents;
    }

    int getCurrentPos() {
        return readEventPos;
    }

    int getNextPos() {
        return writeEventPos;
    }
}
