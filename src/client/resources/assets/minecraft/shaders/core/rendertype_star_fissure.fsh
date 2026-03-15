#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 uv = vertexColor.xy;

    vec2 skyUv = fract(uv * 0.25);
    vec2 portalUv = fract(uv);

    vec4 sky = texture(Sampler0, skyUv);
    vec4 portal = texture(Sampler1, portalUv);

    vec3 color = sky.rgb * 0.35 + portal.rgb * 0.9;
    fragColor = vec4(color, 1.0);
}