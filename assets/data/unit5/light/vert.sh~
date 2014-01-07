uniform mat4 uMVPMatrix;
uniform float uStartAngle;
uniform float uWidthSpan;
attribute vec3 aPosition;
attribute vec2 aTexCoor;
varying vec2 vTextureCoord;

void main(){

	float angleSpanH = 4.0 * 3.1415926;		// 横向跨度所代表的角度  4 pai
	float startX = -uWidthSpan/2.0; 		// 起始的 x 坐标  uWidthSpance为在世界坐标系的宽
	float curAngle = uStartAngle + ((aPosition.x - startX)/uWidthSpan)* angleSpanH;
	float tz = sin(curAngle) * 0.1;			// 把正弦值缩小 10 倍作为 z 值
	
	//根据总变换矩阵来的到顶点位置
	
	gl_Position = uMVPMatrix * vec4(aPosition.x,aPosition.y, tz,1);
	

	//把纹理坐标传入片元着色器
	vTextureCoord = aTexCoor;
}
