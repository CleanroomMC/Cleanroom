package com.cleanroommc.cleanroom.compute.images;

import com.google.common.base.Preconditions;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL20;
import org.lwjgl.opencl.CL21;
import org.lwjgl.opencl.KHRGLDepthImages;

public enum ChannelType {
    /**
     * Each channel component is a normalized signed 8-bit integer value
     */
    SNORM_INT8(CL10.CL_SNORM_INT8) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels;
        }
    },
    /**
     * Each channel component is a normalized signed 16-bit integer value
     */
    SNORM_INT16(CL10.CL_SNORM_INT16) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels*2;
        }
    },
    /**
     * Each channel component is a normalized unsigned 8-bit integer value
     */
    UNORM_INT8(CL10.CL_UNORM_INT8) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels;
        }
    },
    /**
     * Each channel component is a normalized unsigned 16-bit integer value
     */
    UNORM_INT16(CL10.CL_UNORM_INT16) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels*2;
        }
    },
    /**
     * Represents a normalized 5-6-5 3-channel RGB image. The channel order must be {@link ChannelOrder#RGB RGB} or {@link ChannelOrder#RGBx RGBx}.
     */
    UNORM_SHORT_565(CL10.CL_UNORM_SHORT_565) {
        @Override
        public int sizeof(ChannelOrder order) {
            Preconditions.checkArgument(
                    order.equals(ChannelOrder.RGB)
                    || order.equals(ChannelOrder.RGBx),
                    "For UNORM_SHORT_565, the channel order must be RGB or RGBx."
            );
            return 2;
        }
    },
    /**
     * Represents a normalized x-5-5-5 4-channel xRGB image. The channel order must be {@link ChannelOrder#RGB RGB} or {@link ChannelOrder#RGBx RGBx}.
     */
    UNORM_SHORT_555(CL10.CL_UNORM_SHORT_555) {
        @Override
        public int sizeof(ChannelOrder order) {
            Preconditions.checkArgument(
                    order.equals(ChannelOrder.RGB)
                            || order.equals(ChannelOrder.RGBx),
                    "For UNORM_SHORT_555, the channel order must be RGB or RGBx."
            );
            return 2;
        }
    },
    /**
     * Represents a normalized x-10-10-10 4-channel xRGB image. The channel order must be {@link ChannelOrder#RGB RGB} or {@link ChannelOrder#RGBx RGBx}.
     */
    UNORM_INT_101010(CL10.CL_UNORM_INT_101010) {
        @Override
        public int sizeof(ChannelOrder order) {
            Preconditions.checkArgument(
                    order.equals(ChannelOrder.RGB)
                            || order.equals(ChannelOrder.RGBx),
                    "For UNORM_INT_101010, the channel order must be RGB or RGBx."
            );
            return 4;
        }
    },
    /**
     * Represents a normalized 10-10-10-2 four-channel RGBA image. The channel order must be {@link ChannelOrder#RGBA RGBA}.
     */
    UNORM_INT_101010_2(CL21.CL_UNORM_INT_101010_2) {
        @Override
        public int sizeof(ChannelOrder order) {
            Preconditions.checkArgument(
                    order.equals(ChannelOrder.RGBA),
                    "For UNORM_INT_101010_2, the channel order must be RGBA."
            );
            return 4;
        }
    },
    /**
     * Each channel component is an unnormalized signed 8-bit integer value
     */
    SIGNED_INT8(CL10.CL_SIGNED_INT8) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels;
        }
    },
    /**
     * Each channel component is an unnormalized signed 16-bit integer value
     */
    SIGNED_INT16(CL10.CL_SIGNED_INT16) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels*2;
        }
    },
    /**
     * Each channel component is an unnormalized signed 32-bit integer value
     */
    SIGNED_INT32(CL10.CL_SIGNED_INT32) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels*4;
        }
    },
    /**
     * Each channel component is an unnormalized unsigned 8-bit integer value
     */
    UNSIGNED_INT8(CL10.CL_UNSIGNED_INT8) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels;
        }
    },
    /**
     * Each channel component is an unnormalized unsigned 16-bit integer value
     */
    UNSIGNED_INT16(CL10.CL_UNSIGNED_INT16) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels*2;
        }
    },
    /**
     * Each channel component is a normalized unsigned 24-bit integer value
     */
    UNORM_INT24(KHRGLDepthImages.CL_UNORM_INT24) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels*3;
        }
    },
    /**
     * Each channel component is an unnormalized unsigned 32-bit integer value
     */
    UNSIGNED_INT32(CL10.CL_UNSIGNED_INT32) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels*4;
        }
    },
    /**
     * Each channel component is a 16-bit half-float value
     */
    HALF_FLOAT(CL10.CL_HALF_FLOAT) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels*2;
        }
    },
    /**
     * Each channel component is a single precision floating-point value
     */
    FLOAT(CL20.CL_FLOAT) {
        @Override
        public int sizeof(ChannelOrder order) {
            return order.channels*4;
        }
    };

    public final long type;

    ChannelType(long type) {
        this.type = type;
    }

    /**
     * How large is the color unit for the order.
     * @param order The order
     * @return the size
     */
    public abstract int sizeof(ChannelOrder order);
}
