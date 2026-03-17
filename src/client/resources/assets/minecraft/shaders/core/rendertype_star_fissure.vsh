#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in vec3 Position;

out vec4 texProj0;
out vec3 viewPos;
out vec3 localPos;
out vec4 clipPos;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;

void main() {
vec4 modelViewPos = ModelViewMat * vec4(Position, 1.0);
vec4 projectedPos = ProjMat * modelViewPos;

gl_Position = projectedPos;

// Projected coordinate basis for the fragment shader.
texProj0 = projection_from_position(projectedPos);

// View-space position, useful for parallax approximations.
viewPos = modelViewPos.xyz;

// Local/object-space position, useful if we need a cleaner per-face basis later.
localPos = Position;

// Full clip-space position, available for any future screen/projection comparisons.
clipPos = projectedPos;

sphericalVertexDistance = fog_spherical_distance(Position);
cylindricalVertexDistance = fog_cylindrical_distance(Position);
}