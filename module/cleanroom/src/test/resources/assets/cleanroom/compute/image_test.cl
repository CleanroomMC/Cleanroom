__kernel void read_1d(read_only image1d_t image, __global float4 *output) {
    int i = get_global_id(0);
    output[i] = read_imagef(image, i);
}

__kernel void write_1d(write_only image1d_t image) {
    int i = get_global_id(0);
    write_imagef(image, i, (float4)(i * 4, i * 4 + 1, i * 4 + 2, i * 4 + 3));
}

__kernel void read_2d(read_only image2d_t image, __global float4 *output) {
    int i = get_global_id(0);
    output[i] = read_imagef(image, (int2)(i % 7, i / 7));
}

__kernel void write_2d(write_only image2d_t image) {
    int i = get_global_id(0);
    write_imagef(image, (int2)(i % 7, i / 7), (float4)(i * 4, i * 4 + 1, i * 4 + 2, i * 4 + 3));
}

__kernel void read_3d(read_only image3d_t image, __global float4 *output) {
    int i = get_global_id(0);
    output[i] = read_imagef(image, (int4)(i % 7, (i / 7) % 5, i / 35, 0));
}

__kernel void write_3d(write_only image3d_t image) {
    int i = get_global_id(0);
    write_imagef(image, (int4)(i % 7, (i / 7) % 5, i / 35, 0), (float4)(i * 4, i * 4 + 1, i * 4 + 2, i * 4 + 3));
}

__kernel void read_1d_buffer(read_only image1d_buffer_t image, __global float4 *output) {
    int i = get_global_id(0);
    output[i] = read_imagef(image, i);
}

__kernel void write_1d_buffer(write_only image1d_buffer_t image) {
    int i = get_global_id(0);
    write_imagef(image, i, (float4)(i * 4, i * 4 + 1, i * 4 + 2, i * 4 + 3));
}

__kernel void read_1d_array(read_only image1d_array_t image, __global float4 *output) {
    int i = get_global_id(0);
    output[i] = read_imagef(image, (int2)(i % 7, i / 7));
}

__kernel void write_1d_array(write_only image1d_array_t image) {
    int i = get_global_id(0);
    write_imagef(image, (int2)(i % 7, i / 7), (float4)(i * 4, i * 4 + 1, i * 4 + 2, i * 4 + 3));
}

__kernel void read_2d_array(read_only image2d_array_t image, __global float4 *output) {
    int i = get_global_id(0);
    output[i] = read_imagef(image, (int4)(i % 7, (i / 7) % 5, i / 35, 0));
}

__kernel void write_2d_array(write_only image2d_array_t image) {
    int i = get_global_id(0);
    write_imagef(image, (int4)(i % 7, (i / 7) % 5, i / 35, 0), (float4)(i * 4, i * 4 + 1, i * 4 + 2, i * 4 + 3));
}
