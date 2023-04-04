package org.lwjgl.lwjgl3ify.textures;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;

// ReadableByteChannel impl without locking.
// In cases where the channel can never be accessed concurrently, this is a significant performance improvement.
public class FastByteChannel extends AbstractInterruptibleChannel implements ReadableByteChannel {

    private final InputStream in;
    private static final int TRANSFER_SIZE = 8192;
    private byte[] buf = new byte[0];

    public FastByteChannel(InputStream in) {
        this.in = in;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }

        int len = dst.remaining();
        int totalRead = 0;
        int bytesRead = 0;
        while (totalRead < len) {
            int bytesToRead = Math.min((len - totalRead), TRANSFER_SIZE);
            if (buf.length < bytesToRead) {
                buf = new byte[bytesToRead];
            }

            if ((totalRead > 0) && !(in.available() > 0)) {
                break; // block at most once
            }

            try {
                begin();
                bytesRead = in.read(buf, 0, bytesToRead);
            } finally {
                end(bytesRead > 0);
            }

            if (bytesRead < 0) {
                break;
            } else {
                totalRead += bytesRead;
            }
            dst.put(buf, 0, bytesRead);
        }

        if ((bytesRead < 0) && (totalRead == 0)) {
            return -1;
        }

        return totalRead;
    }

    @Override
    protected void implCloseChannel() throws IOException {
        in.close();
    }
}
