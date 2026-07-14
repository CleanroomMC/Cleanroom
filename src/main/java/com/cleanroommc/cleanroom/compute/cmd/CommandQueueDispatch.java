package com.cleanroommc.cleanroom.compute.cmd;

import com.cleanroommc.cleanroom.compute.Compute;
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
        Preconditions.checkNotNull(name);
        CommandQueue queue = commandQueues.put(name, new CommandQueue(Compute.instance().devices[currDevice]));
        currDevice++;
        currDevice %= Compute.instance().devices.length;
        return queue;
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
