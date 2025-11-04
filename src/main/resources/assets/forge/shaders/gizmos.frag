#version 330 core

out vec4 FragColor;
in vec4 Color;

void main()
{
    FragColor = vec4(Color.rgb + vec3(0.6, 0.6, 0.6), 0.5);
}
