package com.graphics.engine.gl.scroller.effector.gridscreeeneffector;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.scroller.ScreenScroller;
import com.go.gl.scroller.ScreenScrollerEffector;
import com.go.gl.scroller.ScreenScrollerListener;

import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * 只绘制当前两屏网格的特效类的工厂。
 * <br> 使用{@link #setType(int)}设置预定义的特效种类。
 * <br> 使用者也可以扩展这个类，增加type常量，实现更多种类的特效。
 * @author dengweiming
 *
 */
public class GridScreenEffector implements ScreenScrollerEffector {
	/** 低质量绘图 */
	public final static int DRAW_QUALITY_LOW = 0;
	/** 中等质量绘图 */
	public final static int DRAW_QUALITY_MID = 1;
	/** 高等质量绘图 */
	
	public final static int DRAW_QUALITY_HIGH = 2;
	
	public final static int GRID_EFFECTOR_TYPE_SCRIBBLE = -2;
	public final static int GRID_EFFECTOR_TYPE_RANDOM = -1;
	public final static int GRID_EFFECTOR_TYPE_DEFAULT = 0;
	
	public final static int GRID_EFFECTOR_TYPE_BINARY_STAR = 1;
	public final static int GRID_EFFECTOR_TYPE_CHARIOT = 2;
	public final static int GRID_EFFECTOR_TYPE_SHUTTER = 3;
	public final static int GRID_EFFECTOR_TYPE_CHORD = 4;
	public final static int GRID_EFFECTOR_TYPE_CYLINDER = 5;
	public final static int GRID_EFFECTOR_TYPE_SPHERE = 6;

	public final static int GRID_EFFECTOR_TYPE_ZOOM = 7;
	public final static int GRID_EFFECTOR_TYPE_FLYAWAY = 8;
	public final static int GRID_EFFECTOR_TYPE_CYLINDER2 = 9;
	
	public final static int GRID_EFFECTOR_TYPE_FLIP = 10;	
	//往后新增特效不再在此处添加Key, 而在项目中的GridScreenEffectorImp

	public final static Interpolator DECELERATEINTERPOLATOR3 = new DecelerateInterpolator(1.5f); //CHECKSTYLE OFF
	public final static Interpolator DECELERATEINTERPOLATOR5 = new DecelerateInterpolator(2.5f); //CHECKSTYLE OFF

	protected GridScreenContainer mContainer;
	protected ScreenScroller mScroller;
	protected int mOrientation;
	protected int mScreenSize;
	protected MGridScreenEffector[] mRandomEffectors;
	protected MGridScreenEffector mEffector;
	protected int mCurrentIndex;
	protected int mType;
	protected int mQuality;
	protected int mGap;
	protected int mTopPadding;
	protected boolean mVerticalSlide = false;
	protected int mBackgroundColor = 0x00000000;

	public GridScreenEffector(ScreenScroller scroller) {
		assert scroller != null; // 如果为null也就没任何意义了
		mScroller = scroller;
		mScroller.setEffector(this);
	}

	@Override
	public boolean onDraw(GLCanvas canvas) {
		if (mOrientation == ScreenScroller.VERTICAL) {
			return false; // 暂时不支持垂直滚屏特效
		}

		final int screenA = mScroller.getDrawingScreenA();
		final int screenB = mScroller.getDrawingScreenB();
		final int scroll = mScroller.getScroll() + mGap * 2;

		boolean bgDrawn = mScroller.isBackgroundAlwaysDrawn();
		boolean combinebg = mEffector != null && mEffector.isCombineBackground();
		if (!combinebg) {
			bgDrawn |= mScroller.drawBackground(canvas, mScroller.getScroll());
		}
		if (!bgDrawn) {
			if (mBackgroundColor != 0x00000000) {
				canvas.drawColor(mBackgroundColor);
			}
		}

		int offset = mScroller.getCurrentScreenOffset();
		if (offset > 0) {
			offset -= mScreenSize;
		}
		//		offset += mGap * 2;

		final int top = mTopPadding;

		if (offset == 0 && mScroller.getCurrentDepth() == 0) {
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA - 1, offset
					- mScreenSize, top, scroll);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
					scroll);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA + 1, offset
					+ mScreenSize, top, scroll);
			MGridScreenEffector.drawScreen(mContainer, canvas, mScroller, screenA, offset, top,
					scroll);
		} else if (mEffector == null) {
			MGridScreenEffector.drawScreen(mContainer, canvas, mScroller, screenA, offset, top,
					scroll);
			MGridScreenEffector.drawScreen(mContainer, canvas, mScroller, screenB, offset
					+ mScreenSize, top, scroll);
		} else {
			
			canvas.save();
			
			// 适配float和int型两种绘制方式
			if (mEffector.isFloatAdapted()) {
				drawEffectorByFloat(canvas, screenA, screenB, top, offset, scroll);
			} else {
				drawByEffectorInt(canvas, screenA, screenB, top, offset, scroll);
			}
			canvas.restore();
		}
		return true;
	}
	
	private void drawEffectorByFloat(GLCanvas canvas, int screenA, int screenB, int top, int offset, int scroll) {
		float offsetFloat = getCurrentScreenDrawingOffset();
		float scrollFloat = mScroller.getScrollFloat();
		if (mEffector.isCurrentScreenOnTop() && screenA == mScroller.getCurrentScreen()) {
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenB, offset
					+ mScreenSize, top, scroll);
			mEffector.drawScreen(canvas, screenB, offsetFloat + mScreenSize, top, scrollFloat);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
					scroll);
			mEffector.drawScreen(canvas, screenA, offsetFloat, top, scrollFloat);
		} else {
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
					scroll);
			mEffector.drawScreen(canvas, screenA, offsetFloat, top, scrollFloat);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenB, offset
					+ mScreenSize, top, scroll);
			mEffector.drawScreen(canvas, screenB, offsetFloat + mScreenSize, top, scrollFloat);
		}
	}
	
	private void drawByEffectorInt(GLCanvas canvas, int screenA, int screenB, int top, int offset, int scroll) {
		if (mEffector.isCurrentScreenOnTop() && screenA == mScroller.getCurrentScreen()) {
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenB, offset
					+ mScreenSize, top, scroll);
			mEffector.drawScreen(canvas, screenB, offset + mScreenSize, top, scroll);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
					scroll);
			mEffector.drawScreen(canvas, screenA, offset, top, scroll);
		} else {
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
					scroll);
			mEffector.drawScreen(canvas, screenA, offset, top, scroll);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenB, offset
					+ mScreenSize, top, scroll);
			mEffector.drawScreen(canvas, screenB, offset + mScreenSize, top, scroll);
		}
	}
	
	
	private float getCurrentScreenDrawingOffset() {
		float offset = mScroller.getCurrentScreenOffsetFloat();
		if (mScroller.getCurrentScreenOffset() > 0) {
			offset -= mScreenSize;
		}
		return offset;
	}

	@Override
	public void setType(int type) {
		MGridScreenEffector oldEffector = mEffector;
		if (type == GRID_EFFECTOR_TYPE_RANDOM) {
			if (mType != type) {
				mRandomEffectors = new MGridScreenEffector[] { new BinaryStarEffector(),
						new ChariotEffector(), new ShutterEffector(), new ChordEffector(),
						new CylinderEffector(), new SphereEffector(), new ZoomEffector(),
						new FlyAwayEffector() };
				mCurrentIndex = -1;
			}
			mType = type;
			int index = (int) (Math.random() * mRandomEffectors.length);
			if (index == mCurrentIndex) {
				index = (index + 1) % mRandomEffectors.length;
			}
			mEffector = mRandomEffectors[index];
			mCurrentIndex = index;
		} else if (mType == type) {
			return;
		} else {
			mType = type;
			mRandomEffectors = null;
			switch (type) {
				case GRID_EFFECTOR_TYPE_BINARY_STAR :
					mEffector = new BinaryStarEffector();
					break;
				case GRID_EFFECTOR_TYPE_CHARIOT :
					mEffector = new ChariotEffector();
					break;
				case GRID_EFFECTOR_TYPE_SHUTTER :
					mEffector = new ShutterEffector();
					break;
				case GRID_EFFECTOR_TYPE_CHORD :
					mEffector = new ChordEffector();
					break;
				case GRID_EFFECTOR_TYPE_CYLINDER :
					mEffector = new CylinderEffector();
					break;
				case GRID_EFFECTOR_TYPE_SPHERE :
					mEffector = new SphereEffector();
					break;
				//	    		case GRID_EFFECTOR_TYPE_SCRIBBLE:
				//	    			mEffector = new ScribbleEffector();
				//	    			break;
				case GRID_EFFECTOR_TYPE_ZOOM :
					mEffector = new ZoomEffector();
					break;
				case GRID_EFFECTOR_TYPE_FLYAWAY :
					mEffector = new FlyAwayEffector();
					break;
				case GRID_EFFECTOR_TYPE_CYLINDER2 :
					mEffector = new Cylinder2Effector();
					break;
				case GRID_EFFECTOR_TYPE_FLIP :
					mEffector = new GridFlipEffector();
					break;
				default :
					mEffector = null;
					break;
			}
		}
		mScroller.setInterpolator(mEffector == null
				? DECELERATEINTERPOLATOR3
				: DECELERATEINTERPOLATOR5);
		if (oldEffector != mEffector) {
			if (oldEffector != null) {
				oldEffector.onDetach();
			}
			if (mEffector != null) {
				mEffector.setDrawQuality(mQuality);
				mEffector.onAttach(mContainer, mScroller);
			}
		}
	}

	@Override
	public void updateRandomEffect() {
		if (mType == GRID_EFFECTOR_TYPE_RANDOM) {
			setType(GRID_EFFECTOR_TYPE_RANDOM);
		}
	}

	@Override
	public void onSizeChanged(int w, int h, int orientation) {
		mOrientation = mScroller.getOrientation();
		mScreenSize = mScroller.getScreenSize();
		if (mEffector != null) {
			mEffector.onSizeChanged(w, h);
		}

	}

	@Override
	public int getMaxOvershootPercent() {
		return 0;
	}

	@Override
	public void onAttach(ScreenScrollerListener container) {
		if (container != null && container instanceof GridScreenContainer) {
			ScreenScroller scroller = container.getScreenScroller();
			mContainer = (GridScreenContainer) container;
			if (scroller == null) {
				throw new IllegalArgumentException("Container has no ScreenScroller.");
			} else if (mScroller != scroller) {
				mScroller = scroller;
				mOrientation = mScroller.getOrientation();
				mScreenSize = mScroller.getScreenSize();
				int oldType = mType;
				mType = GRID_EFFECTOR_TYPE_DEFAULT;
				mEffector = null;
				setType(oldType);
			}
		} else {
			throw new IllegalArgumentException(
					"container is not an instance of GridScreenEffector.GridScreenContainer");
		}

	}

	@Override
	public void onDetach() {
		mContainer = null;
		mScroller = null;
		//		mRandomEffectors = null;
		if (mEffector != null) {
			mEffector.onDetach();
		}
	}

	@Override
	public void setDrawQuality(int quality) {
		mQuality = quality;
		if (mEffector != null) {
			mEffector.setDrawQuality(quality);
		}
	}

	@Override
	public void recycle() {
		mRandomEffectors = null;
	}

	@Override
	public void setScreenGap(int gap) {
		mGap = gap;
	}

	@Override
	public void setTopPadding(int top) {
		mTopPadding = top;
	}

	@Override
	public void setVerticalSlide(boolean verticalSlide) {
		mVerticalSlide = verticalSlide;
		if (mEffector != null) {
			mEffector.setVerticalSlide(verticalSlide);
		}
	}

	@Override
	public boolean isAnimationing() {
		return mEffector != null && mEffector.isAnimationing();
	}


	@Override
	public boolean isNeedEnableNextWidgetDrawingCache() {
		return mEffector == null ? true : mEffector
				.isNeedEnableNextWidgetDrawingCache();
	}

	@Override
	public boolean disableWallpaperScrollDelay() {
		return mEffector == null ? false : mEffector
				.disableWallpaperScrollDelay();
	}

	@Override
	public void onScrollStart() {
		if (mEffector != null) {
			mEffector.onScrollStart();
		}
	}

	@Override
	public void onScrollEnd() {
		if (mEffector != null) {
			mEffector.onScrollEnd();
		}
	}

	@Override
	public void onFlipStart() {
		if (mEffector != null) {
			mEffector.onFlipStart();
		}
	}

	@Override
	public void onFlipInterupted() {
		if (mEffector != null) {
			mEffector.onFlipInterupted();
		}
	}

	@Override
	public void onThemeSwitch() {
		if (mEffector != null) {
			mEffector.onThemeSwitch();
		}
	}

	@Override
	public void cleanup() {
		if (mEffector != null) {
			mEffector.cleanup();
		}
	}

	@Override
	public MGridScreenEffector getEffector() {
		return mEffector;
	}
}
