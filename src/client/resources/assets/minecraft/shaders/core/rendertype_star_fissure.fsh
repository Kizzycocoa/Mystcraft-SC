#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 uv = vertexColor.xy;
    fragColor = texture(Sampler1, uv);
}