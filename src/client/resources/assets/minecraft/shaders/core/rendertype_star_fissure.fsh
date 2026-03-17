#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float time;

in vec4 texProj0;
in vec3 viewPos;
in vec3 localPos;
in vec4 clipPos;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

// Safety fallbacks so the shader still preprocesses if a define is missing.
#ifndef STAR_FISSURE_FACE_INDEX
#define STAR_FISSURE_FACE_INDEX 0
#endif

#ifndef STAR_FISSURE_PASS_INDEX
#define STAR_FISSURE_PASS_INDEX 0
#endif

#ifndef STAR_FISSURE_IS_SKY_PASS
#define STAR_FISSURE_IS_SKY_PASS 0
#endif

float legacy_scale(int passIndex) {
if (passIndex == 0) return 0.125;
if (passIndex == 1) return 0.5;
return 0.04;
}

float legacy_angle_degrees(int passIndex) {
if (passIndex == 0) return 0.0;
float i = float(passIndex);
return (i * i + i * 9.0) * 2.0;
}

float legacy_depth_factor(int passIndex) {
if (passIndex == 0) return 65.0;
return 16.0 - float(passIndex); // 15,14,13,12,11,10,9
}

vec2 rotate_around_center(vec2 uv, float degrees) {
float r = radians(degrees);
float s = sin(r);
float c = cos(r);

uv -= vec2(0.5, 0.5);
uv = mat2(c, -s, s, c) * uv;
uv += vec2(0.5, 0.5);

return uv;
}

vec2 legacy_transform(vec2 uv, int passIndex, float timeScroll) {
// Closest practical match to legacy order:
// scroll -> scale -> center rotate -> camera-relative shift
uv.y += timeScroll;
uv *= legacy_scale(passIndex);
uv = rotate_around_center(uv, legacy_angle_degrees(passIndex));

float depth = legacy_depth_factor(passIndex);

// Best current approximation of the old texture-space camera-relative shift.
uv += vec2(viewPos.x, viewPos.z) / depth;

return uv;
}

// Exact precomputed legacy colors from the old Java Random(31100L) pass sequence.
// Index 0 is still the sky pass, which was effectively white * 0.1.
const vec3 LEGACY_FACE0[8] = vec3[](
vec3(0.10, 0.10, 0.10),
vec3(0.13252318, 0.59039604, 0.66490543),
vec3(0.08086246, 0.65228519, 0.60850000),
vec3(0.15476325, 0.56945664, 0.56182394),
vec3(0.20953748, 0.49447209, 0.51676930),
vec3(0.22715426, 0.41193486, 0.34016165),
vec3(0.16577888, 0.22592661, 0.32147998),
vec3(0.15267149, 0.20158945, 0.29948416)
);

const vec3 LEGACY_FACE1[8] = vec3[](
vec3(0.10, 0.10, 0.10),
vec3(0.48257232, 0.76289242, 0.45076700),
vec3(0.42460871, 0.52457442, 0.78076272),
vec3(0.30782203, 0.34709205, 0.58977049),
vec3(0.32043922, 0.33186763, 0.35659590),
vec3(0.12250997, 0.42583143, 0.41263591),
vec3(0.23611890, 0.17147928, 0.25763466),
vec3(0.03546074, 0.23650339, 0.24147770)
);

vec3 legacy_color(int passIndex) {
#if STAR_FISSURE_FACE_INDEX == 0
    return LEGACY_FACE0[passIndex];
#else
    return LEGACY_FACE1[passIndex];
#endif
}

void main() {
float timeScroll = time;
int passIndex = STAR_FISSURE_PASS_INDEX;

vec2 baseUv = texProj0.xy / texProj0.w;

vec2 uv = fract(legacy_transform(baseUv, passIndex, timeScroll));

#if STAR_FISSURE_IS_SKY_PASS == 1
    vec3 color = texture(Sampler0, uv).rgb * 0.10;
#else
    vec3 color = texture(Sampler1, uv).rgb * legacy_color(passIndex);
#endif

fragColor = apply_fog(
vec4(color, 1.0),
sphericalVertexDistance,
cylindricalVertexDistance,
FogEnvironmentalStart,
FogEnvironmentalEnd,
FogRenderDistanceStart,
FogRenderDistanceEnd,
FogColor
);
}