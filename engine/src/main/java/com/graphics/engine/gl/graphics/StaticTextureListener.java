package com.graphics.engine.gl.graphics;

/**
 * 
 * <br>类描述: 静态纹理失效的事件监听者
 * <br>功能详细描述:
 * <br>在{@link GLActivity}销毁时，纹理管理器会移除监听者，如果监听者是静态的（例如着色器），
 * 一般来说，使用者不会正确地销毁和重新创建它，以致重新创建GLActivity时，监听者会失效。
 * <br>因此，对于静态纹理，纹理管理器会在重新创建GLActivity自动将它们重新注册。这需要调用
 * {@link TextureManager#registerStaticTextureListener(StaticTextureListener)}。
 * 
 */
public interface StaticTextureListener extends TextureListener {
	
}