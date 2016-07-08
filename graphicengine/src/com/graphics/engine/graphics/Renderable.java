package com.graphics.engine.graphics;

/**
 * 在GL线程渲染时，执行的操作的封装对象
 */
public interface Renderable {
	
	/**
	 * 静态实例，不做渲染操作，用做哨兵
	 */
	public static final Renderable sInstance = new Renderable() {	//CHECKSTYLE IGNORE

		@Override
		public void run(long timeStamp, RenderContext context) {
		}
		
	};

	/**
	 * <br>功能简述: 在GL线程渲染时的操作
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param timeStamp	时间戳，一般来说没什么用
	 * @param context 渲染时的上下文，包括MVP矩阵等等一些参数
	 */
    public void run(long timeStamp, RenderContext context);
}
