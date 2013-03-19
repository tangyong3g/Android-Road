precision mediump float;
varying vec4 vColor;	//接收从顶点着色器传递过来的颜色
void main() {
	gl_FragColor = vColor;
}
