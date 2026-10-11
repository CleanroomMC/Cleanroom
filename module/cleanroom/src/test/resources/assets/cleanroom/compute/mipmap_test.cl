#pragma OPENCL EXTENSION cl_khr_mipmap_image : enable

__kernel void sample_mipmap(read_only image2d_t image, sampler_t sampler,
                            float lod, __global float4 *output) {
    output[0] = read_imagef(image, sampler, (float2)(.5f, .5f), lod);
}
