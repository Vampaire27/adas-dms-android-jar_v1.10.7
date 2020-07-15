precision mediump float;
varying lowp vec2 vTextureCoord;
uniform sampler2D samplerY;
uniform sampler2D samplerUV;
uniform sampler2D samplerFS;
uniform float uTextureFlag;
uniform  vec4 uColor;


const  mat3 yuv2rgb = mat3(
    1.0,        1.0,        1.0,
    0.0,        -0.39465,   2.03211,
    1.13983,    -0.58060,   0.0);

const  mat3 bgmat = mat3(0.8);
const  mat3 fsmat = mat3(0.2);
const  vec3 fsColor = vec3(0.2, 0.8, 0.2) * fsmat;

 vec4 getTextureColor(in  vec2 coord){
    vec3 yuv = vec3(
        texture2D(samplerY, coord).r,
        texture2D(samplerUV, coord).a - 0.5,
        texture2D(samplerUV, coord).r - 0.5);
    vec3 bgColor = yuv2rgb * yuv;
    if (texture2D(samplerFS, coord).r > 0.5) {
        return vec4(bgmat * bgColor + fsColor, 1.0);
    } else {
        return vec4(bgColor, 1.0);
    }
}
void main()
{
    if (uTextureFlag > 0.5) {
        gl_FragColor = getTextureColor(vTextureCoord);
    } else {
        gl_FragColor = uColor;
    }
}
