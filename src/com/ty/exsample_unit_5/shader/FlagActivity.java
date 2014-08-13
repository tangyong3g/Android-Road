package com.ty.exsample_unit_5.shader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;

import com.ty.exsample.R;
import com.ty.example_unit_1.cube.MatrixState;
import com.ty.exsample_unit_5.unit.ShaderUtil;

/**
 * 
 * 利用着色器改变顶点，形成波浪的效果。
 * 
 * 
 * 
 * @author tang
 * 
 */
public class FlagActivity extends Activity {

	FlagSurfaceView mView;
	Context mContext;
	static boolean mRun = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("cycle","onCreate");
		mContext = this;
		mView = new FlagSurfaceView(this);
		setContentView(mView);
	}

	class FlagSurfaceView extends GLSurfaceView {

		FlagRender mRender;

		public FlagSurfaceView(Context context) {
			super(context);

			mRender = new FlagRender();
			setEGLContextClientVersion(2);
			setRenderer(mRender);
		}

	}

	class FlagRender implements Renderer {

		Flag flag;
		FlagSurfaceView mView;
		int textureFlagId;

		@Override
		public void onDrawFrame(GL10 gl) {
			// 清除深度缓冲与颜色缓冲
			GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
					| GLES20.GL_COLOR_BUFFER_BIT);
			MatrixState.pushMatrix();
			MatrixState.translate(0, 0, -1);
			// MatrixState.rotate(yAngle, 0, 1, 0);
			// MatrixState.rotate(xAngle, 1, 0, 0);
			// 绘制纹理矩形
			flag.drawSelf(textureFlagId);
			MatrixState.popMatrix();

		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			// 设置视窗大小及位置
			GLES20.glViewport(0, 0, width, height);
			// 计算GLSurfaceView的宽高比
			float ratio = (float) width / height;
			// 调用此方法计算产生透视投影矩阵
			MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 4, 100);
			// 调用此方法产生摄像机9参数位置矩阵
			MatrixState.setCamera(0, 0, 5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			// 设置屏幕背景色RGBA
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			// GLES20.glClearColor(1.0f,1.0f,1.0f,1.0f);
			// 创建纹理矩形对对象
			flag = new Flag(FlagActivity.this.mView);
			// 打开深度检测
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			// 初始化纹理
			textureFlagId = initTexture(R.drawable.ic_launcher);
			// 关闭背面剪裁
			GLES20.glDisable(GLES20.GL_CULL_FACE);
			// 初始化变换矩阵
			MatrixState.setInitStack();
		}

		public int initTexture(int drawableId)// textureId
		{
			// 生成纹理ID
			int[] textures = new int[1];
			GLES20.glGenTextures(1, // 产生的纹理id的数量
					textures, // 纹理id的数组
					0 // 偏移量
			);
			int textureId = textures[0];
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			// 通过输入流加载图片===============begin===================
			InputStream is = mContext.getResources()
					.openRawResource(drawableId);
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
			// 实际加载纹理
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, // 纹理类型，在OpenGL
														// ES中必须为GL10.GL_TEXTURE_2D
					0, // 纹理的层次，0表示基本图像层，可以理解为直接贴图
					bitmapTmp, // 纹理图像
					0 // 纹理边框尺寸
			);
			bitmapTmp.recycle(); // 纹理加载成功后释放图片
			return textureId;
		}

	}

	class Flag {

		int mPrograms;;// 自定义渲染管线着色器程序id
		int muMVPMatrixHandle;// 总变换矩阵引用
		int maPositionHandle; // 顶点位置属性引用
		int maTexCoorHandle; // 顶点纹理坐标属性引用
		int maStartAngleHandle; // 本帧起始角度属性引用
		int muWidthSpanHandle;// 横向长度总跨度引用
		int currIndex = 0;// 当前着色器索引
		FloatBuffer mVertexBuffer;// 顶点坐标数据缓冲
		FloatBuffer mTexCoorBuffer;// 顶点纹理坐标数据缓冲
		int vCount = 0;
		final float WIDTH_SPAN = 3.3f;// 2.8f;//横向长度总跨度
		float currStartAngle = 0;// 当前帧的起始角度0~2PI

		public Flag(FlagSurfaceView mv) {
			// 初始化顶点坐标与着色数据
			initVertexData();
			// 初始化shader
			initShader(mv, 0, "data/unit5/shader/vert.sh");
			// 启动一个线程定时换帧
			new Thread() {
				public void run() {
					while (mRun) {
						currStartAngle += (float) (Math.PI / 16);
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}

		// 初始化shader
		public void initShader(FlagSurfaceView mv, int index, String vertexName) {
			// 加载顶点着色器的脚本内容
			String mVertexShader = ShaderUtil.loadFromAssetsFile(vertexName,
					mv.getResources());
			// 加载片元着色器的脚本内容
			String mFragmentShader = ShaderUtil.loadFromAssetsFile(
					"data/unit5/shader/frag.sh", mv.getResources());
			// 基于顶点着色器与片元着色器创建程序
			mPrograms = ShaderUtil
					.createProgram(mVertexShader, mFragmentShader);
			// 获取程序中顶点位置属性引用
			maPositionHandle = GLES20.glGetAttribLocation(mPrograms,
					"aPosition");
			// 获取程序中顶点纹理坐标属性引用
			maTexCoorHandle = GLES20.glGetAttribLocation(mPrograms, "aTexCoor");
			// 获取程序中总变换矩阵引用
			muMVPMatrixHandle = GLES20.glGetUniformLocation(mPrograms,
					"uMVPMatrix");
			// 获取本帧起始角度属性引用
			maStartAngleHandle = GLES20.glGetUniformLocation(mPrograms,
					"uStartAngle");
			// 获取横向长度总跨度引用
			muWidthSpanHandle = GLES20.glGetUniformLocation(mPrograms,
					"uWidthSpan");
		}

		public void drawSelf(int texId) {
			// 制定使用某套shader程序
			GLES20.glUseProgram(mPrograms);
			// 将最终变换矩阵传入shader程序
			GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false,
					MatrixState.getFinalMatrix(), 0);
			// 将本帧起始角度传入shader程序
			GLES20.glUniform1f(maStartAngleHandle, currStartAngle);
			// 将横向长度总跨度传入shader程序
			GLES20.glUniform1f(muWidthSpanHandle, WIDTH_SPAN);
			// 将顶点位置数据传入渲染管线
			GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
					false, 3 * 4, mVertexBuffer);
			// 将顶点纹理坐标数据传入渲染管线
			GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_FLOAT,
					false, 2 * 4, mTexCoorBuffer);
			// 启用顶点位置、纹理坐标数据
			GLES20.glEnableVertexAttribArray(maPositionHandle);
			GLES20.glEnableVertexAttribArray(maTexCoorHandle);
			// 绑定纹理
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
		}

		// 初始化顶点坐标与着色数据的方法
		public void initVertexData() {
			final int cols = 12;// 列数
			final int rows = cols * 3 / 4;// 行数
			final float UNIT_SIZE = WIDTH_SPAN / cols;// 每格的单位长度
			// 顶点坐标数据的初始化================begin============================
			vCount = cols * rows * 6;// 每个格子两个三角形，每个三角形3个顶点
			float vertices[] = new float[vCount * 3];// 每个顶点xyz三个坐标
			int count = 0;// 顶点计数器
			for (int j = 0; j < rows; j++) {
				for (int i = 0; i < cols; i++) {
					// 计算当前格子左上侧点坐标
					float zsx = -UNIT_SIZE * cols / 2 + i * UNIT_SIZE;
					float zsy = UNIT_SIZE * rows / 2 - j * UNIT_SIZE;
					float zsz = 0;

					vertices[count++] = zsx;
					vertices[count++] = zsy;
					vertices[count++] = zsz;

					vertices[count++] = zsx;
					vertices[count++] = zsy - UNIT_SIZE;
					vertices[count++] = zsz;

					vertices[count++] = zsx + UNIT_SIZE;
					vertices[count++] = zsy;
					vertices[count++] = zsz;

					vertices[count++] = zsx + UNIT_SIZE;
					vertices[count++] = zsy;
					vertices[count++] = zsz;

					vertices[count++] = zsx;
					vertices[count++] = zsy - UNIT_SIZE;
					vertices[count++] = zsz;

					vertices[count++] = zsx + UNIT_SIZE;
					vertices[count++] = zsy - UNIT_SIZE;
					vertices[count++] = zsz;
				}
			}
			// 创建顶点坐标数据缓冲
			// vertices.length*4是因为一个整数四个字节
			ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
			vbb.order(ByteOrder.nativeOrder());// 设置字节顺序
			mVertexBuffer = vbb.asFloatBuffer();// 转换为Float型缓冲
			mVertexBuffer.put(vertices);// 向缓冲区中放入顶点坐标数据
			mVertexBuffer.position(0);// 设置缓冲区起始位置
			// 顶点纹理坐标数据的初始化================begin============================
			float texCoor[] = generateTexCoor(cols, rows);
			// 创建顶点纹理坐标数据缓冲
			ByteBuffer cbb = ByteBuffer.allocateDirect(texCoor.length * 4);
			cbb.order(ByteOrder.nativeOrder());// 设置字节顺序
			mTexCoorBuffer = cbb.asFloatBuffer();// 转换为Float型缓冲
			mTexCoorBuffer.put(texCoor);// 向缓冲区中放入顶点着色数据
			mTexCoorBuffer.position(0);// 设置缓冲区起始位置
		}

		// 自动切分纹理产生纹理数组的方法
		public float[] generateTexCoor(int bw, int bh) {
			float[] result = new float[bw * bh * 6 * 2];
			float sizew = 1.0f / bw;// 列数
			float sizeh = 0.75f / bh;// 行数
			int c = 0;
			for (int i = 0; i < bh; i++) {
				for (int j = 0; j < bw; j++) {
					// 每行列一个矩形，由两个三角形构成，共六个点，12个纹理坐标
					float s = j * sizew;
					float t = i * sizeh;

					result[c++] = s;
					result[c++] = t;

					result[c++] = s;
					result[c++] = t + sizeh;

					result[c++] = s + sizew;
					result[c++] = t;

					result[c++] = s + sizew;
					result[c++] = t;

					result[c++] = s;
					result[c++] = t + sizeh;

					result[c++] = s + sizew;
					result[c++] = t + sizeh;
				}
			}
			return result;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("cycle","onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("cycle","onResume");
	}

}
