precision mediump float;
varying vec2 vTextureCoord;
//纹理采样器，代表一幅纹理
uniform sampler2D sTexture;
void main() {
	gl_FragColor = texture2D(sTexture, vTextureCoord);
}