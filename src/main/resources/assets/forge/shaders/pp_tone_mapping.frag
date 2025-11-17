#version 330 core

out vec4 FragColor;
in vec2 TexCoords;

uniform sampler2D screenTexture;

const float baseExposure = 1.0;

vec3 ACESFilm(vec3 x)
{
    const float a = 2.51;
    const float b = 0.03;
    const float c = 2.43;
    const float d = 0.59;
    const float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

float computeAutoExposure(vec3 color)
{
    float brightness = dot(color, vec3(0.2126, 0.7152, 0.0722));
    float adapted = 0.18 / (brightness + 0.001);
    return clamp(adapted * baseExposure, 0.4, 1.8);
}

void main()
{
    vec3 hdrColor = texture(screenTexture, TexCoords).rgb;

    vec3 color = hdrColor * computeAutoExposure(hdrColor);
    color = ACESFilm(color);
    color = pow(color, vec3(1.0 / 2.2));

    FragColor = vec4(color, 1.0);
}
