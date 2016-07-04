package com.graphics.engine.gl.animation;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;


/**
 * 生成一些常用的插值器的工厂
 * @author dengweiming
 *
 */
public class InterpolatorFactory {
	/** 减速 */
	public final static int EASE_OUT = 0;
	/** 加速 */
	public final static int EASE_IN = 1;
	/** 先加速后减速 */
	public final static int EASE_IN_OUT = 2;
	
	
	/** 线性插值器，恒速 */
	public static final int LINEAR = 0;
	/** 指数曲线插值器，其速度变化很快 
	 * @param type 加速/减速/加减速
	 * @param pow 指数的幂系数，默认为10，仅在type=EASE_OUT时生效
	 * */
	public static final int EXPONENTIAL = 1;
	/** 粘性液体插值器，模拟物体在粘性液体中的运动效果 */
	public static final int VISCOUS_FLUID = 2;
	/** 模拟一次周期运动的插值器，其值变化为0.5->1->0->0.5，注意动画重复模式要用默认的RESTART */
	public static final int CYCLE_FORWARD = 3;
	/** 模拟一次周期运动的插值器，其值变化为0.5->0->1->0.5，注意动画重复模式要用默认的RESTART */
	public static final int CYCLE_BACKWARD = 4;
	/** 二次曲线 */
	public static final int QUADRATIC = 5;
	/** 三次曲线 */
	public static final int CUBIC = 6;
	/** 四次曲线 */
	public static final int QUARTIC = 7;
	/** 
	 * 多次回弹曲线，需要0个或2个参数：
	 * @param period	越小则弹的次数越多，弹出部分越多，[0..1]，推荐值0.25
	 * @param overshoot	过冲部分的比例，也会受<var>period<var>的限制，推荐值0.25
	 * */
	public static final int ELASTIC = 8;
	
	private static final float PI = (float) Math.PI;
	private static final float PI2 = PI * 2;
	
	
	/**
	 * 
	 * @param type	插值器类型
	 * @return 指定类型的（减速）插值器
	 */
	public static Interpolator getInterpolator(int type) {
		return getInterpolator(type, EASE_OUT, null);
	}
	
	/**
	 * 
	 * @param type	插值器类型
	 * @param ease	指定速度变化控制方法，参见{@link #EASE_OUT}, {@link #EASE_IN}, {@link #EASE_IN_OUT}（默认），部分插值器可能忽略它
	 * @return 指定类型的插值器
	 */
	public static Interpolator getInterpolator(int type, int ease) {
		return getInterpolator(type, ease, null);
	}
	
	/**
	 * 
	 * @param type	插值器类型
	 * @param ease	指定速度变化控制方法，参见{@link #EASE_OUT}, {@link #EASE_IN}, {@link #EASE_IN_OUT}（默认），部分插值器可能忽略它
	 * @param args	其他参数，可为 null
	 * @return 指定类型的插值器
	 */
	public static Interpolator getInterpolator(int type, int ease, float[] args) {
		switch (type) {
			case EXPONENTIAL :
				if (args != null && args.length > 0) {
					return new ExponentialInterpolator(ease, args[0]);
				} else {
					return new ExponentialInterpolator(ease);
				}
			case VISCOUS_FLUID :
				return sViscousFluidInterpolater;
			case CYCLE_FORWARD :
				return sCycleForwardInterpolator;
			case CYCLE_BACKWARD :
				return sCycleBackwardInterpolator;
			case QUADRATIC : {
				switch (ease) {
					case EASE_OUT :
						return sQuadraticEaseOutInterpolator;
					case EASE_IN :
						return sQuadraticEaseInInterpolator;
					case EASE_IN_OUT :
						return sQuadraticEaseInOutInterpolator;
				}
			}
				break;
			case CUBIC : {
				switch (ease) {
					case EASE_OUT :
						return sCubicEaseOutInterpolator;
					case EASE_IN :
						return sCubicEaseInInterpolator;
					case EASE_IN_OUT :
						return sCubicEaseInOutInterpolator;
				}
			}
				break;
			case QUARTIC : {
				switch (ease) {
					case EASE_OUT :
						return sQuarticEaseOutInterpolator;
					case EASE_IN :
						return sQuarticEaseInInterpolator;
					case EASE_IN_OUT :
						return sQuarticEaseInOutInterpolator;
				}
			}
				break;
			case ELASTIC : {
				switch (ease) {
					case EASE_OUT :
						if (args == null) {
							return sElasticEaseOutInterpolator;
						} else {
							if (args.length >= 2) {
								return new ElasticEaseOutInterpolator(args[0], args[1]);
							}
						}
					case EASE_IN :
					case EASE_IN_OUT :
						throw new RuntimeException("Elastic Interpolator only supports EaseOut now.");
				}
			}
				break;
		}
		return sLinearInterpolator;
	}
	
	/**
	 * 线性插值，没有边界检查
	 * @param a
	 * @param b
	 * @param t
	 * @return
	 */
	public static float lerp(float a, float b, float t) {
		return (b - a) * t + a;
	}
	
	/**
	 * 将[t1, t2]上的时间t重映射到[0, 1]区间上，有边界检查
	 * @param a
	 * @param b
	 * @param t
	 * @return
	 */
	public static float remapTime(float t1, float t2, float t) {
		if (t <= t1) {
			return 0;
		}
		if (t >= t2) {
			return 1;
		}
		return (t - t1) / (t2 - t1);
	}
	
	/**
	 * 将[t1, t2]上的时间t重映射到[0, 1]区间上，再对[a, b]插值，有边界检查
	 * @param a
	 * @param b
	 * @param t
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static float lerp(float a, float b, float t, float t1, float t2) {
		if (t <= t1) {
			return a;
		}
		if (t >= t2) {
			return b;
		}
		return (b - a) * (t - t1) / (t2 - t1) + a;
	}
	
	public static float quadraticEaseOut(float a, float b, float t) {
		return sQuadraticEaseOutInterpolator.getInterpolation(t) * (b - a) + a; 
	}
	
	public static float quadraticEaseIn(float a, float b, float t) {
		return sQuadraticEaseInInterpolator.getInterpolation(t) * (b - a) + a; 
	}
	
	public static float quadraticEaseInOut(float a, float b, float t) {
		return sQuadraticEaseInOutInterpolator.getInterpolation(t) * (b - a) + a; 
	}
	
	public static float cubicEaseOut(float a, float b, float t) {
		return sCubicEaseOutInterpolator.getInterpolation(t) * (b - a) + a; 
	}
	
	public static float cubicEaseIn(float a, float b, float t) {
		return sCubicEaseInInterpolator.getInterpolation(t) * (b - a) + a; 
	}
	
	public static float cubicEaseInOut(float a, float b, float t) {
		return sCubicEaseInOutInterpolator.getInterpolation(t) * (b - a) + a; 
	}
	
	public static float quarticEaseOut(float a, float b, float t) {
		return sQuarticEaseOutInterpolator.getInterpolation(t) * (b - a) + a; 
	}
	
	public static float quarticEaseIn(float a, float b, float t) {
		return sQuarticEaseInInterpolator.getInterpolation(t) * (b - a) + a; 
	}
	
	public static float quarticEaseInOut(float a, float b, float t) {
		return sQuarticEaseInOutInterpolator.getInterpolation(t) * (b - a) + a; 
	}
	
	public static float elasticEaseInOut(float a, float b, float t) {
		return sElasticEaseOutInterpolator.getInterpolation(t) * (b - a) + a; 
	}
	
	private static Interpolator sLinearInterpolator = new LinearInterpolator();
	
	/**
	 * 指数函数插值器，其速度变化很快
	 * @author dengweiming
	 *
	 */
	private static class ExponentialInterpolator implements Interpolator {
		int mEase = EASE_OUT;
		float mPow = 10;
		float mPowRatio; // 修正系数， 仅在减速插值时生效，数值为传入参数t = 1时，得出的interpolation时间的倒数 
		
		public ExponentialInterpolator(int ease, float pow) {
			mEase = ease;
			if (ease == EASE_OUT) {
				mPow = pow;
				mPowRatio =  1.0f / (1.0f - (float) Math.pow(2, -10)) - 1;
			}
		}
		
		/**
		 * 创建速度变化类型为{@link #EASE_OUT}的插值器
		 */
		public ExponentialInterpolator(int ease) {
			this(ease, 10);
		}
		
		@Override
		public float getInterpolation(float t) {
			// CHECKSTYLE:OFF 
			if (t <= 0) {
				return 0;
			}
			if (t >= 1) {
				return 1;
			}
			switch (mEase) {
				case EASE_OUT :
					final float tempPow = mPow + (10 - mPow) * t; 
					return (1 + mPowRatio * t) * (1 - (float) Math.pow(2, -tempPow * t));
				case EASE_IN :
					return (float) Math.pow(2, mPow * t - mPow);
				case EASE_IN_OUT :
					t *= 2;
					if (t < 1) {
						return (float) Math.pow(2, mPow * t - mPow) * 0.5f;
					} else {
						return 1 - (float) Math.pow(2, mPow - mPow * t) * 0.5f;
					}
			}
			// CHECKSTYLE:ON
			return t;
		}
	}
	

	private static Interpolator sViscousFluidInterpolater = new ViscousFluidInterpolater();

	/**
	 * 粘性液体插值器，模拟物体在粘性液体中的运动效果。
	 * 代码修改自{@link android.widget.Scroller}。
	 * @author dengweiming
	 *
	 */
	private static class ViscousFluidInterpolater implements Interpolator {
		final static float MidValue = 0.36787944117f;   // 1/e == exp(-1)	// CHECKSTYLE IGNORE
		
	    // This controls the viscous fluid effect (how much of it)
	    final static float sViscousFluidScale = 8.0f;	// CHECKSTYLE IGNORE
	    // must be set to 1.0 (used in viscousFluid())
	    static float sViscousFluidNormalize = 1.0f;
	    
	    
		static {
	    	// 在sViscousFluidNormalize为1时，计算y值范围，再取倒数作为y值规范化时的缩放比例
	        sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
		}
	    
	    /**
	     * 物体在粘性液体中的运动曲线函数
	     * @param x		插值时间[0, 1]
	     * @return
	     */
	    public static float viscousFluid(float x) {
	        x *= sViscousFluidScale;
	        float y;
	        if (x < 1.0f) {
	            y = x - (1.0f - (float) Math.exp(-x));	// 结果范围为[0, MidValue]	// CHECKSTYLE IGNORE
	        } else {
	            y = 1.0f - (float) Math.exp(1.0f - x);	// 结果范围为[0, 1)		// CHECKSTYLE IGNORE
	            y = MidValue + y * (1.0f - MidValue);	// 在[MidValue, 1]区间按y插值，结果范围为[0, 1)
	        }
	        return y * sViscousFluidNormalize;			// 规范化到[0, 1]
	    }
	    
		@Override
		public float getInterpolation(float t) {
			return viscousFluid(t);
		}    	
	}
	
	/**
	 * 模拟一次周期运动的插值器，其值变化为0.5->1->0->0.5，注意动画重复模式要用默认的RESTART
	 */
	private static Interpolator sCycleForwardInterpolator = new Interpolator() {
		@Override
		public float getInterpolation(float t) {
			return (float) Math.sin(t * (PI * 2)) * 0.5f + 0.5f;	// CHECKSTYLE IGNORE
		}
	};
	
	/**
	 * 模拟一次周期运动的插值器，其值变化为0.5->0->1->0.5，注意动画重复模式要用默认的RESTART
	 */
	private static Interpolator sCycleBackwardInterpolator = new Interpolator() {
		@Override
		public float getInterpolation(float t) {
			return (float) Math.sin((t + 0.5f) * (PI * 2)) * 0.5f + 0.5f;	// CHECKSTYLE IGNORE
		}
	};
	
	private static Interpolator sQuadraticEaseOutInterpolator = new Interpolator() {
		@Override
		public final float getInterpolation(float t) {
			return 1 - (1 - t) * (1 - t);
		}
	};
	
	private static Interpolator sQuadraticEaseInInterpolator = new Interpolator() {
		@Override
		public final float getInterpolation(float t) {
			return t * t;
		}
	};
	
	private static Interpolator sQuadraticEaseInOutInterpolator = new Interpolator() {
		@Override
		public final float getInterpolation(float t) {
			t *= 2;
			t = t < 1 ? t * t : 2 - (2 - t) * (2 - t);
			return t * 0.5f;	// CHECKSTYLE IGNORE
		}
	};
	
	private static Interpolator sCubicEaseOutInterpolator = new Interpolator() {
		@Override
		public final float getInterpolation(float t) {
			return 1 - (1 - t) * (1 - t) * (1 - t);
		}
	};
	
	private static Interpolator sCubicEaseInInterpolator = new Interpolator() {
		@Override
		public final float getInterpolation(float t) {
			return t * t * t;
		}
	};
	
	private static Interpolator sCubicEaseInOutInterpolator = new Interpolator() {
		@Override
		public final float getInterpolation(float t) {
			t *= 2;
			t = t < 1 ? t * t * t : 2 - (2 - t) * (2 - t) * (2 - t);
			return t * 0.5f;	// CHECKSTYLE IGNORE
		}
	};
	
	private static Interpolator sQuarticEaseOutInterpolator = new Interpolator() {
		@Override
		public final float getInterpolation(float t) {
			t = (1 - t) * (1 - t);
			return 1 - t * t;
		}
	};
	
	private static Interpolator sQuarticEaseInInterpolator = new Interpolator() {
		@Override
		public final float getInterpolation(float t) {
			t = t * t;
			return t * t;
		}
	};
	
	private static Interpolator sQuarticEaseInOutInterpolator = new Interpolator() {
		@Override
		public final float getInterpolation(float t) {
			t *= 2;
			if (t < 1) {
				t = t * t;
				t = t * t;
			} else {
				t = (2 - t) * (2 - t);
				t = 2 - t * t;
			}
			return t * 0.5f;	// CHECKSTYLE IGNORE
		}
	};
	
	//CHECKSTYLE IGNORE 1 LINES
	private static Interpolator sElasticEaseOutInterpolator = new ElasticEaseOutInterpolator(0.25f, 0.25f);
	
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2012-10-8]
	 */
	static class ElasticEaseOutInterpolator implements Interpolator {
		float mAmplitude;
		float mPeriod;
		float mPhase;
		
		/**
		 * @param period	越小则弹的次数越多，弹出部分越多，[0..1]，推荐值0.25
		 * @param overshoot	过冲部分的比例，也会受<var>period<var>的限制，推荐值0.25
		 */
		public ElasticEaseOutInterpolator(float period, float overshoot) {
			/*
				f(t) = pow(2, -10 * t) * a * sin(pi2 * (t / p - s)) + 1
				0 < p < 1 越小则弹的次数越多，弹出部分越多（横向缩放比例）
				a >= 1 越大弹出部分越大
				令 s = asin(1/a) / pi2 使得 f(他、
				overshoot = pow(2 , -5 * p) * a  （当p=0.3,a=1时，exceed=0.35）
				解得 a = overshoot * pow(2, 5 * p)
			*/
			
			mPeriod = Math.max(0, Math.min(period, 1));
			mAmplitude = Math.max(1, overshoot * (float) Math.pow(2, 10 * mPeriod * 0.5));	// CHECKSTYLE IGNORE
			mPhase = (float) Math.asin(1 / mAmplitude) / PI2;
		}
		
		@Override
		public float getInterpolation(float t) {
			if (t <= 0) {
				return 0;
			}
			if (t >= 1) {
				return 1;
			}
			return (float) (Math.pow(2, -10 * t) * mAmplitude * Math.sin(PI2 * (t / mPeriod - mPhase)) + 1);	// CHECKSTYLE IGNORE
		}
		
	}

}


