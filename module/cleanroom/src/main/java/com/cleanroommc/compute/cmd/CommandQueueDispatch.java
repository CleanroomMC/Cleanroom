package com.cleanroommc.compute.cmd;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.Device;
import com.cleanroommc.compute.errors.UnavaliableDeviceError;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import org.jspecify.annotations.NonNull;

import java.io.Closeable;
import java.util.Map;
import java.util.Optional;

/**
 * Allows for the selection of CommandQueues based on device capabilities.
 * @author EΣrie
 */
public final class CommandQueueDispatch implements Closeable {

    private int currDevice = 0;
    private final Map<String, CommandQueue> commandQueues = new Object2ObjectAVLTreeMap<>();

    /**
     * Select any device and return a CommandQueue.
     * @param name name of the CommandQueue
     * @return the CommandQueue
     * @author EΣrie
     */
    public CommandQueue dispatch(@NonNull String name) {
        return dispatch(name, false, false, false);
    }

    /**
     * Select a device based on the given capabilities and return a CommandQueue.
     * @param name name of the CommandQueue
     * @param images should the device support images?
     * @param mipmaps should the device support mipmapped images?
     * @param pipes should the device support pipes?
     * @return the CommandQueue
     * @throws UnavaliableDeviceError if no device is found matching the given capabilities.
     * @author EΣrie
     */
    public CommandQueue dispatch(@NonNull String name, boolean images, boolean mipmaps, boolean pipes) throws UnavaliableDeviceError {
        Preconditions.checkNotNull(name);
        int startDevice = currDevice;
        do {
            Device device = Compute.instance().devices[currDevice];
            currDevice = (currDevice + 1) % Compute.instance().devices.length;
            if ((!images || device.supportsImages()) &&
                    (!mipmaps || device.supportsMipmaps()) &&
                    (!pipes || device.supportsPipes())) {
                CommandQueue queue = new CommandQueue(device.handle());
                commandQueues.put(name, queue);
                return queue;
            }
        } while (currDevice != startDevice);
        throw new UnavaliableDeviceError("No device found matching requirements.");
    }

    /**
     * Get CommandQueue by name.
     * @param name name of the CommandQueue
     * @return the CommandQueue, or empty if it doesn't exist.
     * @author EΣrie
     */
    public @NonNull Optional<CommandQueue> get(@NonNull String name) {
        Preconditions.checkNotNull(name);
        return Optional.ofNullable(commandQueues.get(name));
    }

    /**
     * Close all CommandQueues.
     * @author EΣrie
     */
    @Override
    public void close() {
        for (CommandQueue queue : commandQueues.values()) {
            queue.close();
        }
    }
}
