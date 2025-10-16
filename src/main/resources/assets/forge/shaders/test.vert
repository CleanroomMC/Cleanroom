#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec4 color;
layout(location = 2) in vec2 texCoord0;
layout(location = 3) in vec2 texCoord1;

out vec2 TexCoord0;
out vec2 TexCoord1;
out vec4 Color;

uniform vec3 camPos;
uniform vec3 offset;

uniform mat4 modelView;
uniform mat4 projection;

void main(void)
{
    gl_Position = projection * modelView * vec4(position - camPos + offset, 1.0);
    TexCoord0 = texCoord0;
    TexCoord1 = texCoord1;
    Color = color;
}
