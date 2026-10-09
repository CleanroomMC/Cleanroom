package net.minecraftforge.fml.common.discovery.cache;

import java.util.List;

import com.google.common.primitives.Ints;

/**
 * The annotation scan of one jar: every scanned class entry that {@linkplain #hasContent has content}, tagged with
 * its ordinal among the jar's scanned entries, plus the fingerprint the scan was taken for.
 *
 * <p>{@code indices} and {@code records} are parallel and ascending by index. Classes whose scan is empty are not
 * stored at all (they only contribute a class entry, which the container discovery takes from the central
 * directory). {@code fromDisk} tells whether the scan was read back from the cache or freshly parsed; only fresh
 * scans are written.
 */
public record JarScanRecord(JarFingerprint fingerprint, boolean fromDisk, int[] indices, ClassScanRecord[] records) {

    public JarScanRecord {
        if (indices.length != records.length) {
            throw new IllegalArgumentException("indices and records must be parallel");
        }
    }

    public static boolean hasContent(ClassScanRecord record) {
        return record.annotations().length > 0 || record.interfaces().length > 0;
    }

    public static JarScanRecord of(JarFingerprint fingerprint, List<Integer> indices, List<ClassScanRecord> records) {
        return new JarScanRecord(fingerprint, false, Ints.toArray(indices), records.toArray(new ClassScanRecord[0]));
    }
}
