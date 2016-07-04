package com.graphics.engine.gl.graphics;

import android.content.res.Resources;

/**
 * 
 * <br>类描述: Shader的封装器，用于扩展 {@link GLDrawable} 的绘制行为
 * <br>功能详细描述: 
 * <br>使用{@link GLDrawable#setShaderWrapper(GLShaderWrapper)}方法来启用。
 * <br>支持在{@link RenderContext}中存储最多四个额外的浮点数参数。
 * <br>其他参数可以作为成员变量，在构造时指定。如果后续再修改，为了避免线程同步问题，需要
 * 使用synchronized关键字，或者通过{@link VertexBufferBlock}传递（复杂点）。
 * <br>一般实现上，封装器就是用来传递参数的，应该是多个实例的，实际上的着色器用单例实现，
 * 处理不同的参数组即可。
 * 
 * @author  dengweiming
 * @date  [2012-11-20]
 */
public abstract class GLShaderWrapper extends GLShaderProgram {
	
	public GLShaderWrapper() {
	}
	
	public GLShaderWrapper(Resources res, String vertexFile, String fragmentFile) {
		super(res, vertexFile, fragmentFile);
	}
	
	public GLShaderWrapper(String vertexSource, String fragmentSource) {
		super(vertexSource, fragmentSource);
	}

	/**
	 * <br>功能简述: 在主线程绘制时的操作
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context 绘制的上下文，里面的各个成员只剩下{@link RenderContext#color}可供使用，也即最多只能支持四个额外的浮点数参数
	 */
	public abstract void onDraw(RenderContext context);
	
	/**
	 * <br>功能简述: 在GL线程渲染时的操作
	 * <br>功能详细描述: 实现代码示例：
	 * <pre>
	 * 	//封装了一个静态的shader实例，这里以一个{@link TextureShader}的子类为例
	 * 	InternalShader shader = sInternalShader;
	 * 	if (shader == null || !shader.bind()) {
	 * 		return null;
	 * 	}
	 * 	shader.setAlpha(context.alpha);
	 * 	shader.setMatrix(context.matrix, 0);
	 * 	shader.setExtraArgs(context.color);	//给封装的shader传递额外参数（最多四个）
	 * 	return shader;
	 * </pre>
	 * 
	 * <br>注意: 需要bind一个shader
	 * @param context
	 * @return 实际需要用到的shader
	 */
	public abstract GLShaderProgram onRender(RenderContext context);

}
