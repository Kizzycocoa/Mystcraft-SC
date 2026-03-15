#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float GameTime;

in vec4 vertexColor;

out vec4 fragColor;

vec2 rotateAroundCenter(vec2 uv, float angle) {
    float s = sin(angle);
    float c = cos(angle);

    uv -= vec2(0.5, 0.5);
    uv = mat2(c, -s, s, c) * uv;
    uv += vec2(0.5, 0.5);

    return uv;
}

void main() {
    vec2 uv = vertexColor.xy;

    float t = GameTime * 0.2;

    vec2 skyUv = fract(uv * 0.25 + vec2(t * 0.02, t * 0.01));

    vec2 portalUv1 = fract(uv + vec2(0.0, t * 0.08));

    vec2 portalUv2 = fract(rotateAroundCenter(uv, t * 0.35) * 1.6 + vec2(t * -0.03, t * 0.05));

    vec3 sky = texture(Sampler0, skyUv).rgb;
    vec3 portal1 = texture(Sampler1, portalUv1).rgb;
    vec3 portal2 = texture(Sampler1, portalUv2).rgb;

    vec3 color = sky * 0.25
    + portal1 * 0.65
    + portal2 * 0.45;

    fragColor = vec4(color, 1.0);
}