package com.graphics.engine.graphics;

import com.graphics.engine.util.Pool;
import com.graphics.engine.util.Poolable;
import com.graphics.engine.util.PoolableManager;
import com.graphics.engine.util.Pools;

/**
 * @hide
 * <br>类描述:包含渲染信息的链表节点
 * <br>功能详细描述:
 * 基于内存池实现。每个节点可以除了指向下一个节点，还可以指向一个分支节点，用于实现嵌套链表。
 * 不过目前使用嵌套链表并没有作进一步的优化功能（例如一个视图没有更改时直接使用原来的链表，而是每次都生成）。
 * 
 * @author  dengweiming
 * @date  [2012-9-6]
 */
public final class RenderInfoNode implements Poolable<RenderInfoNode> {
	
	private static final int POOL_LIMIT = 2048;
	public static final int STACK_LIMIT = 128;
	

	//CHECKSTYLE IGNORE 1 LINES
	private final static RenderInfoNode[] sRenderInfoNodeStack = new RenderInfoNode[RenderInfoNode.STACK_LIMIT];
	
	//CHECKSTYLE IGNORE 1 LINES
	private static final Pool<RenderInfoNode> sRenderInfoNodePool = 
			Pools.finitePool(new PoolableManager<RenderInfoNode>() {
				public RenderInfoNode newInstance() {
					RenderInfoNode node = new RenderInfoNode();
					return node;
				}
				
				public void onAcquired(RenderInfoNode element) {
				}
				
				public void onReleased(RenderInfoNode element) {
				}
			}, POOL_LIMIT);
	
    private RenderInfoNode mNext;		//right sibling
    
    RenderInfoNode mFork;				//left child
    public Renderable mRenderable;		//self
    public RenderContext mContext;
    public int mSn;
    public int mVertexPosition;
    
    public RenderInfoNode() {
    	mRenderable = Renderable.sInstance;
    }

    public void setNextPoolable(RenderInfoNode element) {
        mNext = element;
    }

    public RenderInfoNode getNextPoolable() {
        return mNext;
    }
    
    public void setForkNode(RenderInfoNode element) {
    	mFork = element;
    }
    
    public RenderInfoNode getForkNode() {
    	return mFork;
    }
    
    public void setNextNode(RenderInfoNode element) {
    	//和setNextPoolable方法一样，只是换个名字更适合其意义
    	mNext = element;
    }
    
    public RenderInfoNode getNextNode() {
    	//和getNextPoolable方法一样，只是换个名字更适合其意义
    	return mNext;
    }

    public void release() {
    	resetNodeTree(this);
    	sRenderInfoNodePool.release(this);
    }
    
    public void reset() {
    	resetNodeTree(this);
    }
    
    public static RenderInfoNode acquire() {
    	return sRenderInfoNodePool.acquire();
    }
    
    public RenderInfoNode acquireNext() {
    	RenderInfoNode next = sRenderInfoNodePool.acquire();
    	setNextNode(next);
    	return next;
    }
    
    private static void resetNodeTree(RenderInfoNode rootNode) {
    	final RenderInfoNode[] stack = sRenderInfoNodeStack;
    	int count = 0;
    	
    	rootNode.mRenderable = Renderable.sInstance;
		if (rootNode.mContext != null) {
			rootNode.mContext.release();
			rootNode.mContext = null;
		}
		if (rootNode.mNext != null) {
			stack[count++] = rootNode.mNext;		//push
			rootNode.mNext = null;
		}
		if (rootNode.mFork != null) {
			stack[count++] = rootNode.mFork;		//push
			rootNode.mFork = null;
		}
		
		while (count > 0) {
			RenderInfoNode node = stack[--count];	//pop
			stack[count] = null;
			
			node.mRenderable = Renderable.sInstance;
			if (node.mContext != null) {
				node.mContext.release();
				node.mContext = null;
			}
			if (node.mNext != null) {
				stack[count++] = node.mNext;		//push
				node.mNext = null;
			}
			if (node.mFork != null) {
				stack[count++] = node.mFork;		//push
				node.mFork = null;
			}
			sRenderInfoNodePool.release(node);
		}
    }

}

