precision mediump float;
varying lowp vec2 vTextureCoord;
uniform sampler2D samplerY;
uniform sampler2D samplerU;
uniform sampler2D samplerV;
uniform sampler2D samplerFS;
uniform float uTextureFlag;
uniform  vec4 uColor;

const  mat3 yuv2rgb = mat3(
    1.0,        1.0,        1.0,
    0.0,        -0.39465,   2.03211,
    1.13983,    -0.58060,   0.0);

 vec4 getTextureColor(in  vec2 coord){
    vec3 yuv = vec3(
        texture2D(samplerY, coord).r,
        texture2D(samplerU, coord).r - 0.5,
        texture2D(samplerV, coord).r - 0.5);

    vec3 bgColor = yuv2rgb * yuv;
    return vec4(bgColor, 1.0);
}

void main()
{
    if (uTextureFlag > 0.5) {
        gl_FragColor = getTextureColor(vTextureCoord);
    } else {
        gl_FragColor = uColor;
    }
}
