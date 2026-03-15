#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:matrix.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 texProj0;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

const vec3[] COLORS = vec3[](
vec3(0.022087, 0.098399, 0.110818),
vec3(0.011892, 0.095924, 0.089485),
vec3(0.027636, 0.101689, 0.100326),
vec3(0.046564, 0.109883, 0.114838),
vec3(0.064901, 0.117696, 0.097189),
vec3(0.063761, 0.086895, 0.123646),
vec3(0.084817, 0.111994, 0.166380),
vec3(0.097489, 0.154120, 0.091064)
);

const mat4 SCALE_TRANSLATE = mat4(
0.5, 0.0, 0.0, 0.25,
0.0, 0.5, 0.0, 0.25,
0.0, 0.0, 1.0, 0.0,
0.0, 0.0, 0.0, 1.0
);

mat4 end_portal_layer(float layer) {
mat4 translate = mat4(
1.0, 0.0, 0.0, 17.0 / layer,
0.0, 1.0, 0.0, (2.0 + layer / 1.5) * (GameTime * 1.5),
0.0, 0.0, 1.0, 0.0,
0.0, 0.0, 0.0, 1.0
);

mat2 rotate = mat2_rotate_z(radians((layer * layer * 4321.0 + layer * 9.0) * 2.0));

float s = (4.5 - layer / 4.0) * 2.0;
mat2 scaledRot = mat2(
rotate[0][0] * s, rotate[0][1] * s,
rotate[1][0] * s, rotate[1][1] * s
);

mat4 layerMat = mat4(
scaledRot[0][0], scaledRot[0][1], 0.0, 0.0,
scaledRot[1][0], scaledRot[1][1], 0.0, 0.0,
0.0,            0.0,            1.0, 0.0,
0.0,            0.0,            0.0, 1.0
);

return layerMat * translate * SCALE_TRANSLATE;
}

void main() {
vec3 color = textureProj(Sampler0, texProj0).rgb * COLORS[0];

for (int i = 0; i < PORTAL_LAYERS; i++) {
color += textureProj(Sampler1, texProj0 * end_portal_layer(float(i + 1))).rgb * COLORS[i];
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