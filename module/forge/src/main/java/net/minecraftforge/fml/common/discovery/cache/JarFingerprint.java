package net.minecraftforge.fml.common.discovery.cache;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.zip.CRC32;
import java.util.zip.CRC32C;
import java.util.zip.ZipEntry;

import net.minecraftforge.fml.common.discovery.ITypeDiscoverer;

/**
 * 64-bit fingerprint of a jar over its scanned class entries, built from central directory fields only:
 * high 32 bits are CRC32C, low 32 bits are CRC32. {@code classEntryCount} is part of the input and
 * travels with the value so the record header can cross-check it.
 */
public record JarFingerprint(long value, int classEntryCount) {

    private static final int BUFFER_BYTES = 1 << 16;
    /** length prefix + crc32 + size, per entry. */
    private static final int FIXED_FIELDS_BYTES = 4 + 4 + 8;

    /** Folds the entries selected by {@link ITypeDiscoverer#shouldScan}, in central directory order. */
    public static JarFingerprint compute(Collection<? extends ZipEntry> entries) {
        CRC32C crc32c = new CRC32C();
        CRC32 crc32 = new CRC32();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_BYTES).order(ByteOrder.LITTLE_ENDIAN);

        int count = 0;
        for (ZipEntry entry : entries) {
            String name = entry.getName();
            if (!ITypeDiscoverer.shouldScan(name)) {
                continue;
            }
            count++;

            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            int needed = FIXED_FIELDS_BYTES + nameBytes.length;
            if (needed > buffer.capacity()) {
                buffer = ByteBuffer.allocate(needed).order(ByteOrder.LITTLE_ENDIAN);
            }
            buffer.clear();
            buffer.putInt(nameBytes.length).put(nameBytes);
            buffer.putInt((int) entry.getCrc());
            buffer.putLong(entry.getSize());
            buffer.flip();
            crc32c.update(buffer);
            buffer.rewind();
            crc32.update(buffer);
        }

        buffer.clear();
        buffer.putInt(count);
        buffer.flip();
        crc32c.update(buffer);
        buffer.rewind();
        crc32.update(buffer);

        long high = crc32c.getValue() & 0xFFFFFFFFL;
        long low = crc32.getValue() & 0xFFFFFFFFL;
        return new JarFingerprint((high << 32) | low, count);
    }
}
