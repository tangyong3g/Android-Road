package com.graphics.engine.gl.scroller.effector.subscreeneffector;


import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.scroller.ScreenScroller;
import com.graphics.engine.gl.scroller.ScreenScrollerEffector;
import com.graphics.engine.gl.scroller.ScreenScrollerListener;

/**
 * 只绘制当前两屏的特效类的工厂。
 * <br> 使用{@link #setType(int)}设置预定义的特效种类。
 * <br> 使用者也可以扩展这个类，增加type常量，实现更多种类的特效。
 * @author dengweiming
 *
 */
public class SubScreenEffector implements ScreenScrollerEffector {

	/** 低质量绘图 */
	public final static int DRAW_QUALITY_LOW = 0;
	/** 中等质量绘图 */
	public final static int DRAW_QUALITY_MID = 1;
	/** 高等质量绘图 */
	public final static int DRAW_QUALITY_HIGH = 2;
	
	public final static int MAXOVERSHOOTPERCENT = 100;

	public final static int EFFECTOR_TYPE_RANDOM = -1;
	public final static int EFFECTOR_TYPE_RANDOM_CUSTOM = -2;
	public final static int EFFECTOR_TYPE_DEFAULT = 0;

	//	public final static int EFFECTOR_TYPE_CHORD = 10;
	//	public final static int EFFECTOR_TYPE_ZOOM = 11;
	
	public final static int EFFECTOR_TYPE_ROLL = 21;
	public final static int EFFECTOR_TYPE_WINDMILL = 22;

	public final static int EFFECTOR_TYPE_BOUNCE = 23;
	public final static int EFFECTOR_TYPE_BULLDOZE = 24;
	public final static int EFFECTOR_TYPE_CUBOID1 = 25;
	public final static int EFFECTOR_TYPE_FLIP = 26;
	public final static int EFFECTOR_TYPE_WAVE = 27;
	public final static int EFFECTOR_TYPE_FLYAWAY = 28;
	public final static int EFFECTOR_TYPE_CUBOID = 29;
	public final static int EFFECTOR_TYPE_FLIP2 = 30;
	public final static int EFFECTOR_TYPE_ALPHA = 31;
	public final static int EFFECTOR_TYPE_DOCK_FLIP = 32;
	public final static int EFFECTOR_TYPE_STACK = 33;
	public final static int EFFECTOR_TYPE_CUBOID2 = 34;
	
	public final static int EFFECTOR_TYPE_FLIP3D = 35;
	public final static int EFFECTOR_TYPE_CRYSTAL = 36;
	public final static int EFFECTOR_TYPE_CLOTH = 37;
	public final static int EFFECTOR_TYPE_ORIGAMI = 40;

	//往后新增特效不再在此处添加Key, 而在项目中的SubScreenEffectorImp
//	public final static int EFFECTOR_TYPE_CARDSCALE = 38;
//	public final static int EFFECTOR_TYPE_WAVE_FLIP = 39;

	protected SubScreenContainer mContainer;
	protected ScreenScroller mScroller;
	protected MSubScreenEffector mEffector;
	protected MSubScreenEffector[] mRandomEffectors;
	protected int mCurrentIndex;
	protected int mType;
	protected int mBackgroundColor = 0x00000000;
	protected int mScreenSize;
	protected int mOrientation;
	protected int mQuality;
	protected int mGap;
	protected int mTopPadding;
	protected boolean mVerticalSlide = false; //是否支持上下滑动

	public SubScreenEffector(ScreenScroller scroller) {
		assert scroller != null; // 如果为null也就没任何意义了
		mScroller = scroller;
		mScroller.setEffector(this);
	}

	@Override
	public boolean onDraw(GLCanvas canvas) {
		final int scroll = mScroller.getScroll();
		boolean bgDrawn = mScroller.isBackgroundAlwaysDrawn();
		boolean combinebg = mEffector != null && mEffector.isCombineBackground();
		if (!combinebg) {
			bgDrawn |= mScroller.drawBackground(canvas, scroll);
		}
		if (!bgDrawn) {
			if (mBackgroundColor != 0x00000000) {
				canvas.drawColor(mBackgroundColor);
			}
		}
		int curOffset = mScroller.getCurrentScreenOffset();
		int offset = curOffset;
		if (offset > 0) {
			offset -= mScreenSize;
		}
		
		final int screenA = mScroller.getDrawingScreenA();
		final int screenB = mScroller.getDrawingScreenB();
		if (mScroller.isFinished()) {
			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA, offset, bgDrawn,
					mVerticalSlide);
		} else if (mEffector == null) {
			float offsetFloat = mScroller.getCurrentScreenDrawingOffset(true);
			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA, offsetFloat, bgDrawn,
					mVerticalSlide);
			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenB, offsetFloat
					+ mScreenSize, bgDrawn, mVerticalSlide);
		} else {
			mEffector.onScrollChanged(scroll, curOffset);
			if (mEffector.toReverse()) {
				mEffector.drawView(canvas, screenB, offset + mScreenSize, false);
				mEffector.drawView(canvas, screenA, offset, true);
			} else {
				mEffector.drawView(canvas, screenA, offset, true);
				mEffector.drawView(canvas, screenB, offset + mScreenSize, false);
			}
		}
		return true;
	}

	@Override
	public void setType(int type) {
		MSubScreenEffector oldEffector = mEffector;
		if (type <= EFFECTOR_TYPE_RANDOM) {
			if (mRandomEffectors == null) {
				mRandomEffectors = new MSubScreenEffector[] {
				//XXX
				//						new BounceEffector(), 
				//						new BulldozeEffector(), 
				//						new CuboidInsideEffector(), 
				//						new CuboidOutsideEffector(), 
				//						new FlipEffector(), 	// not contain Flip2Effector
				//						new RollEffector(), 
				//						new WaveEffector(), 
				//						new WindmillEffector(),
				};
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
			//XXX
				case EFFECTOR_TYPE_BOUNCE :
					mEffector = new BounceEffector();
					break;
			//				case EFFECTOR_TYPE_BULLDOZE:
			//					mEffector = new BulldozeEffector();
			//					break;
				case EFFECTOR_TYPE_CUBOID1 :
					mEffector = new CuboidInsideEffector();
					break;
				case EFFECTOR_TYPE_CUBOID2 :
					mEffector = new CuboidOutsideEffector();
					break;
				case EFFECTOR_TYPE_FLIP :
					mEffector = new FlipEffector();
					break;
				case EFFECTOR_TYPE_FLIP2:
					mEffector = new Flip2Effector();
					break;
				//				case EFFECTOR_TYPE_ROLL:
				//					mEffector = new RollEffector();
				//					break;
				case EFFECTOR_TYPE_WAVE :
					mEffector = new WaveEffector();
					break;
				case EFFECTOR_TYPE_WINDMILL :
					mEffector = new WindmillEffector();
					break;
				//				case EFFECTOR_TYPE_CHORD:
				//					mEffector = new ChordScreenEffector();
				//					break;
				//				case EFFECTOR_TYPE_ZOOM:
				//					mEffector = new ZoomScreenEffector();
				//					break;
				case EFFECTOR_TYPE_FLYAWAY :
					mEffector = new FlyAwayScreenEffectorForAppDrawer();
					break;
				case EFFECTOR_TYPE_CUBOID :
					mEffector = new CuboidScreenEffector();
					break;
				case EFFECTOR_TYPE_ALPHA :
					mEffector = new AlphaEffector();
					break;
				case EFFECTOR_TYPE_DOCK_FLIP :
					mEffector = new DockFlipEffector();
					break;
				case EFFECTOR_TYPE_STACK :
					mEffector = new StackEffector();
					break;
				case EFFECTOR_TYPE_BULLDOZE :
					mEffector = new BulldozeEffector();
					break;
//				case EFFECTOR_TYPE_CARDSCALE :
//					mEffector = new CardScaleEffector();
//					break;
//					
//				case EFFECTOR_TYPE_WAVE_FLIP :
//					mEffector = new WaveFlipEffector();
//					break;
				default :
					mEffector = null;
					break;
			}
		}
		if (oldEffector != mEffector) {
			if (oldEffector != null) {
				oldEffector.onDetach();
			}
			if (mEffector != null) {
				mEffector.setDrawQuality(mQuality);
				mEffector.onAttach(mContainer, mScroller);
			} else {
				mScroller.setOvershootPercent(MAXOVERSHOOTPERCENT);
			}
		}
	}

	@Override
	public void updateRandomEffect() {
		if (mType == EFFECTOR_TYPE_RANDOM) {
			setType(EFFECTOR_TYPE_RANDOM);
		}
	}

	@Override
	public void onSizeChanged(int w, int h, int orientation) {
		mOrientation = mScroller.getOrientation();
		mScreenSize = mScroller.getScreenSize();
		if (mEffector != null) {
			mEffector.onSizeChanged();
		}

	}

	@Override
	public int getMaxOvershootPercent() {
		return mEffector == null ? MAXOVERSHOOTPERCENT : mEffector.getMaxOvershootPercent();
	}

	@Override
	public void onAttach(ScreenScrollerListener container) {
		if (container != null && container instanceof SubScreenContainer) {
			ScreenScroller scroller = container.getScreenScroller();
			mContainer = (SubScreenContainer) container;
			if (scroller == null) {
				throw new IllegalArgumentException("Container has no ScreenScroller.");
			} else if (mScroller != scroller) {
				mScroller = scroller;
				mOrientation = mScroller.getOrientation();
				mScreenSize = mScroller.getScreenSize();
				int oldType = mType;
				mType = EFFECTOR_TYPE_DEFAULT;
				mEffector = null;
				setType(oldType);
			}
		} else {
			throw new IllegalArgumentException(
					"container is not an instance of SubScreenEffector.SubScreenContainer");
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

	// TODO:GridScreenEffector需要这样的参数，提到接口层
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
	public MSubScreenEffector getEffector() {
		return mEffector;
	}
}
