#version 460 core

in vec2 ViewportUV;
out vec4 Color;

uniform vec2 resolution;
uniform float progress;
uniform sampler2D logoTexture;
uniform vec2 logoCenter;
uniform vec2 logoScale;
uniform vec3 background;
uniform vec3 glow;
uniform float time;

const float INITIAL_SCALE = 0.94;
const float INITIAL_BLUR = 4.0;
const float INITIAL_OFFSET_Y = 7.0;
const float GLOW_SPEED = 0.85;
const float GLOW_INTENSITY = 0.06;

vec3 sampleBackground(vec2 uv)
{
    float glowTime = time * GLOW_SPEED;
    float aspectRatio = resolution.x / max(resolution.y, 1.0);
    float widthMix = 0.65 * smoothstep(1.3, 4.5, aspectRatio);
    float materialWidth = mix(resolution.y, resolution.x, widthMix);
    vec2 p = (uv - 0.5) * resolution * vec2(1.0, -1.0);
    vec2 materialPos = p / max(vec2(materialWidth, resolution.y), vec2(1.0));

    float centerGlow = 1.0 - smoothstep(0.05, 0.85, length(materialPos));
    vec3 color = background * (0.96 + centerGlow * 0.04);

    vec2 glowCenterA = vec2(0.42 * cos(glowTime), 0.23 * sin(glowTime * 2.0));
    vec2 deltaA = materialPos - glowCenterA;
    float glowA = exp(-dot(deltaA, deltaA) * 4.5);

    vec2 glowCenterB = vec2(-0.34 * cos(glowTime * 2.0), 0.18 * sin(glowTime));
    vec2 deltaB = materialPos - glowCenterB;
    float glowB = exp(-dot(deltaB, deltaB) * 7.0);

    float flowCoordinate = dot(materialPos, normalize(vec2(0.82, -0.57)));
    float flowPosition = 0.55 * sin(glowTime);
    float flowDelta = flowCoordinate - flowPosition;
    float flowingBand = exp(-flowDelta * flowDelta * 5.5);

    float interiorLight = glowA * 0.55 + glowB * 0.28 + flowingBand * 0.42;
    return color + glow * interiorLight * GLOW_INTENSITY;
}

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
    vec3 backgroundColor = mix(background, sampleBackground(ViewportUV), eased);
    float opacity = smoothstep(0.0, 0.72, t);
    vec2 scale = logoScale * mix(INITIAL_SCALE, 1.0, eased);
    vec2 center = logoCenter - vec2(0.0, INITIAL_OFFSET_Y / resolution.y) * (1.0 - eased);
    vec2 uv = (ViewportUV - center) / scale + 0.5;
    vec2 blurStep = INITIAL_BLUR * (1.0 - eased) / resolution / scale;
    vec2 extent = blurStep * 2.0;

    if (opacity == 0.0 || any(lessThan(uv, -extent)) || any(greaterThan(uv, 1.0 + extent)))
    {
        Color = vec4(backgroundColor, 1.0);
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

    Color = vec4(backgroundColor * (1.0 - logo.a * opacity) + logo.rgb * opacity, 1.0);
}
