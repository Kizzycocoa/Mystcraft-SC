#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 texProj0;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

// Closest practical approximation to the old deterministic 1.12 tint stack.
// Pass 0 = faint white sky.
// Passes 1-7 = portal passes based on the old seeded random ranges * colorShift.
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
if (passIndex == 0) return 0.125; // sky pass
if (passIndex == 1) return 0.5;   // first portal pass
return 0.04;                      // remaining portal passes
}

float legacy_angle_degrees(int passIndex) {
if (passIndex == 0) return 0.0;
float i = float(passIndex);
return (i * i + i * 9.0) * 2.0;
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
float scale = legacy_scale(passIndex);
float angle = legacy_angle_degrees(passIndex);

// Old shader scrolled in Y, then scaled, then rotated around center.
uv.y += timeScroll;
uv *= scale;
uv = rotate_around_center(uv, angle);

return uv;
}

void main() {
// The modern equivalent of the old projected/texgen coords.
vec2 baseUv = texProj0.xy / texProj0.w;

// Old code used wall-clock time:
// (System.currentTimeMillis() % 700000L) / 200000F
// We don't have wall-clock in shader, so use GameTime as the nearest modern equivalent.
// This constant is deliberately slow so it behaves more like the old long-cycle drift.
float timeScroll = GameTime * 0.03;

vec3 color = vec3(0.0);

// Pass 0: end_sky, faint
vec2 skyUv = fract(legacy_transform(baseUv, 0, timeScroll));
color += texture(Sampler0, skyUv).rgb * LEGACY_COLORS[0];

// Passes 1-7: end_portal, additive-style accumulation
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