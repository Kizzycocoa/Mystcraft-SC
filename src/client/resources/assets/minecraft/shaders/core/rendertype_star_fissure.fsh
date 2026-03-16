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

float legacy_brightness(int passIndex) {
if (passIndex == 0) return 0.1;
return 1.0 - float(passIndex) * 0.1; // 0.9 .. 0.3
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

float legacy_hash(float n) {
return fract(sin(n) * 43758.5453123);
}

vec3 legacy_color(int passIndex, float faceSeed) {
if (passIndex == 0) {
// Legacy pass 0 was white * 0.1
return vec3(0.10, 0.10, 0.10);
}

float i = float(passIndex);
float f7 = legacy_brightness(passIndex);

// Still an approximation of Random(31100L), because GLSL has no Java RNG state.
float base = 31100.0 + faceSeed * 1000.0;

float r = (legacy_hash(base + i * 3.0 + 0.0) * 0.5 + 0.1) * f7;
float g = (legacy_hash(base + i * 3.0 + 1.0) * 0.5 + 0.4) * f7;
float b = (legacy_hash(base + i * 3.0 + 2.0) * 0.5 + 0.5) * f7;

return vec3(r, g, b);
}

vec2 legacy_transform(vec2 uv, int passIndex, float timeScroll) {
// Closest practical match to legacy order:
// scroll -> scale -> center rotate -> camera-relative shift

uv.y += timeScroll;
uv *= legacy_scale(passIndex);
uv = rotate_around_center(uv, legacy_angle_degrees(passIndex));

float depth = legacy_depth_factor(passIndex);

// Remove the non-legacy bias factor. Just use plain per-pass depth separation.
uv += vec2(viewPos.x, viewPos.z) / depth;

return uv;
}

void main() {
vec2 baseUv = texProj0.xy / texProj0.w;

// Closest simple stand-in for:
// (System.currentTimeMillis() % 700000L) / 200000F
float timeScroll = mod(GameTime * 0.00025, 3.5);

// Still only an approximation of top/bottom divergence.
float faceSeed = step(0.0, viewPos.y);

// Pass 0: sky
vec2 skyUv = fract(legacy_transform(baseUv, 0, timeScroll));
vec3 color = texture(Sampler0, skyUv).rgb * legacy_color(0, faceSeed);

// Passes 1-7: portal
for (int i = 1; i <= 7; i++) {
vec2 portalUv = fract(legacy_transform(baseUv, i, timeScroll));
vec3 portalSample = texture(Sampler1, portalUv).rgb;
color += portalSample * legacy_color(i, faceSeed);
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