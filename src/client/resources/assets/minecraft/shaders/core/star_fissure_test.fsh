#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 vertexColor;
out vec4 fragColor;

void main() {
    fragColor = vec4(0.2, 0.0, 0.4, 1.0);
}