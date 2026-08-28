package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.events.SensorEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLSensor;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLTimer;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_SensorEvent;

import java.nio.IntBuffer;
import java.util.List;

/**
 * Standalone sensors. First use initializes {@code SDL_INIT_SENSOR}.
 */
public final class Sensors {

    private final Int2ObjectMap<Sensor> sensors = new Int2ObjectArrayMap<>();

    private boolean started;

    Sensors() { }

    synchronized void ensure() {
        if (started) {
            return;
        }
        SDL.ensureSubsystem(SDLInit.SDL_INIT_SENSOR);
        started = true;
        openConnected();
    }

    public synchronized List<Sensor> list() {
        ensure();
        return List.copyOf(sensors.values());
    }

    public synchronized Sensor byId(int id) {
        ensure();
        return sensors.get(id);
    }

    public synchronized void handle(SDL_Event event) {
        if (!started) {
            return;
        }
        int type = event.type();
        if (type == SDLEvents.SDL_EVENT_SENSOR_UPDATE) {
            SDL_SensorEvent update = event.sensor();
            Sensor sensor = sensors.get(update.which());
            if (sensor == null) {
                return;
            }
            float[] data = {update.data(0), update.data(1), update.data(2)};
            SDL.EVENT_BUS.post(new SensorEvent.Updated(update.timestamp(), sensor, data,
                    update.sensor_timestamp()));
        }
        // Standalone sensors have no add/remove event
    }

    synchronized void reset() {
        for (Sensor sensor : sensors.values()) {
            SDLSensor.SDL_CloseSensor(sensor.handle());
        }
        sensors.clear();
        started = false;
    }

    private void openConnected() {
        IntBuffer ids = SDLSensor.SDL_GetSensors();
        if (ids == null) {
            return;
        }
        try {
            while (ids.hasRemaining()) {
                open(ids.get());
            }
        } finally {
            SDLStdinc.SDL_free(ids);
        }
    }

    private Sensor open(int id) {
        Sensor existing = sensors.get(id);
        if (existing != null) {
            return existing;
        }
        long already = SDLSensor.SDL_GetSensorFromID(id);
        long handle = already != 0L ? already : SDLSensor.SDL_OpenSensor(id);
        if (handle == 0L) {
            return null;
        }
        Sensor sensor = new Sensor(id, handle);
        sensors.put(id, sensor);
        return sensor;
    }

    synchronized Sensor injectAdded(int id, long handle) {
        started = true;
        Sensor sensor = new Sensor(id, handle);
        sensors.put(id, sensor);
        SDL.EVENT_BUS.post(new SensorEvent.Added(SDLTimer.SDL_GetTicksNS(), sensor));
        return sensor;
    }

    synchronized void injectRemoved(int id) {
        sensors.remove(id);
        SDL.EVENT_BUS.post(new SensorEvent.Removed(id, SDLTimer.SDL_GetTicksNS()));
    }

}
