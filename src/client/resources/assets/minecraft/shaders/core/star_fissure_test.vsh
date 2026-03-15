#version 150

#moj_import <projection.glsl>

in vec3 Position;
in vec4 Color;

out vec4 vertexColor;

void main() {
gl_Position = ProjMat * vec4(Position, 1.0);
vertexColor = Color;
}