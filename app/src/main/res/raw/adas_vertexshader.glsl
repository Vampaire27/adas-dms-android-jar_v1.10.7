precision mediump float;
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;
uniform float uTextureFlag;
uniform float aWidth;
uniform float aHeight;
mat4 matrix1 = mat4 (1.0);
vec4 matrix2 = vec4(-1.0, 1.0, 0.0, 0.0);

void main(){
    if (uTextureFlag > 0.5) {
        gl_Position = aPosition;
        vTextureCoord = aTextureCoord;
    } else {
        matrix1[0][0] = float(2.0) / float(aWidth);
        matrix1[1][1] = float(-2.0) / float(aHeight);
        gl_Position= aPosition * matrix1  + matrix2;
    }
}