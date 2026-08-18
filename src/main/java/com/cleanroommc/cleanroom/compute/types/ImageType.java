package com.cleanroommc.cleanroom.compute.types;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record ImageType(int dimensions, boolean fromBuffer, boolean array, boolean msaa, boolean depth) implements OpenCLType {

    public ImageType {
        Preconditions.checkArgument(dimensions >= 1 && dimensions <= 3, "Dimensions must be between 1 & 3.");
        Preconditions.checkArgument(dimensions == 1 || !fromBuffer);
        Preconditions.checkArgument(dimensions == 2 || !msaa);
        Preconditions.checkArgument(dimensions == 2 || !depth);
        Preconditions.checkArgument(!(dimensions == 3 && array));
    }

    @Override
    public int sizeof() {
        return 8;
    }

    @Override
    public @NonNull String toString() {
        return String.format("image%dd%s%st", dimensions, array ? "_array" : (fromBuffer ? "_buffer" : "_"), msaa ? "_msaa_" : "");
    }
}
