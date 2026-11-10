#version 460 core

uniform vec2 resolution;
uniform float progress;
uniform sampler2D logoTexture;
uniform vec3 background;

in vec2 ViewportUV;
out vec4 Color;

const vec2 LOGO_CENTER = vec2(0.5, 0.548);
const float FACE_DURATION = 0.607;
const float SETTLE_START = 0.836;
const float SETTLE_END = 1.0;
const float GLOW_FADE_START = 0.541;
const float GLOW_FADE_END = SETTLE_END;

vec2 faceE1(int face)
{
    if (face == 0) return vec2(0.437, 0.226);
    if (face == 1) return vec2(-0.437, 0.226);
    return vec2(0.0, -0.548);
}

vec2 faceE2(int face)
{
    if (face == 0) return vec2(-0.437, 0.226);
    if (face == 1) return vec2(0.0, -0.548);
    return vec2(0.437, 0.226);
}

vec2 faceLocalUV(vec2 logoUV, int face)
{
    vec2 a = faceE1(face);
    vec2 b = faceE2(face);
    vec2 p = logoUV - LOGO_CENTER;
    float det = a.x * b.y - a.y * b.x;
    return vec2((p.x * b.y - p.y * b.x), (a.x * p.y - a.y * p.x)) / det;
}

float faceCoverage(vec2 logoUV, int face)
{
    vec2 q = faceLocalUV(logoUV, face);
    vec2 w = max(fwidth(q), vec2(0.001));
    vec2 lo = smoothstep(-w, w, q);
    vec2 hi = vec2(1.0) - smoothstep(vec2(1.0) - w, vec2(1.0) + w, q);
    return clamp(lo.x * lo.y * hi.x * hi.y, 0.0, 1.0);
}

vec4 sampleLogo(vec2 uv)
{
    if (any(lessThan(uv, vec2(0.0))) || any(greaterThan(uv, vec2(1.0)))) return vec4(0.0);
    return texture(logoTexture, vec2(uv.x, 1.0 - uv.y));
}

vec2 rotate2D(vec2 p, float angle)
{
    float c = cos(angle), s = sin(angle);
    return mat2(c, s, -s, c) * p;
}

float ease(float x)
{
    x = clamp(x, 0.0, 1.0);
    return x * x * (3.0 - 2.0 * x);
}

float easeOut(float x)
{
    x = clamp(x, 0.0, 1.0);
    return 1.0 - pow(1.0 - x, 3.0);
}

vec2 entryOffset(int face)
{
    if (face == 0) return vec2(0.0, 0.105);
    if (face == 1) return vec2(-0.115, -0.05);
    return vec2(0.115, -0.05);
}

float entryAngle(int face)
{
    if (face == 0) return -0.18;
    if (face == 1) return -0.22;
    return 0.22;
}

float startProgress(int face)
{
    if (face == 0) return 0.033;
    if (face == 1) return 0.115;
    return 0.197;
}

vec4 animateFace(vec2 screenLogoUV, int face, float t)
{
    float prog = clamp((t - startProgress(face)) / FACE_DURATION, 0.0, 1.0);
    float approach = easeOut(clamp(prog * 1.16, 0.0, 1.0));

    vec2 translation = entryOffset(face) * (1.0 - approach);
    float rotation = entryAngle(face) * (1.0 - approach);
    vec2 canonicalUV = rotate2D(screenLogoUV - LOGO_CENTER - translation, -rotation) + LOGO_CENTER;

    vec4 texel = sampleLogo(canonicalUV);
    float region = faceCoverage(canonicalUV, face);
    vec2 local = faceLocalUV(canonicalUV, face);

    float path = clamp(0.5 * local.x + 0.5 * local.y, 0.0, 1.0);
    if (face == 1) path = clamp(1.0 - (0.6 * local.x + 0.4 * local.y), 0.0, 1.0);
    if (face == 2) path = clamp(0.35 * local.x + 0.65 * local.y, 0.0, 1.0);
    float front = ease(clamp((prog * 1.15 - path + 0.05) / 0.12, 0.0, 1.0));

    float inside = min(min(local.x, local.y), min(1.0 - local.x, 1.0 - local.y));
    float edgeBand = 1.0 - smoothstep(0.045, 0.1, inside);
    float fillIn = ease(clamp((prog - 0.37) / 0.52, 0.0, 1.0));
    float visibility = region * texel.a * max(edgeBand * front, fillIn);

    float light = (1.0 - fillIn) * edgeBand * front * 0.74;
    vec3 rgb = mix(texel.rgb, vec3(0.96, 0.97, 1.0), light);
    return vec4(rgb * visibility, visibility);
}

float faceGlow(vec2 screenLogoUV, int face, float t)
{
    float prog = clamp((t - startProgress(face)) / FACE_DURATION, 0.0, 1.0);
    if (prog <= 0.001) return 0.0;
    float approach = easeOut(clamp(prog * 1.16, 0.0, 1.0));
    vec2 q = rotate2D(screenLogoUV - LOGO_CENTER - entryOffset(face) * (1.0 - approach), -entryAngle(face) * (1.0 - approach)) + LOGO_CENTER;
    vec2 v = faceLocalUV(q, face);
    float edge = min(min(v.x, v.y), min(1.0 - v.x, 1.0 - v.y));
    float front = clamp(prog * 1.5, 0.0, 1.0);
    return (0.12 * exp(-18.0 * abs(edge)) + 0.06 * exp(-7.0 * abs(edge))) * front;
}

void main()
{
    float scale = resolution.y * 0.4;
    vec2 logoUV = (ViewportUV - 0.5) * resolution / scale + 0.5;
    float t = clamp(progress, 0.0, 1.0);

    vec4 texel = sampleLogo(logoUV);
    vec4 original = vec4(texel.rgb * texel.a, texel.a);

    vec4 logo = original;
    float glow = 0.0;

    if (t < SETTLE_END)
    {
        vec4 assembled = vec4(0.0);
        for (int i = 0; i < 3; ++i)
        {
            vec4 piece = animateFace(logoUV, i, t);
            assembled.rgb += (1.0 - assembled.a) * piece.rgb;
            assembled.a += (1.0 - assembled.a) * piece.a;
            glow += faceGlow(logoUV, i, t);
        }

        float settle = ease((t - SETTLE_START) / (SETTLE_END - SETTLE_START));
        logo = mix(assembled, original, settle);

        float glowStrength = 1.0 - ease((t - GLOW_FADE_START) / (GLOW_FADE_END - GLOW_FADE_START));
        glow *= glowStrength;
    }

    vec3 rgb = background * (1.0 - logo.a) + logo.rgb;
    rgb += glow * vec3(0.57, 0.74, 1.0);
    Color = vec4(rgb, 1.0);
}
