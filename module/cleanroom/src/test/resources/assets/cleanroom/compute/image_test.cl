const sampler_t samplerA = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_REPEAT | CLK_FILTER_NEAREST;

__kernel void gradient1d(__read_write image1d_t image) {
    size_t idx = get_global_id(0);
    write_imagef(image,
        idx,
        (float4)(((float)idx)/10.f, 0.f, 0.f, 1.f)
    );
}