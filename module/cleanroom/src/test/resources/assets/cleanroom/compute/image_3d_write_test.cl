#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable

__kernel void write_3d(write_only image3d_t image) {
    int i = get_global_id(0);
    write_imagef(image, (int4)(i % 7, (i / 7) % 5, i / 35, 0), (float4)(i * 4, i * 4 + 1, i * 4 + 2, i * 4 + 3));
}

