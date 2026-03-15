#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 baseUv = vertexColor.xy;

    float scrollHi = round(vertexColor.z * 255.0);
    float scrollLo = round(vertexColor.w * 255.0);
    float scroll = (scrollHi * 256.0 + scrollLo) / 65535.0;

    vec2 skyUv = fract(baseUv * 0.25);
    vec2 portalUv = fract(baseUv * 8.0 + vec2(0.0, scroll));

    vec3 sky = texture(Sampler0, skyUv).rgb;
    vec3 portal = texture(Sampler1, portalUv).rgb;

    vec3 color = sky * 0.20 + portal * 0.90;
    fragColor = vec4(color, 1.0);
}