package com.graphics.engine.gl.graphics;

/**
 * 
 * <br>类描述: 纹理上传完成后的回调者
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2012-9-14]
 */
public interface TextureLoadedListener {
	
	/**
	 * <br>功能简述: 在纹理上传完成后的回调方法
	 * <br>功能详细描述:
	 * <br>注意: 已经同步在主线程了
	 * @param texture
	 */
	public void onTextureLoaded(Texture texture);
}
