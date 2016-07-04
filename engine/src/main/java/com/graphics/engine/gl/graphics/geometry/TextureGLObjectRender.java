package com.graphics.engine.gl.graphics.geometry;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.graphics.engine.gl.graphics.BitmapTexture;
import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLShaderProgram;
import com.graphics.engine.gl.graphics.GLVBO;
import com.graphics.engine.gl.graphics.IndexBufferBlock;
import com.graphics.engine.gl.graphics.RenderContext;
import com.graphics.engine.gl.graphics.Renderable;
import com.graphics.engine.gl.graphics.Texture;
import com.graphics.engine.gl.graphics.TextureListener;
import com.graphics.engine.gl.graphics.TextureManager;
import com.graphics.engine.gl.graphics.TextureShader;
import com.graphics.engine.gl.graphics.VertexBufferBlock;
import com.graphics.engine.gl.util.NdkUtil;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;

/**
 * <br>类描述: 单贴图物体的渲染器{@link GLObjectRender}
 * <br>功能详细描述:
 * <br>使用{@link #setTexture(Bitmap)}或{@link #setTexture(Texture)},
 * {@link #setTexture(Resources, int)}设置纹理。
 * <br>使用完毕需要使用{@link #clear()}清理资源。
 * <br>物体需要使用{@link GLObject#setTexcoords(float, float, float, float)}设置纹理坐标，
 * 否则会绘制不出来。
 * 
 * @author  dengweiming
 * @date  [2013-10-22]
 */
public class TextureGLObjectRender extends BaseGLObjectRender implements TextureListener {
	public Texture mTexture;
	private float mAlpha = 1;
	
	public TextureGLObjectRender() {
		mHasTexcoord = true;
		TextureManager.getInstance().registerTextureListener(this);
	}
	
	/**
	 * <br>功能简述: 设置纹理
	 * <br>功能详细描述:
	 * <br>注意: 原来的纹理会被清除
	 * @param texture
	 */
	public void setTexture(Texture texture) {
		if (mTexture != null && mTexture != texture) {
			mTexture.clear();
		}
		mTexture = texture;
	}

	/**
	 * <br>功能简述: 设置纹理
	 * <br>功能详细描述:
	 * <br>注意: 原来的纹理会被清除
	 * @param bitmap
	 */
	public void setTexture(Bitmap bitmap) {
		setTexture(BitmapTexture.createSharedTexture(bitmap));
	}
	
	/**
	 * <br>功能简述: 设置纹理
	 * <br>功能详细描述:
	 * <br>注意: 原来的纹理会被清除
	 * @param res
	 * @param id
	 */
	public void setTexture(Resources res, int id) {
		Drawable drawable = res.getDrawable(id);
		if (drawable instanceof BitmapDrawable) {
			setTexture(((BitmapDrawable) drawable).getBitmap());
		} else {
			throw new RuntimeException("This image (id=" + id + ") is not a bitmap");
		}
	}

	@Override
	public void draw(GLCanvas canvas, GLObject object) {
		if (mTexture == null) {
			return;
		}
		
		final int fadeAlpha = canvas.getAlpha();
		float alpha = mAlpha;
		if (fadeAlpha < FULL_ALPHA) {
			alpha *= fadeAlpha * ONE_OVER_255;
		}
		
		GLShaderProgram shader = TextureShader.getShader(alpha >= 1
				? TextureShader.MODE_NONE
				: TextureShader.MODE_ALPHA);
		if (shader == null) {
			return;
		}
		
		// use VBO to render
		if (object.getPositionVBO() != null) {
			if (!putDataWithVBO(object, sRenderableWithVBO, canvas)) {
				return;
			}
			
			RenderContext context = RenderContext.acquire();
			context.texture = mTexture;
			context.alpha = alpha;
			context.shader = shader;
			canvas.getFinalMatrix(context);
			canvas.addRenderable(sRenderableWithVBO, context);
			return;
		}
		
		if (!putData(object, sRenderable)) {
			return;
		}
		
		RenderContext context = RenderContext.acquire();
		context.texture = mTexture;
		context.alpha = alpha;
		context.shader = shader;
		canvas.getFinalMatrix(context);
		canvas.addRenderable(sRenderable, context);
	}

	@Override
	public void onTextureInvalidate() {
		if (mTexture != null) {
			mTexture.onTextureInvalidate();
		}
	}
	
	@Override
	public void clear() {
		//TODO:清理纹理
	}

	@Override
	public void onClear() {
		TextureManager.getInstance().unRegisterTextureListener(this);
	}

	@Override
	public void yield() {
		
	}

	@Override
	public void onYield() {
		
	}
	
	private final static Renderable sRenderable = new Renderable() {	//CHECKSTYLE IGNORE
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);
			
			float[] info = TEMP_FLOAT_BUFFER_GL;
			VertexBufferBlock.popVertexData(info, 0, INDEX_LAST);
			if (info[INDEX_MARK] != MARK) {
				throw new RuntimeException("mark incorrect!");
			}
			final int mode = GLObject.MODE[(int) info[INDEX_DRAW_MODE]];
			final int indexCount = (int) info[INDEX_INDEX_COUNT];
			final int vertexCount = (int) info[INDEX_VERTEX_COUNT];
			final int totalVertexData = (int) info[INDEX_TOTAL_VERTEX_DATA];
			
			if (context.texture == null 
					|| context.shader == null 
					|| !context.texture.bind() 
					|| !context.shader.bind()) {
				VertexBufferBlock.popVertexData(null, 0, totalVertexData);
				IndexBufferBlock.popVertexData(null, 0, indexCount);
				return;
			}
			
			VertexBufferBlock.rewindReadingBuffer(totalVertexData);
			TextureShader shader = (TextureShader) context.shader;
			shader.setMatrix(context.matrix, 0);
			shader.setAlpha(context.alpha);
			
			FloatBuffer positionBuffer = VertexBufferBlock.popVertexData((int) info[INDEX_POSITION_ELEMENTS]);
			shader.setPosition(positionBuffer, (int) info[INDEX_POSITION_COMPONENT]);
			
			FloatBuffer texCoordBuffer = VertexBufferBlock.popVertexData((int) info[INDEX_TEXCOORD_ELEMENTS]);
			shader.setTexCoord(texCoordBuffer, (int) info[INDEX_TEXCOORD_COMPONENT]);
			
			if (indexCount > 0) {
				IndexBufferBlock.rewindReadingBuffer(indexCount);
				ShortBuffer indexBuffer = IndexBufferBlock.popVertexData(indexCount);
				GLES20.glDrawElements(mode, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
			} else {
				GLES20.glDrawArrays(mode, 0, vertexCount);
			}
			
		}
	};
	
	private final static Renderable sRenderableWithVBO = new Renderable() {	//CHECKSTYLE IGNORE
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);
			
			float[] info = TEMP_FLOAT_BUFFER_GL;
			VertexBufferBlock.popVertexData(info, 0, INDEX_LAST);
			if (info[INDEX_MARK] != MARK) {
				throw new RuntimeException("mark incorrect!");
			}
			final int mode = GLObject.MODE[(int) info[INDEX_DRAW_MODE]];
			final int indexCount = (int) info[INDEX_INDEX_COUNT];
			final int vertexCount = (int) info[INDEX_VERTEX_COUNT];
			final int totalVertexData = (int) info[INDEX_TOTAL_VERTEX_DATA];
			
			if (context.texture == null 
					|| context.shader == null 
					|| !context.texture.bind() 
					|| !context.shader.bind()) {
				
				if (info[INDEX_POSITION_ELEMENTS] > 0) {
					GLVBO.unbindOnGLThread();
				}
				if (info[INDEX_TEXCOORD_ELEMENTS] > 0) {
					GLVBO.unbindOnGLThread();
				}
				if (indexCount > 0) {
					GLVBO.unbindOnGLThread();
				}
				return;
			}
			
			TextureShader shader = (TextureShader) context.shader;
			shader.setMatrix(context.matrix, 0);
			shader.setAlpha(context.alpha);
			
			if (info[INDEX_POSITION_ELEMENTS] > 0) {
				shader.setPosition(0, (int) info[INDEX_POSITION_COMPONENT]);
				GLVBO.unbindOnGLThread();
			}
			if (info[INDEX_TEXCOORD_ELEMENTS] > 0) {
				shader.setTexCoord(0, (int) info[INDEX_TEXCOORD_COMPONENT]);
				GLVBO.unbindOnGLThread();
			}
			
			if (indexCount > 0) {
				NdkUtil.glDrawElements(mode, indexCount, GLES20.GL_UNSIGNED_SHORT, 0);
				GLVBO.unbindOnGLThread();
			} else {
				GLES20.glDrawArrays(mode, 0, vertexCount);
			}
			
		}
	};

}
