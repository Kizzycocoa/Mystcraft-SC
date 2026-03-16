#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 texProj0;
in vec3 viewPos;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

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
// Legacy order approximation:
// scroll -> scale -> center rotate -> camera-relative shift
uv.y += timeScroll;
uv *= legacy_scale(passIndex);
uv = rotate_around_center(uv, legacy_angle_degrees(passIndex));

float depth = legacy_depth_factor(passIndex);
uv += vec2(viewPos.x, viewPos.z) / depth;

return uv;
}

// Deterministic pseudo-random, standing in for the old Random(31100L).
float legacy_hash(float n) {
return fract(sin(n) * 43758.5453123);
}

vec3 legacy_color(int passIndex) {
// Pass 0 was forced white, then dimmed to 0.1
if (passIndex == 0) {
return vec3(0.10, 0.10, 0.10);
}

float i = float(passIndex);

// Old f7 brightness curve:
// pass 1 = 0.9, pass 2 = 0.8, ... pass 7 = 0.3
float f7 = 1.0 - i * 0.1;

// Approximate the old deterministic random ranges:
// R: 0.1 .. 0.6
// G: 0.4 .. 0.9
// B: 0.5 .. 1.0
float r = (legacy_hash(31100.0 + i * 3.0 + 0.0) * 0.5 + 0.1) * f7;
float g = (legacy_hash(31100.0 + i * 3.0 + 1.0) * 0.5 + 0.4) * f7;
float b = (legacy_hash(31100.0 + i * 3.0 + 2.0) * 0.5 + 0.5) * f7;

return vec3(r, g, b);
}

void main() {
vec2 baseUv = texProj0.xy / texProj0.w;

// Still using GameTime for now; wall-clock comes later.
float timeScroll = GameTime * 0.03;

vec3 color = vec3(0.0);

// Pass 0: sky
vec2 skyUv = fract(legacy_transform(baseUv, 0, timeScroll));
color += texture(Sampler0, skyUv).rgb * legacy_color(0);

// Passes 1-7: portal
for (int i = 1; i < PORTAL_LAYERS; i++) {
vec2 portalUv = fract(legacy_transform(baseUv, i, timeScroll));
vec3 portal = texture(Sampler1, portalUv).rgb;

// Slight boost to mimic old additive multipass feel
color += portal * legacy_color(i) * 1.35;
}

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