precision mediump float;
uniform sampler2D sTexture;//纹理内容数据
//接收从顶点着色器过来的参数
varying vec2 vTextureCoord;

void main()                         
{    
   //将计算出的颜色给此片元
   vec4 finalColor=texture2D(sTexture, vTextureCoord);    
   gl_FragColor = finalColor;
}   
