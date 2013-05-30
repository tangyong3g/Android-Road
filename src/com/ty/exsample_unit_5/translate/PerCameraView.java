package com.ty.exsample_unit_5.translate;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.ty.exsample_unit_5.models.SixPointedStar;
import com.ty.exsample_unit_5.unit.MatrixState;

/**
 * 
 * @author tangyong ty_sany@163.com
 *
 */
public class PerCameraView extends GLSurfaceView{

	private Context mContext;
	private OrthRender mRender;
	
	private float mPreviousY;
	private float mPreviousX;
	
	private static final float  TOUCH_SCALE_FACTOR = 180/320.0f;

	public PerCameraView (Context context) {
		super(context);
		mContext= context;
		setEGLContextClientVersion(2);
		mRender = new OrthRender();
		setRenderer(mRender);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
	}
	
	//触摸事件回调方法
   @Override 
   public boolean onTouchEvent(MotionEvent e) {
       float y = e.getY();
       float x = e.getX();
       switch (e.getAction()) {
       case MotionEvent.ACTION_MOVE:
           float dy = y - mPreviousY;//计算触控笔Y位移
           float dx = x - mPreviousX;//计算触控笔X位移            
           for(SixPointedStar h: mRender.array)
           {
           	h.yAngle += dx * TOUCH_SCALE_FACTOR;//设置六角星数组中的各个六角星绕y轴旋转角度
               h.xAngle+= dy * TOUCH_SCALE_FACTOR;//设置六角星数组中的各个六角星绕x轴旋转角度
           }
       }
       mPreviousY = y;//记录触控笔位置
       mPreviousX = x;//记录触控笔位置
       return true;
   }
	
	class OrthRender implements Renderer{
		
	public	SixPointedStar [] array = new SixPointedStar[3];
		
		@Override
		public void onSurfaceCreated (GL10 gl, EGLConfig config) {
		    //设置屏幕背景色RGBA
         GLES20.glClearColor(0.5f,0.5f,0.5f, 1.0f);  
         //创建六角星数组中的各个对象 
         for(int i=0;i<array.length;i++)
         {
         	array[i]=new SixPointedStar(mContext,0.2f,0.5f,-0.3f*i);   
         }            
         //打开深度检测
         GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		}

		@Override
		public void onSurfaceChanged (GL10 gl, int width, int height) {
			
	      //设置视窗大小及位置 
        	GLES20.glViewport(0, 0, width, height);
        	//计算GLSurfaceView的宽高比
        	float ratio= (float) width / height;
            //设置平行投影
        	MatrixState.setProjectFrustum(-ratio, ratio, -1,1, 1, 100);
        	
            //调用此方法产生摄像机9参数位置矩阵
			MatrixState.setCamera(
					0, 0, 3f, 
					0, 0, -1f, 
					0f, 1.0f, 0.0f
					);
		}

		@Override
		public void onDrawFrame (GL10 gl) {
			Log.i("tyler.tang","onDraframe");
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			
			for(SixPointedStar star: array){
				star.drawSelf();
			}
		}
	}
}
