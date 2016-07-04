package com.graphics.engine.gl.graphics;

/**
 * 
 * <br>类描述:纹理失效的事件监听者
 * <br>功能详细描述:
 * <br>在程序切换时，纹理{@link Texture}会被销毁导致失效，那么下次使用之前需要重新上传像素数据，
 * 监听失效的事件以便确定是否需要重新上传。
 * 
 */
public interface TextureListener {
	
	/**
	 * <br>功能简述: 纹理失效时的回调方法
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onTextureInvalidate();
}