package com.graphics.engine.badlogic.gdx.graphics.g2d;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.PorterDuff.Mode;
import android.opengl.GLES20;

import com.graphics.engine.badlogic.gdx.Disposable;
import com.graphics.engine.badlogic.gdx.MathUtils;
import com.graphics.engine.badlogic.gdx.NumberUtils;
import com.graphics.engine.badlogic.gdx.graphics.Color;
import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.GLShaderProgram;
import com.graphics.engine.graphics.IndexBufferBlock;
import com.graphics.engine.graphics.RenderContext;
import com.graphics.engine.graphics.Renderable;
import com.graphics.engine.graphics.Texture;
import com.graphics.engine.graphics.VertexBufferBlock;
import com.graphics.engine.graphics.ext.GradientTextureShaderWrapper.GradientTextureShader;

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/



/** <p>
 * A SpriteBatch is used to draw 2D rectangles that reference a texture (region). The class will batch the drawing commands and
 * optimize them for processing by the GPU.
 * </p>
 * 
 * <p>
 * To draw something with a SpriteBatch one has to first call the {@link SpriteBatch#begin()} method which will setup appropriate
 * render states. When you are done with drawing you have to call {@link SpriteBatch#end()} which will actually draw the things
 * you specified.
 * </p>
 * 
 * <p>
 * All drawing commands of the SpriteBatch operate in screen coordinates. The screen coordinate system has an x-axis pointing to
 * the right, an y-axis pointing upwards and the origin is in the lower left corner of the screen. You can also provide your own
 * transformation and projection matrices if you so wish.
 * </p>
 * 
 * <p>
 * A SpriteBatch is managed. In case the OpenGL context is lost all OpenGL resources a SpriteBatch uses internally get
 * invalidated. A context is lost when a user switches to another application or receives an incoming call on Android. A
 * SpriteBatch will be automatically reloaded after the OpenGL context is restored.
 * </p>
 * 
 * <p>
 * A SpriteBatch is a pretty heavy object so you should only ever have one in your program.
 * </p>
 * 
 * <p>
 * A SpriteBatch works with OpenGL ES 1.x and 2.0. In the case of a 2.0 context it will use its own custom shader to draw all
 * provided sprites. You can set your own custom shader via {@link #setShader(ShaderProgram)}.
 * </p>
 * 
 * <p>
 * A SpriteBatch has to be disposed if it is no longer used.
 * </p>
 * 
 * @author mzechner */
//CHECKSTYLE IGNORE 10000 LINES
public class SpriteBatch implements Disposable {

	private Texture lastTexture = null;
	private float invTexWidth = 0;
	private float invTexHeight = 0;

	private int idx = 0;
	private final float[] vertices;
	private final short[] indices;

	private GLCanvas curCanvasOnDraw;
	private static GradientTextureShader sShader;
	private float[] matrix = new float[16];	//CHECKSTYLE IGNORE

	private boolean drawing = false;

	private boolean blendingDisabled = false;
	private Mode blendMode;

	float color = Color.WHITE.toFloatBits();
	private Color tempColor = new Color(1, 1, 1, 1);

	/** number of render calls since last {@link #begin()} **/
	public int renderCalls = 0;

	/** number of rendering calls ever, will not be reset, unless it's done manually **/
	public int totalRenderCalls = 0;

	/** the maximum number of sprites rendered in one batch so far **/
	public int maxSpritesInBatch = 0;

	/** Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
	 * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
	 * respect to the screen resolution. */
	public SpriteBatch () {
		this(1000);
	}

	/** Constructs a SpriteBatch with the specified size and (if GL2) the default shader. See
	 * {@link #SpriteBatch(int, ShaderProgram)}. */
	public SpriteBatch (int size) {
		this(size, null);
	}

	/** <p>
	 * Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
	 * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
	 * respect to the screen resolution.
	 * </p>
	 * 
	 * <p>
	 * The size parameter specifies the maximum size of a single batch in number of sprites
	 * </p>
	 * 
	 * <p>
	 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
	 * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See the {@link #createDefaultShader()} method.
	 * </p>
	 * 
	 * @param size the batch size in number of sprites
	 * @param defaultShader the default shader to use. This is not owned by the SpriteBatch and must be disposed separately. */
	public SpriteBatch (int size, GLShaderProgram defaultShader) {
		this(size, 1, defaultShader);
	}

	/** Constructs a SpriteBatch with the specified size and number of buffers and (if GL2) the default shader. See
	 * {@link #SpriteBatch(int, int, ShaderProgram)}. */
	public SpriteBatch (int size, int buffers) {
		this(size, buffers, null);
	}

	/** <p>
	 * Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
	 * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
	 * respect to the screen resolution.
	 * </p>
	 * 
	 * <p>
	 * The size parameter specifies the maximum size of a single batch in number of sprites
	 * </p>
	 * 
	 * <p>
	 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
	 * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See the {@link #createDefaultShader()} method.
	 * </p>
	 * 
	 * @param size the batch size in number of sprites
	 * @param buffers the number of buffers to use. only makes sense with VBOs. This is an expert function.
	 * @param defaultShader the default shader to use. This is not owned by the SpriteBatch and must be disposed separately. */
	public SpriteBatch (int size, int buffers, GLShaderProgram defaultShader) {

		vertices = new float[size * Sprite.SPRITE_SIZE];

		int len = size * 6;
		short[] indices = new short[len];
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
//			indices[i + 0] = (short)(j + 0);
//			indices[i + 1] = (short)(j + 1);
//			indices[i + 2] = (short)(j + 2);
//			indices[i + 3] = (short)(j + 2);
//			indices[i + 4] = (short)(j + 3);
//			indices[i + 5] = (short)(j + 0);
			//改为逆时针顺序
			indices[i + 0] = (short)(j + 0);
			indices[i + 1] = (short)(j + 2);
			indices[i + 2] = (short)(j + 1);
			indices[i + 3] = (short)(j + 2);
			indices[i + 4] = (short)(j + 0);
			indices[i + 5] = (short)(j + 3);
		}

		this.indices = indices;

	}


	/** Sets up the SpriteBatch for drawing. This will disable depth buffer writting. It enables blending and texturing. If you have
	 * more texture units enabled than the first one you have to disable them before calling this. Uses a screen coordinate system
	 * by default where everything is given in pixels. You can specify your own projection and modelview matrices via
	 * {@link #setProjectionMatrix(Matrix4)} and {@link #setTransformMatrix(Matrix4)}. */
	public void begin (GLCanvas canvas) {
		if (drawing) throw new IllegalStateException("you have to call SpriteBatch.end() first");
		renderCalls = 0;

		idx = 0;
		lastTexture = null;
		drawing = true;
		
		curCanvasOnDraw = canvas;
	}

	/** Finishes off rendering. Enables depth writes, disables blending and texturing. Must always be called after a call to
	 * {@link #begin()} */
	public void end () {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before end.");
		if (idx > 0) renderMesh();
		lastTexture = null;
		idx = 0;
		drawing = false;
		
		curCanvasOnDraw = null;
	}

	/** Sets the color used to tint images when they are added to the SpriteBatch. Default is {@link Color#WHITE}. */
	public void setColor (Color tint) {
		color = tint.toFloatBits();
	}

	/** @see #setColor(Color) */
	public void setColor (float r, float g, float b, float a) {
		int intBits = (int)(255 * a) << 24 | (int)(255 * b) << 16 | (int)(255 * g) << 8 | (int)(255 * r);
		color = NumberUtils.intToFloatColor(intBits);
	}

	/** @see #setColor(Color)
	 * @see Color#toFloatBits() */
	public void setColor (float color) {
		this.color = color;
	}

	/** @return the rendering color of this SpriteBatch. Manipulating the returned instance has no effect. */
	public Color getColor () {
		int intBits = NumberUtils.floatToIntColor(color);
		Color color = this.tempColor;
		color.r = (intBits & 0xff) / 255f;
		color.g = ((intBits >>> 8) & 0xff) / 255f;
		color.b = ((intBits >>> 16) & 0xff) / 255f;
		color.a = ((intBits >>> 24) & 0xff) / 255f;
		return color;
	}

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The rectangle is offset by
	 * originX, originY relative to the origin. Scale specifies the scaling factor by which the rectangle should be scaled around
	 * originX, originY. Rotation specifies the angle of counter clockwise rotation of the rectangle around originX, originY. The
	 * portion of the {@link Texture} given by srcX, srcY and srcWidth, srcHeight is used. These coordinates and sizes are given in
	 * texels. FlipX and flipY specify whether the texture portion should be fliped horizontally or vertically.
	 * 
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param originX the x-coordinate of the scaling and rotation origin relative to the screen space coordinates
	 * @param originY the y-coordinate of the scaling and rotation origin relative to the screen space coordinates
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param scaleX the scale of the rectangle around originX/originY in x
	 * @param scaleY the scale of the rectangle around originX/originY in y
	 * @param rotation the angle of counter clockwise rotation of the rectangle around originX/originY
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param flipX whether to flip the sprite horizontally
	 * @param flipY whether to flip the sprite vertically */
	public void draw (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) renderMesh();

		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;

		// scale
		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}

		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;

		float x1;
		float y1;
		float x2;
		float y2;
		float x3;
		float y3;
		float x4;
		float y4;

		// rotate
		if (rotation != 0) {
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			x1 = cos * p1x - sin * p1y;
			y1 = sin * p1x + cos * p1y;

			x2 = cos * p2x - sin * p2y;
			y2 = sin * p2x + cos * p2y;

			x3 = cos * p3x - sin * p3y;
			y3 = sin * p3x + cos * p3y;

			x4 = x1 + (x3 - x2);
			y4 = y3 - (y2 - y1);
		} else {
			x1 = p1x;
			y1 = p1y;

			x2 = p2x;
			y2 = p2y;

			x3 = p3x;
			y3 = p3y;

			x4 = p4x;
			y4 = p4y;
		}

		x1 += worldOriginX;
		y1 += worldOriginY;
		x2 += worldOriginX;
		y2 += worldOriginY;
		x3 += worldOriginX;
		y3 += worldOriginY;
		x4 += worldOriginX;
		y4 += worldOriginY;

		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;

		if (flipX) {
			float tmp = u;
			u = u2;
			u2 = tmp;
		}

		if (flipY) {
			float tmp = v;
			v = v2;
			v2 = tmp;
		}

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The portion of the
	 * {@link Texture} given by srcX, srcY and srcWidth, srcHeight is used. These coordinates and sizes are given in texels. FlipX
	 * and flipY specify whether the texture portion should be fliped horizontally or vertically.
	 * 
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param flipX whether to flip the sprite horizontally
	 * @param flipY whether to flip the sprite vertically */
	public void draw (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
		int srcHeight, boolean flipX, boolean flipY) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) renderMesh();

		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;
		final float fx2 = x + width;
		final float fy2 = y + height;

		if (flipX) {
			float tmp = u;
			u = u2;
			u2 = tmp;
		}

		if (flipY) {
			float tmp = v;
			v = v2;
			v2 = tmp;
		}

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The portion of the
	 * {@link Texture} given by srcX, srcY and srcWidth, srcHeight are used. These coordinates and sizes are given in texels.
	 * 
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels */
	public void draw (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) renderMesh();

		final float u = srcX * invTexWidth;
		final float v = (srcY + srcHeight) * invTexHeight;
		final float u2 = (srcX + srcWidth) * invTexWidth;
		final float v2 = srcY * invTexHeight;
		final float fx2 = x + srcWidth;
		final float fy2 = y + srcHeight;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the bottom left corner at x,y having the given width and height in pixels. The portion of the
	 * {@link Texture} given by u, v and u2, v2 are used. These coordinates and sizes are given in texture size percentage. The
	 * rectangle will have the given tint {@link Color}.
	 * 
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param width the width in pixels
	 * @param height the height in pixels */
	public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) renderMesh();

		final float fx2 = x + width;
		final float fy2 = y + height;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the bottom left corner at x,y having the width and height of the texture.
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space */
	public void draw (Texture texture, float x, float y) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) renderMesh();

		final float fx2 = x + texture.getWidth();
		final float fy2 = y + texture.getHeight();

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = 0;
		vertices[idx++] = 1;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = 0;
		vertices[idx++] = 0;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = 1;
		vertices[idx++] = 0;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = 1;
		vertices[idx++] = 1;
	}

	/** Draws a rectangle with the bottom left corner at x,y and stretching the region to cover the given width and height. */
	public void draw (Texture texture, float x, float y, float width, float height) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			renderMesh();

		final float fx2 = x + width;
		final float fy2 = y + height;
		final float u = 0;
		final float v = 1;
		final float u2 = 1;
		final float v2 = 0;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle using the given vertices. There must be 4 vertices, each made up of 5 elements in this order: x, y, color,
	 * u, v. The {@link #getColor()} from the SpriteBatch is not applied. */
	public void draw (Texture texture, float[] spriteVertices, int offset, int length) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		if (texture != lastTexture) {
			switchTexture(texture);
		}

		int remainingVertices = vertices.length - idx;
		if (remainingVertices == 0) {
			renderMesh();
			remainingVertices = vertices.length;
		}
		int vertexCount = Math.min(remainingVertices, length - offset);
		System.arraycopy(spriteVertices, offset, vertices, idx, vertexCount);
		offset += vertexCount;
		idx += vertexCount;

		while (offset < length) {
			renderMesh();
			vertexCount = Math.min(vertices.length, length - offset);
			System.arraycopy(spriteVertices, offset, vertices, 0, vertexCount);
			offset += vertexCount;
			idx += vertexCount;
		}
	}

	/** Draws a rectangle with the bottom left corner at x,y having the width and height of the region. */
	public void draw (TextureRegion region, float x, float y) {
		draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
	}

	/** Draws a rectangle with the bottom left corner at x,y and stretching the region to cover the given width and height. */
	public void draw (TextureRegion region, float x, float y, float width, float height) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		Texture texture = region.texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			renderMesh();

		final float fx2 = x + width;
		final float fy2 = y + height;
		final float u = region.u;
		final float v = region.v2;
		final float u2 = region.u2;
		final float v2 = region.v;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the bottom left corner at x,y and stretching the region to cover the given width and height. The
	 * rectangle is offset by originX, originY relative to the origin. Scale specifies the scaling factor by which the rectangle
	 * should be scaled around originX, originY. Rotation specifies the angle of counter clockwise rotation of the rectangle around
	 * originX, originY. */
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		Texture texture = region.texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			renderMesh();

		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;

		// scale
		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}

		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;

		float x1;
		float y1;
		float x2;
		float y2;
		float x3;
		float y3;
		float x4;
		float y4;

		// rotate
		if (rotation != 0) {
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			x1 = cos * p1x - sin * p1y;
			y1 = sin * p1x + cos * p1y;

			x2 = cos * p2x - sin * p2y;
			y2 = sin * p2x + cos * p2y;

			x3 = cos * p3x - sin * p3y;
			y3 = sin * p3x + cos * p3y;

			x4 = x1 + (x3 - x2);
			y4 = y3 - (y2 - y1);
		} else {
			x1 = p1x;
			y1 = p1y;

			x2 = p2x;
			y2 = p2y;

			x3 = p3x;
			y3 = p3y;

			x4 = p4x;
			y4 = p4y;
		}

		x1 += worldOriginX;
		y1 += worldOriginY;
		x2 += worldOriginX;
		y2 += worldOriginY;
		x3 += worldOriginX;
		y3 += worldOriginY;
		x4 += worldOriginX;
		y4 += worldOriginY;

		final float u = region.u;
		final float v = region.v2;
		final float u2 = region.u2;
		final float v2 = region.v;

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;
	}

	/** Draws a rectangle with the texture coordinates rotated 90 degrees. The bottom left corner at x,y and stretching the region
	 * to cover the given width and height. The rectangle is offset by originX, originY relative to the origin. Scale specifies the
	 * scaling factor by which the rectangle should be scaled around originX, originY. Rotation specifies the angle of counter
	 * clockwise rotation of the rectangle around originX, originY.
	 * @param clockwise If true, the texture coordinates are rotated 90 degrees clockwise. If false, they are rotated 90 degrees
	 *           counter clockwise. */
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, boolean clockwise) {
		if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

		Texture texture = region.texture;
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			renderMesh();

		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;

		// scale
		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}

		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;

		float x1;
		float y1;
		float x2;
		float y2;
		float x3;
		float y3;
		float x4;
		float y4;

		// rotate
		if (rotation != 0) {
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			x1 = cos * p1x - sin * p1y;
			y1 = sin * p1x + cos * p1y;

			x2 = cos * p2x - sin * p2y;
			y2 = sin * p2x + cos * p2y;

			x3 = cos * p3x - sin * p3y;
			y3 = sin * p3x + cos * p3y;

			x4 = x1 + (x3 - x2);
			y4 = y3 - (y2 - y1);
		} else {
			x1 = p1x;
			y1 = p1y;

			x2 = p2x;
			y2 = p2y;

			x3 = p3x;
			y3 = p3y;

			x4 = p4x;
			y4 = p4y;
		}

		x1 += worldOriginX;
		y1 += worldOriginY;
		x2 += worldOriginX;
		y2 += worldOriginY;
		x3 += worldOriginX;
		y3 += worldOriginY;
		x4 += worldOriginX;
		y4 += worldOriginY;

		float u1, v1, u2, v2, u3, v3, u4, v4;
		if (clockwise) {
			u1 = region.u2;
			v1 = region.v2;
			u2 = region.u;
			v2 = region.v2;
			u3 = region.u;
			v3 = region.v;
			u4 = region.u2;
			v4 = region.v;
		} else {
			u1 = region.u;
			v1 = region.v;
			u2 = region.u2;
			v2 = region.v;
			u3 = region.u2;
			v3 = region.v2;
			u4 = region.u;
			v4 = region.v2;
		}

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = u1;
		vertices[idx++] = v1;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = u3;
		vertices[idx++] = v3;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = u4;
		vertices[idx++] = v4;
	}

	/** Causes any pending sprites to be rendered, without ending the SpriteBatch. */
	public void flush () {
		renderMesh();
	}

	private void renderMesh () {
		if (idx == 0) return;

		renderCalls++;
		totalRenderCalls++;
		int spritesInBatch = idx / Sprite.SPRITE_SIZE;
		if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
		
		GLCanvas canvas = curCanvasOnDraw;
		if (canvas == null) {
			return;
		}
		
//		//for debug log
//		final String tag = "DWM";
//		int maxLogCount = 2;
//		Log.v(tag, "render mesh count=" + spritesInBatch + " ============================= ");
//		StringBuilder sb = new StringBuilder(1000);
//		int index = 0;
//		for(int i = 0; i < maxLogCount && i < spritesInBatch; ++i){
//			for(int j = 0; j < 4; ++j){
//				for(int k = 0; k < 5; ++k){
//					sb.append(vertices[index++]);
//					sb.append('\t');
//				}
//				sb.append('\n');
//			}
//			Log.v(tag, sb.toString());
//			Log.v(tag, "==");
//		}

		Renderable renderable = sRenderable;
		RenderContext context = RenderContext.acquire();
		context.texture = lastTexture;
		if (sShader == null) {
			//因为需要设置跨距，不希望影响到GradientTextureShaderWrapper静态实例，所以创建一个新的静态实例
			sShader = new GradientTextureShader();
			sShader.setVertexAttributeStride((X2 - X1) * 4);
		}
		context.shader = sShader;
		context.color[0] = spritesInBatch;
		System.arraycopy(matrix, 0, context.matrix, 0, matrix.length);

		final boolean blend = canvas.isBlendEnabled();
		final Mode mode = canvas.getBlendMode();
		canvas.setBlend(!blendingDisabled);
		canvas.setBlendMode(blendMode);
		
		VertexBufferBlock.pushVertexData(renderable);
		IndexBufferBlock.pushVertexData(indices, 0, spritesInBatch * 6);
		VertexBufferBlock.pushVertexData(vertices, 0, spritesInBatch * Sprite.SPRITE_SIZE);
		canvas.addRenderable(renderable, context);

		canvas.setBlendMode(mode);
		canvas.setBlend(blend);
		
		idx = 0;
	}

	/** Disables blending for drawing sprites. */
	public void disableBlending () {
		if (blendingDisabled) return;
		renderMesh();
		blendingDisabled = true;
	}

	/** Enables blending for sprites */
	public void enableBlending () {
		if (!blendingDisabled) return;
		renderMesh();
		blendingDisabled = false;
	}

//	/** Sets the blending function to be used when rendering sprites.
//	 * 
//	 * @param srcFunc the source function, e.g. GL11.GL_SRC_ALPHA. If set to -1, SpriteBatch won't change the blending function.
//	 * @param dstFunc the destination function, e.g. GL11.GL_ONE_MINUS_SRC_ALPHA */
//	public void setBlendFunction (int srcFunc, int dstFunc) {
//		renderMesh();
//		blendSrcFunc = srcFunc;
//		blendDstFunc = dstFunc;
//	}
	
	/** Sets the blending mode to be used when rendering sprites. */
	public void setBlendMode(Mode mode) {
		renderMesh();
		blendMode = mode;
	}

	/** Disposes all resources associated with this SpriteBatch */
	public void dispose () {
	}
	
	/**
	 * <br>功能简述: 设置MVP矩阵
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param canvas	
	 */
	public void setMVPMatrix(GLCanvas canvas) {
		if (drawing) flush();
		System.arraycopy(canvas.getFinalMatrix(), 0, matrix, 0, matrix.length);
	}

	private void switchTexture (Texture texture) {
		renderMesh();
		lastTexture = texture;
		invTexWidth = 1.0f / texture.getWidth();
		invTexHeight = 1.0f / texture.getHeight();
	}

	/** @return whether blending for sprites is enabled */
	public boolean isBlendingEnabled () {
		return !blendingDisabled;
	}

	static public final int X1 = 0;
	static public final int Y1 = 1;
	static public final int C1 = 2;
	static public final int U1 = 3;
	static public final int V1 = 4;
	static public final int X2 = 5;
	static public final int Y2 = 6;
	static public final int C2 = 7;
	static public final int U2 = 8;
	static public final int V2 = 9;
	static public final int X3 = 10;
	static public final int Y3 = 11;
	static public final int C3 = 12;
	static public final int U3 = 13;
	static public final int V3 = 14;
	static public final int X4 = 15;
	static public final int Y4 = 16;
	static public final int C4 = 17;
	static public final int U4 = 18;
	static public final int V4 = 19;
	
	private final static Renderable sRenderable = new Renderable() {

		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);
			int spriteCount = (int) context.color[0];
			int vertexDataSize = spriteCount * Sprite.SPRITE_SIZE;
			int indexCount = spriteCount * 6;
			if (context.texture == null 
					|| context.shader == null 
					|| !context.texture.bind() 
					|| !context.shader.bind()) {
				VertexBufferBlock.popVertexData(null, 0, vertexDataSize);
				IndexBufferBlock.popVertexData(null, 0, indexCount);
				return;
			}
			
			VertexBufferBlock.rewindReadingBuffer(vertexDataSize);
			GradientTextureShader shader = (GradientTextureShader) context.shader;
			shader.setMatrix(context.matrix, 0);
			
			FloatBuffer attributesBuffer = VertexBufferBlock.popVertexData(vertexDataSize);
			
			Buffer positionBuffer = attributesBuffer.position(X1);
			shader.setPosition(positionBuffer, 2);
			
			Buffer colorCoBuffer = attributesBuffer.position(C1);
			shader.setColor(colorCoBuffer, (C2 - C1) * 4);
			
			Buffer texCoordBuffer = attributesBuffer.position(U1);
			shader.setTexCoord(texCoordBuffer, 2);
			
			IndexBufferBlock.rewindReadingBuffer(indexCount);
			ShortBuffer indexBuffer = IndexBufferBlock.popVertexData(indexCount);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
		}
		
	};
	
}
