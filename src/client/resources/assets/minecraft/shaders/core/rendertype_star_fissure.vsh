#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in vec3 Position;

out vec4 texProj0;
out vec3 viewPos;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;

void main() {
vec4 mvPos = ModelViewMat * vec4(Position, 1.0);
gl_Position = ProjMat * mvPos;

texProj0 = projection_from_position(gl_Position);
viewPos = mvPos.xyz;

sphericalVertexDistance = fog_spherical_distance(Position);
cylindricalVertexDistance = fog_cylindrical_distance(Position);
}