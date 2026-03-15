#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

out vec4 fragColor;

void main() {
    vec2 uv = fract(gl_FragCoord.xy * 0.01);
    fragColor = texture(Sampler1, uv);
}