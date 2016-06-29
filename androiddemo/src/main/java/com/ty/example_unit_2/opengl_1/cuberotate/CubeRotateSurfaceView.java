package com.ty.example_unit_2.opengl_1.cuberotate;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import com.sny.tangyong.androiddemo.R;
import com.ty.example_unit_2.opengl_1.cube.Cube;
import com.ty.util.Utils;

/**
 * 
 * @author tangyong
 * 
 */
public class CubeRotateSurfaceView extends GLSurfaceView {

	public CubeRotateSurfaceView(Context context) {
		super(context);
		setRenderer(new CuberOtateRender(context));
	}
	
	/**
	 * 
	 * @author tangyong
	 * 
	 */
	class CuberOtateRender implements Renderer {

		Cube cube = null;
		private float angle = 0;
		IntBuffer textureIdBuffer ;
		Context mContext;

		public CuberOtateRender(Context context) {
			cube = new Cube();
			mContext = context;
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			Log.i("tyler.tang","onDraw");
			// 设置色彩　
			gl.glColor4f(1f, 1, 1, 1);
			// 清除缓存
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			
			// 回到原点
			gl.glLoadIdentity();
			gl.glTranslatef(0, 0, -9);

			gl.glEnable(GL10.GL_LINE_SMOOTH);
			// 打开管线顶点客户端状态，纹理坐标状态
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glEnable(GL10.GL_TEXTURE_2D);
			
			gl.glPushMatrix();
			gl.glRotatef(angle, 0, 1, 0);
			cube.draw(gl,textureIdBuffer.get(0));
			gl.glPopMatrix();

			// 关闭管线　
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glDisable(GL10.GL_LINE_SMOOTH);
			gl.glDisable(GL10.GL_TEXTURE_2D);
			angle += 0.8f;
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			// 当大小变化的时候会给调用
			float ratio = (float) width / height;
			// 设置视口
			gl.glViewport(0, 0, width, height);
			// 接下来要处理的是投影矩阵
			gl.glMatrixMode(GL10.GL_PROJECTION);
			// 设置透视投影　左，右，下，上 ,近，远
			gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
			// 接下来要处理的是模型矩阵
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			// 平面还是平滑着色
			gl.glShadeModel(GL10.GL_SMOOTH);
			// 指定色彩缓冲区
			gl.glClearColor(0, 0, 0, 0);
			// 设置深度缓存,进行跟踪
			gl.glClearDepthf(1.0f);
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glDepthFunc(GL10.GL_LEQUAL);
			// gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
			initTexture(gl,0);
		}

		/**
		 * 初始化纹理　
		 */
		private void initTexture(GL10 gl ,int index) {
			
			textureIdBuffer = IntBuffer.allocate(6);
			gl.glGenTextures(6, textureIdBuffer);
			
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIdBuffer.get(0));
			
			//设置纹理参数
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
					GL10.GL_LINEAR);
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
					GL10.GL_LINEAR);
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
					GL10.GL_CLAMP_TO_EDGE);
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
					GL10.GL_CLAMP_TO_EDGE);
			
			
			Bitmap texture = null;
			if(index == 0){
				texture = Utils.getTextureFromBitmapResource(mContext, R.drawable.ic_launcher);
			}else if(index ==1 ){
//				texture = Utils.getTextureFromBitmapResource(context, R.drawable.gallery_photo_2);
			}else if(index ==2){
//				texture = Utils.getTextureFromBitmapResource(context, R.drawable.gallery_photo_3);
			}
			else if(index ==3){
//				texture = Utils.getTextureFromBitmapResource(context, R.drawable.gallery_photo_4);
			}
			else if(index ==4){
//				texture = Utils.getTextureFromBitmapResource(context, R.drawable.gallery_photo_5);
			}
			else if(index ==5){
//				texture = Utils.getTextureFromBitmapResource(context, R.drawable.gallery_photo_6);
			}
			//GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, BitmapFactory.decodeResource(context.getResources(), R.drawable.nehe), 0);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, texture, 0);
			texture.recycle();
		}

	}

	
	

}
