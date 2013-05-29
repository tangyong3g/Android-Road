package com.ty.example_unit_2.opengl_2.loadmodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.MotionEvent;

import com.ty.util.MatrixState;

/**
 * 
 * @author tangyong
 * 
 */
public class ModelView extends GLSurfaceView {
	
	
	private static String TAG = "ModelView";
	private float mAngle = 0.0f;

	SenceRender mRenderer;
	private final float TOUCH_SCALE_FACTOR = 180.0f / 320;// 角度缩放比例
	private float mPreviousY;// 上次的触控位置Y坐标
	private float mPreviousX;// 上次的触控位置X坐标
	LoadedObjectVertexOnly obj = null;
	ModelControler modelControler;
	ControlerThread modelThread;
	
	public ModelView(Context context) {
		
		super(context);
		// 设置使用OPENGL ES2.0
		this.setEGLContextClientVersion(2); 
		// 创建场景渲染器
		mRenderer = new SenceRender();
		// 设置渲染器
		setRenderer(mRenderer);
		// 设置渲染模式为主动渲染
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		
		modelControler = new ModelControler(obj,this);
	}
	
	
	public SenceRender getRender(){
		
		return mRenderer;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		//mySensormanager.unregisterListener(modelControler);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		/*
		mySensormanager.registerListener
		(
				modelControler,         //eventListener 
				sensor,       // sensor
				SensorManager.SENSOR_DELAY_GAME   //delay type
        );*/
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
	  float y = e.getY();
        float x = e.getX();
        switch (e.getAction()) {
        case MotionEvent.ACTION_MOVE:
            float dy = y - mPreviousY;//计算触控笔Y位移
            float dx = x - mPreviousX;//计算触控笔X位移
            mRenderer.yAngle += dx * TOUCH_SCALE_FACTOR;//设置沿x轴旋转角度
            mRenderer.xAngle+= dy * TOUCH_SCALE_FACTOR;//设置沿z轴旋转角度
            requestRender();//重绘画面
        }
        mPreviousY = y;//记录触控笔位置
        mPreviousX = x;//记录触控笔位置
        return true;
	}

	/**
	 * 
	 * @author tangyong
	 * 
	 */
	class SenceRender implements Renderer {

		LoadedObjectVertexOnly lovo;
		LoadedObjectVertexOnly cube;
		int mTextureId;
		int mCubeId;
		float yAngle;// 绕Y轴旋转的角度
		float xAngle; // 绕Z轴旋转的角度

		float mAngle;
		
		public LoadedObjectVertexOnly getObject(){
			return lovo;
		}

		long start = 0;
		long end = 0;
		int index = 0;
	

		int frame = 0;
		long time = 0;
		long timebase = 0;
		
		
		@Override
		public void onDrawFrame(GL10 gl) {
			//清除深度缓冲与颜色缓冲
            GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            MatrixState.pushMatrix();
            
            //若加载的物体部位空则绘制物体
            if(lovo!=null)
            {
            	lovo.drawSelf(mTextureId);
            }   
            MatrixState.popMatrix();      
            
            MatrixState.pushMatrix();
            
            if(cube!=null){
            	MatrixState.translate(0, 200, 0);
            	cube.drawSelf(mCubeId);
            }
            MatrixState.popMatrix();
            
            if(index == 0){
            	start = Calendar.getInstance().getTimeInMillis();
            }
            index++;
            long rs = Calendar.getInstance().getTimeInMillis()- start;
            if(rs  > 1000 ){
            	
            	 Log.i("tyler.tang","FPS:\t"+index);
            	 index = 0;
            }
            
		}

		
		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			// 设置视窗大小及位置
			GLES20.glViewport(0, 0, width, height);
			// 计算GLSurfaceView的宽高比
			float ratio = (float) width / height;
			// 调用此方法计算产生透视投影矩阵
			MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 1, 8000);
			// 调用此方法产生摄像机9参数位置矩阵
			MatrixState.setCamera
			(
					0, 0, 300,
					0f, 0f, -1f,
					0f, 1.0f, 0.0f
            );
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			// 打开深度测试
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			// 打开背面剪
			GLES20.glDisable(GLES20.GL_CULL_FACE);
			// 初始化矩阵
			MatrixState.setInitStack();
			// 加载要绘制的物体 实际上就是把文件内容解析然后加入 顶点缓冲中去
			lovo = LoadUtil.loadFromFile("data/unit2/model/ground.obj",ModelView.this.getResources(), ModelView.this);
			cube = LoadUtil.loadFromFile("data/unit2/model/cube.obj",ModelView.this.getResources(), ModelView.this);
			
			mTextureId = initTexture(com.example.android_begin_gl_3d.R.drawable.ground_texture);
			mCubeId = initTexture(com.example.android_begin_gl_3d.R.drawable.cube_texture);
			modelControler.setLoadModelFinished(true);
		}
	}

	public int initTexture(int drawableId)// textureId
	{
		// 生成纹理ID
		int[] textures = new int[1];
		GLES20.glGenTextures(
				1, // 产生的纹理id的数量
				textures, // 纹理id的数组
				0 // 偏移量
		);
		int textureId = textures[0];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_REPEAT);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_REPEAT);

		// 通过输入流加载图片===============begin===================
		InputStream is = this.getResources().openRawResource(drawableId);
		Bitmap bitmapTmp;
		try {
			bitmapTmp = BitmapFactory.decodeStream(is);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 通过输入流加载图片===============end=====================
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, // 纹理类型
				0, GLUtils.getInternalFormat(bitmapTmp), bitmapTmp, // 纹理图像
				GLUtils.getType(bitmapTmp), 0 // 纹理边框尺寸
		);
		bitmapTmp.recycle(); // 纹理加载成功后释放图片
		return textureId;
	}

}
