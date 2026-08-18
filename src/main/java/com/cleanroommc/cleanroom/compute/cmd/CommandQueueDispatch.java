package com.cleanroommc.cleanroom.compute.cmd;

import com.cleanroommc.cleanroom.compute.Compute;
import com.cleanroommc.cleanroom.compute.Device;
import com.cleanroommc.cleanroom.compute.errors.UnavaliableDeviceError;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import org.jspecify.annotations.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public final class CommandQueueDispatch implements Closeable {

    private int currDevice = 0;
    private final Map<String, CommandQueue> commandQueues = new Object2ObjectAVLTreeMap<>();

    public CommandQueue dispatch(@NonNull String name) {
        return dispatch(name, false, false, false);
    }

    public CommandQueue dispatch(@NonNull String name, boolean images, boolean mipmaps, boolean pipes) {
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

    public @NonNull Optional<CommandQueue> get(@NonNull String name) {
        Preconditions.checkNotNull(name);
        return Optional.ofNullable(commandQueues.get(name));
    }

    @Override
    public void close() throws IOException {
        for (CommandQueue queue : commandQueues.values()) {
            queue.close();
        }
    }
}
