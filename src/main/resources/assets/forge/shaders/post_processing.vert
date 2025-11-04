#version 330 core

layout (location = 0) in vec3 position;
out vec2 TexCoords;

void main()
{
    gl_Position = vec4(position, 1.0);
    TexCoords = vec2(0.5 * gl_Position.x, 0.5 * gl_Position.y) + vec2(0.5);
}
