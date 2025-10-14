#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec4 color;

out vec4 Color;

uniform vec3 worldOffset;

uniform mat4 viewRot;
uniform mat4 projection;

void main(void)
{
    gl_Position = projection * viewRot * vec4(position - worldOffset, 1.0);
    Color = color;
}
