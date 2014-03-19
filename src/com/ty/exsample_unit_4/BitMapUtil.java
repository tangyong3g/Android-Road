package com.ty.exsample_unit_4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;

/**
 * 图像处理
 * 
 * @author tangyong
 * 
 */
//CHECKSTYLE:OFF
public class BitMapUtil {

	/**
	 * 调整bitMap的色相值
	 * 
	 * @param bm
	 *            原来图的bitMap
	 * @param hue
	 *            在设置的色相值 值在 -180 - 180之间.
	 * @return {@link Bitmap} 重新处理后的bitMap
	 */
	public static Bitmap convertBitMapWithHue(Bitmap bm, float hue) {

		ColorMatrix hueMatrix = new ColorMatrix();
		Bitmap bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);

		// 创建一个相同尺寸的可变的位图区,用于绘制调色后的图片

		Canvas canvas = new Canvas(bmp); // 得到画笔对象
		Paint paint = new Paint(); // 新建paint
		paint.setAntiAlias(true); // 设置抗锯齿,也即是边缘做平滑处理

		// hueColor就是色轮旋转的角度,正值表示顺时针旋转，负值表示逆时针旋转
		hueMatrix.reset();

		ColorFilterGenerator.adjustHue(hueMatrix, hue);
		paint.setColorFilter(new ColorMatrixColorFilter(hueMatrix.getArray()));// 设置颜色变换效果
		canvas.drawBitmap(bm, 0, 0, paint); // 将颜色变化后的图片输出到新创建的位图区

		//回收原来的位图
		if (!bm.isRecycled()) {
			bm.recycle();
		}

		// 返回新的位图，也即调色处理后的图片
		return bmp;
	}

	/**
	 * 把bitMap转化成Texture
	 * 
	 * @param bm
	 *            bitMap
	 * @param quality
	 *            要转化的质量 0 -100
	 * @return
	 */
	public static Texture convertBitmapToTexture(Bitmap bm, int quality) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] data = baos.toByteArray();

		//原来 的bitMap回收掉
		bm.recycle();
		Texture tx = new Texture(new Pixmap(data, 0, data.length));

		return tx;
	}

	/**
	 * 
	 * @param fileHandler
	 * @param hue
	 * @param bitmap
	 * @return
	 */
	public static TextureAttribute changeHueToAttr(FileHandle fileHandler, float hue, Bitmap bitmap) {
		//把bitmap 做hue的转化
		bitmap = BitMapUtil.convertBitMapWithHue(bitmap, hue);
		//把bitmap转化成Texture 
		Texture texture = BitMapUtil.convertBitmapToTexture(bitmap, 100);
		//把texture转化成textAttri
		TextureAttribute textureAttribute = new TextureAttribute(TextureAttribute.Diffuse, texture);
		return textureAttribute;
	}

	public static Texture changeHueToTx( float hue, Bitmap bitmap) {
		//把bitmap 做hue的转化
		bitmap = BitMapUtil.convertBitMapWithHue(bitmap, hue);
		//把bitmap转化成Texture 
		Texture texture = BitMapUtil.convertBitmapToTexture(bitmap, 100);
		return texture;
	}

	/**
	 * 
	 * -- TODO error 方法无法正常工作，目前还不知道原因
	 * 
	 * 把 Texture 转化成 Bitmap
	 * 
	 * @param texture {@link Texture}
	 * @return {@link Bitmap}
	 */
	public static Bitmap convertTextureToBitmap(Texture texture) {

		//得到纹理数据
		TextureData data = texture.getTextureData();
		data.prepare();
		//转化成pixmap
		Pixmap pixmap = data.consumePixmap();
		//得到数据缓冲区
		ByteBuffer bf = pixmap.getPixels();
		// 创建一张位图
		Bitmap bitMap = Bitmap.createBitmap(pixmap.getWidth(), pixmap.getHeight(),
				Bitmap.Config.ARGB_8888);
		//从缓冲区中读取数据 
		bitMap.copyPixelsFromBuffer(bf);

		return bitMap;
	}

	/**
	 * 把文件转成BitMap
	 * 
	 * @param context 上下文
	 * @param filePath
	 * @return {@link Bitmap}
	 */
	public static Bitmap converFileToBitMapWithFilePath(Context context, String filePath) {

		Bitmap result = null;

		try {

			InputStream is = context.getAssets().open(filePath);
			result = BitmapFactory.decodeStream(is);

		} catch (IOException e) {

		}

		return result;
	}

	/**
	 * 把图片直接做hue的变化
	 * 
	 * @param context 上下文
	 * @param filePath 文件
	 * @param hue	色相值
	 * @return	Texture
	 */
	public static Texture converTextureWithHue(Context context, String filePath, float hue) {

		Texture result = null;
		Bitmap bitMap = converFileToBitMapWithFilePath(context, filePath);

		bitMap = convertBitMapWithHue(bitMap, hue);

		byte[] byteArray = bitmapToBytes(bitMap);
		Pixmap pixMap = new Pixmap(byteArray, 0, byteArray.length);
		result = new Texture(pixMap);
		//bitMap用完后recycle掉
		bitMap.recycle();

		return result;
	}

	public static byte[] bitmapToBytes(Bitmap bitmap) {

		if (bitmap == null)
			return null;
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
		return os.toByteArray();
	}

	/**
	 * RGB to HSB <br>
	 * http://blog.csdn.net/xhhjin/article/details/7020449
	 * http://www.cnblogs.com/latifrons/archive/2012/10/01/2709894.html
	 * 
	 * @param color
	 * @return
	 */
	public static Hsb rgb2hsb(int color) {
		int rgbR, rgbG, rgbB;
		rgbR = Color.red(color);
		rgbG = Color.green(color);
		rgbB = Color.blue(color);
		assert 0 <= rgbR && rgbR <= 255;
		assert 0 <= rgbG && rgbG <= 255;
		assert 0 <= rgbB && rgbB <= 255;
		int[] rgb = new int[] { rgbR, rgbG, rgbB };
		Arrays.sort(rgb);
		int max = rgb[2];
		int min = rgb[0];

		float hsbB = max / 255.0f;
		float hsbS = max == 0 ? 0 : (max - min) / (float) max;

		float hsbH = 0;
		if (max == rgbR && rgbG >= rgbB) {
			hsbH = (rgbG - rgbB) * 60f / (max - min) + 0;
		} else if (max == rgbR && rgbG < rgbB) {
			hsbH = (rgbG - rgbB) * 60f / (max - min) + 360;
		} else if (max == rgbG) {
			hsbH = (rgbB - rgbR) * 60f / (max - min) + 120;
		} else if (max == rgbB) {
			hsbH = (rgbR - rgbG) * 60f / (max - min) + 240;
		}
		Hsb hsb = new Hsb();
		hsb.mB = hsbB;
		hsb.mH = hsbH;
		hsb.mS = hsbS;
		return hsb;
	}

	/**
	 * 转化成一张bitmap
	 * 
	 * @param fileHandler　 
	 * @return {@link Bitmap}
	 */
	public static Bitmap convertFile(FileHandle fileHandler) {
		Bitmap originalBitMap;
		byte[] originalData = fileHandler.readBytes();
		originalBitMap = BitmapFactory.decodeByteArray(originalData, 0, originalData.length);
		Bitmap newBitMap = Bitmap.createBitmap(originalBitMap);
		
		if(!originalBitMap.isRecycled() && originalBitMap != newBitMap){
			originalBitMap.recycle();
		}
		return newBitMap;
	}

}
