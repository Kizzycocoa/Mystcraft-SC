#version 150

#moj_import <minecraft:projection.glsl>

in vec3 Position;

out vec4 texProj0;

void main() {
vec4 position = vec4(Position, 1.0);
gl_Position = ProjMat * position;
texProj0 = position;
}