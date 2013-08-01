package com.ty.exsample_unit_5.translate;

import android.opengl.GLES20;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.ty.example_unit_3.libgdx.ex.GdxTest;


/**
 * 
 * @author tangyong
 *
 */
public class CullFace extends GdxTest{

	ShaderProgram shader;
	Mesh mesh;

	@Override
	public boolean needsGL20 () {
		return true;
	}

	@Override
	public void create () {
		String vertexShader = "attribute vec4 vPosition;    \n" + "void main()                  \n"
			+ "{                            \n" + "   gl_Position = vPosition;  \n" + "}                            \n";
		String fragmentShader = "#ifdef GL_ES\n" + "precision mediump float;\n" + "#endif\n"
			+ "void main()                                  \n" + "{                                            \n"
			+ "  gl_FragColor = vec4 ( 1.0, 1.0, 1.0, 1.0 );\n" + "}";

		shader = new ShaderProgram(vertexShader, fragmentShader);
		mesh = new Mesh(true, 3, 0, new VertexAttribute(Usage.Position, 3, "vPosition"));
		
		
		float[] vertices = {
				/*点顶顺序是顺时针 必需指定巻绕方式*/
				0.0f, 0.5f, 0.0f,
				-0.5f, -0.5f, 0.0f,
				0.5f, -0.5f, 0.0f
				
				};
		mesh.setVertices(vertices);
	}

	@Override
	public void render () {
		//如果关掉背面裁剪那么绘制顺序就没有关系了.
		Gdx.gl20.glDisable(GLES20.GL_CULL_FACE);
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shader.begin();
		mesh.render(shader, GL20.GL_TRIANGLES);
		shader.end();
	}
	
}
