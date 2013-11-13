package com.ty.tools.path;

public class NumberUtils {
	/** When false, the mask in {@link #intToFloatColor(int)} will not be used. This must only be used when the resulting floats
	 * will not be used with {@link #floatToIntColor(float)}. */
	static public boolean intToFloatColorMask = true;

	public static int floatToIntBits (float value) {
		return Float.floatToIntBits(value);
	}

	public static int floatToRawIntBits (float value) {
		return Float.floatToRawIntBits(value);
	}

	public static int floatToIntColor (float value) {
		return Float.floatToRawIntBits(value);
	}

	/** Encodes the ABGR int color as a float. The high bits are masked to avoid using floats in the NaN range, which unfortunately
	 * means the full range of alpha cannot be used. See {@link Float#intBitsToFloat(int)} javadocs. */
	public static float intToFloatColor (int value) {
		return Float.intBitsToFloat(intToFloatColorMask ? (value & 0xfeffffff) : value);
	}

	public static float intBitsToFloat (int value) {
		return Float.intBitsToFloat(value);
	}

	public static long doubleToLongBits (double value) {
		return Double.doubleToLongBits(value);
	}

	public static double longBitsToDouble (long value) {
		return Double.longBitsToDouble(value);
	}
}
