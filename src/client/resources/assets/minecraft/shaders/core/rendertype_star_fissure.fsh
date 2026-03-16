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

const vec3 LEGACY_COLORS[8] = vec3[](
vec3(0.10, 0.10, 0.10),
vec3(0.50, 0.54, 0.74),
vec3(0.40, 0.64, 0.72),
vec3(0.33, 0.56, 0.60),
vec3(0.18, 0.40, 0.54),
vec3(0.12, 0.28, 0.38),
vec3(0.08, 0.22, 0.26),
vec3(0.05, 0.15, 0.18)
);

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
// Match legacy order as closely as possible:
// scroll -> scale -> center rotate -> camera-relative translation
uv.y += timeScroll;
uv *= legacy_scale(passIndex);
uv = rotate_around_center(uv, legacy_angle_degrees(passIndex));

float depth = legacy_depth_factor(passIndex);

// First pass at legacy parallax approximation:
// use view-space x/z so camera movement shifts the layers.
uv += vec2(viewPos.x, viewPos.z) / depth;

return uv;
}

void main() {
vec2 baseUv = texProj0.xy / texProj0.w;

// Keep the current slow modern approximation for now.
float timeScroll = GameTime * 0.03;

vec3 color = vec3(0.0);

vec2 skyUv = fract(legacy_transform(baseUv, 0, timeScroll));
color += texture(Sampler0, skyUv).rgb * LEGACY_COLORS[0];

for (int i = 1; i < PORTAL_LAYERS; i++) {
vec2 portalUv = fract(legacy_transform(baseUv, i, timeScroll));
color += texture(Sampler1, portalUv).rgb * LEGACY_COLORS[i];
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