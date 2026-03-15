#version 150

#moj_import <projection.glsl>

in vec3 Position;
in vec4 Color;

out vec4 vertexColor;

void main() {
vec4 worldPos = vec4(Position + ChunkOffset, 1.0);
gl_Position = ProjMat * worldPos;
vertexColor = Color;
}