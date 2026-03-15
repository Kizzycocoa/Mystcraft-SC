#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 skyUv = fract(vertexColor.xy * 0.25);
    vec2 portalUv = fract(vertexColor.zw);

    vec3 sky = texture(Sampler0, skyUv).rgb;
    vec3 portal = texture(Sampler1, portalUv).rgb;

    vec3 color = sky * 0.20 + portal * 0.90;
    fragColor = vec4(color, 1.0);
}