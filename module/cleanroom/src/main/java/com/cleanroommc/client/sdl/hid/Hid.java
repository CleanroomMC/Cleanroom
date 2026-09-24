package com.cleanroommc.client.sdl.hid;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.SDLException;
import org.lwjgl.sdl.SDLHIDAPI;
import org.lwjgl.sdl.SDL_hid_device_info;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Raw HID enumeration. First use calls {@code SDL_hid_init}.
 */
public final class Hid {

    static final Hid INSTANCE = new Hid();

    private Hid() { }

    private static final Set<HidDevice> OPEN = new HashSet<>();
    private static boolean started;


    public List<HidInfo> enumerate() {
        return enumerate(0, 0);
    }

    public List<HidInfo> enumerate(int vendor, int product) {
        ensure();
        SDL_hid_device_info head = SDLHIDAPI.SDL_hid_enumerate((short) vendor, (short) product);
        if (head == null) {
            return List.of();
        }
        try {
            List<HidInfo> devices = new ArrayList<>();
            for (SDL_hid_device_info info = head; info != null; info = info.next()) {
                devices.add(fromNative(info));
            }
            return List.copyOf(devices);
        } finally {
            SDLHIDAPI.SDL_hid_free_enumeration(head);
        }
    }

    public HidDevice open(int vendor, int product) {
        return open(vendor, product, null);
    }

    public HidDevice open(int vendor, int product, String serial) {
        ensure();
        long handle = SDLHIDAPI.SDL_hid_open((short) vendor, (short) product, serial);
        return track(handle, "SDL_hid_open");
    }

    public HidDevice openPath(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        ensure();
        long handle = SDLHIDAPI.SDL_hid_open_path(path);
        return track(handle, "SDL_hid_open_path");
    }

    public int changeCount() {
        ensure();
        return SDLHIDAPI.SDL_hid_device_change_count();
    }

    static synchronized void closeAll() {
        for (HidDevice device : Set.copyOf(OPEN)) {
            device.close();
        }
        OPEN.clear();
        if (started) {
            SDLHIDAPI.SDL_hid_exit();
            started = false;
        }
    }

    static synchronized void forget(HidDevice device) {
        OPEN.remove(device);
    }

    static HidInfo fromNative(SDL_hid_device_info info) {
        return new HidInfo(
                empty(info.pathString()),
                Short.toUnsignedInt(info.vendor_id()),
                Short.toUnsignedInt(info.product_id()),
                empty(info.serial_numberString()),
                empty(info.manufacturer_stringString()),
                empty(info.product_stringString()),
                Short.toUnsignedInt(info.usage_page()),
                Short.toUnsignedInt(info.usage()),
                HidBus.of(info.bus_type())
        );
    }

    private static synchronized void ensure() {
        if (started) {
            return;
        }
        if (SDLHIDAPI.SDL_hid_init() < 0) {
            throw new SDLException("SDL_hid_init failed");
        }
        started = true;
    }

    private static synchronized HidDevice track(long handle, String call) {
        SDL.checkHandle(handle, call);
        HidDevice device = new HidDevice(handle);
        OPEN.add(device);
        return device;
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }

}
