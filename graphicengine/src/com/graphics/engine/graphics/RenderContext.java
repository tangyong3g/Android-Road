package com.graphics.engine.graphics;

import com.graphics.engine.util.Pool;
import com.graphics.engine.util.Poolable;
import com.graphics.engine.util.PoolableManager;
import com.graphics.engine.util.Pools;

/**
 * 
 * <br>类描述: 渲染时的上下文
 * <br>功能详细描述:
 * 居于内存池实现，使用静态方法{@link #acquire()}来获得一个实例
 * 
 * @author  dengweiming
 * @date  [2012-9-18]
 */
public final class RenderContext implements Poolable<RenderContext> {
	
	private static final int POOL_LIMIT = 1024;
	private static final int COLOR_COMPONENT = 4;
	private static final int MATRIX_COMPONENT = 16;
	
	//CHECKSTYLE IGNORE 1 LINES
	static final Pool<RenderContext> sPool = 
			Pools.finitePool(new PoolableManager<RenderContext>() {
				public RenderContext newInstance() {
					return new RenderContext();
				}
				
				public void onAcquired(RenderContext element) {
					element.mReleased = false;
				}
				
				public void onReleased(RenderContext element) {
				}
			}, POOL_LIMIT);
	
	/**
	 * 获取一个实例。注意使用完调用{@link #release()}释放。
	 */
	public static RenderContext acquire() {
		return sPool.acquire();
	}
	
    private RenderContext mNext;
    boolean mReleased;
    
    //CHECKSTYLE IGNORE 5 LINES
    public float alpha;
    public Texture texture;
    public GLShaderProgram shader;
    public final float[] color = new float[COLOR_COMPONENT];
    public final float[] matrix = new float[MATRIX_COMPONENT];
    
    public void setNextPoolable(RenderContext element) {
        mNext = element;
    }

    public RenderContext getNextPoolable() {
        return mNext;
    }
    
    void reset() {
    	texture = null;
    	shader = null;
    }

    /**
     * 释放对象。
     */
    public void release() {
		if (mReleased) {
			//因为允许一个实例进入GLCanvas的列表多次，所以可能会多次释放，
			//但是要只处理第一次，否则会破坏内存池的链表状态
			return;
		}
		mReleased = true;
    	//reset
    	texture = null;
    	shader = null;
    	
    	sPool.release(this);
    }
    
}