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
return 16.0 - float(passIndex);
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
uv.y += timeScroll;
uv *= legacy_scale(passIndex);
uv = rotate_around_center(uv, legacy_angle_degrees(passIndex));

float depth = legacy_depth_factor(passIndex);
uv += vec2(viewPos.x, viewPos.z) / depth;

return uv;
}

float legacy_hash(float n) {
return fract(sin(n) * 43758.5453123);
}

vec3 legacy_color(int passIndex, float faceSeed) {
if (passIndex == 0) {
return vec3(0.10, 0.10, 0.10);
}

float i = float(passIndex);
float f7 = 1.0 - i * 0.1;

float base = 31100.0 + faceSeed * 1000.0;

float r = (legacy_hash(base + i * 3.0 + 0.0) * 0.5 + 0.1) * f7;
float g = (legacy_hash(base + i * 3.0 + 1.0) * 0.5 + 0.4) * f7;
float b = (legacy_hash(base + i * 3.0 + 2.0) * 0.5 + 0.5) * f7;

return vec3(r, g, b);
}

float legacy_portal_weight(int passIndex) {
// Slight emphasis on earlier portal passes, taper toward later ones.
if (passIndex == 1) return 1.35;
if (passIndex == 2) return 1.20;
if (passIndex == 3) return 1.10;
if (passIndex == 4) return 1.00;
if (passIndex == 5) return 0.90;
if (passIndex == 6) return 0.82;
return 0.75;
}

void main() {
vec2 baseUv = texProj0.xy / texProj0.w;

// Still GameTime for now; wall-clock comes later.
float timeScroll = GameTime * 0.03;

// Approximate per-face divergence.
float faceSeed = step(0.0, viewPos.y);

// Pass 0: faint sky base, treated more like the old alpha-blended backdrop.
vec2 skyUv = fract(legacy_transform(baseUv, 0, timeScroll));
vec3 sky = texture(Sampler0, skyUv).rgb * legacy_color(0, faceSeed);

vec3 color = sky;

// Passes 1-7: portal passes, treated more aggressively like additive buildup.
for (int i = 1; i < PORTAL_LAYERS; i++) {
vec2 portalUv = fract(legacy_transform(baseUv, i, timeScroll));
vec3 portal = texture(Sampler1, portalUv).rgb;
vec3 tinted = portal * legacy_color(i, faceSeed) * legacy_portal_weight(i);

color += tinted;
}

// Gentle additive-style bloom/compression so bright portal layers feel
// more like stacked GL ONE,ONE passes instead of flat summed texture math.
color = 1.0 - exp(-color * 1.15);

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