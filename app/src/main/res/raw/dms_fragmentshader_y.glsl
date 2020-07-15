varying lowp vec2 vTextureCoord;
uniform sampler2D samplerY;
uniform sampler2D samplerU;
uniform sampler2D samplerV;
uniform sampler2D samplerRGB;
uniform mediump float uTextureFlag;

const mediump mat3 yuv2rgb = mat3(
    1.0,        1.0,        1.0,
    0.0,        -0.39465,   2.03211,
    1.13983,    -0.58060,   0.0);

void main()
{
    if(uTextureFlag == 0.0){
        // GRAY
        mediump float y;
        y = texture2D(samplerY,vTextureCoord).r;
        gl_FragColor = vec4(y, y, y, 1);
    }else if(uTextureFlag == 1.0){
        // YV21
        mediump vec3 yuv = vec3(
        texture2D(samplerY,vTextureCoord).r - 16.0/255.0,
        texture2D(samplerU,vTextureCoord).r - 128.0/255.0,
        texture2D(samplerV,vTextureCoord).r - 128.0/255.0);
        mediump vec3 bgColor = yuv2rgb * yuv;
        gl_FragColor = vec4(bgColor,1.0);
    }else if(uTextureFlag == 4.0){
        // RGB
        gl_FragColor = texture2D(samplerRGB,vTextureCoord);
    }

}