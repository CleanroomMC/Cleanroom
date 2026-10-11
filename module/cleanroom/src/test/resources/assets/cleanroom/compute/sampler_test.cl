__kernel void sample_image(read_only image2d_t image, sampler_t sampler,
                           float2 coordinates, __global float4 *output) {
    output[0] = read_imagef(image, sampler, coordinates);
}
