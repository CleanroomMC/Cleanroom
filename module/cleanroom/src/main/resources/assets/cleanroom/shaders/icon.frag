#version 460 core

in vec2 ViewportUV;
out vec4 Color;

uniform vec2 resolution;
uniform float progress;
uniform sampler2D logoTexture;
uniform vec2 logoCenter;
uniform vec2 logoScale;
uniform vec3 background;

const float INITIAL_SCALE = 0.94;
const float INITIAL_BLUR = 4.0;
const float INITIAL_OFFSET_Y = 7.0;

vec4 sampleLogo(vec2 uv)
{
    if (any(lessThan(uv, vec2(0.0))) || any(greaterThan(uv, vec2(1.0)))) return vec4(0.0);
    vec4 color = texture(logoTexture, vec2(uv.x, 1.0 - uv.y));
    return vec4(color.rgb * color.a, color.a);
}

void main()
{
    float t = clamp(progress, 0.0, 1.0);
    float eased = 1.0 - pow(1.0 - t, 3.0);
    float opacity = smoothstep(0.0, 0.72, t);
    vec2 scale = logoScale * mix(INITIAL_SCALE, 1.0, eased);
    vec2 center = logoCenter - vec2(0.0, INITIAL_OFFSET_Y / resolution.y) * (1.0 - eased);
    vec2 uv = (ViewportUV - center) / scale + 0.5;
    vec2 blurStep = INITIAL_BLUR * (1.0 - eased) / resolution / scale;
    vec2 extent = blurStep * 2.0;

    if (opacity == 0.0 || any(lessThan(uv, -extent)) || any(greaterThan(uv, 1.0 + extent)))
    {
        Color = vec4(background, 1.0);
        return;
    }

    vec4 logo;
    if (t == 1.0)
    {
        logo = sampleLogo(uv);
    }
    else
    {
        const float weights[5] = float[5](1.0, 4.0, 6.0, 4.0, 1.0);
        logo = vec4(0.0);
        for (int y = -2; y <= 2; y++)
        {
            for (int x = -2; x <= 2; x++)
            {
                logo += sampleLogo(uv + vec2(x, y) * blurStep) * weights[x + 2] * weights[y + 2] / 256.0;
            }
        }
    }

    Color = vec4(background * (1.0 - logo.a * opacity) + logo.rgb * opacity, 1.0);
}
