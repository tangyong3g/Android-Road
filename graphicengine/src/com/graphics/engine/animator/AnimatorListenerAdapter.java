package com.graphics.engine.animator;

import com.graphics.engine.animator.Animator.AnimatorListener;

/**
 * <br>类描述: 动画监听者的适配器
 * <br>功能详细描述:
 * 提供了{@link AnimatorListener}各个接口的空的实现。自定义监听器可以继承它，则可以只重写那些需要的接口。
 * 
 * @author  dengweiming
 * @date  [2013-10-17]
 */
public abstract class AnimatorListenerAdapter implements Animator.AnimatorListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationCancel(Animator animation) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationEnd(Animator animation) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAnimationStart(Animator animation) {
    }

}
