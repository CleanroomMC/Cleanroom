#version 460 core

out vec2 ViewportUV;

void main()
{
    vec2 position = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
    ViewportUV = position;
    gl_Position = vec4(position * 2.0 - 1.0, 0.0, 1.0);
}
