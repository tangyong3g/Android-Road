package com.sny.tangyong.basic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.IntAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class MomentTest extends AndroidApplication {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initialize(new MomentApplis(getApplicationContext()), true);

	}

	class MomentApplis implements ApplicationListener {

		Camera mCamera;
		ModelInstance mBgOne;
		ModelInstance mBgTwo;
		ModelBatch mBatch;
		ModelBatch mMask;
		
		Context mContext;
		
		
		Texture mTx;
		Texture mMaskTx;
		
		public MomentApplis(Context context){
			mContext = context;
		}

		@Override
		public void create() {
			initModel();
			initModelTwo();
			initMask();
			initBatch();
		}

		private void initMask() {
			
			mMaskTx = new Texture(Gdx.files.internal("data/mask.png"));
			
			int width = Gdx.graphics.getWidth();
			int height = Gdx.graphics.getHeight();

			Vector3 position_one = new Vector3(-width / 2, height / 2, 0);
			Vector3 position_two = new Vector3(width / 2, height / 2, 0);
			Vector3 position_three = new Vector3(width / 2, -height / 2, 0);
			Vector3 position_four = new Vector3(-width / 2, -height / 2, 0);


			Material material = new Material();
			BlendingAttribute blendingAttributeCube = new BlendingAttribute(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);
			TextureAttribute txAttr = TextureAttribute.createDiffuse(mMaskTx);
//			material.set(mMaskTx);
			material.set(blendingAttributeCube);

			IntAttribute intAttr = new IntAttribute(IntAttribute.CullFace, 0);
			material.set(intAttr);

			/**/
			ModelBuilder builder = new ModelBuilder();
			Model model = builder.createRect(position_one.x, position_one.y, position_one.z,
					position_two.x, position_two.y, position_two.z, position_three.x,
					position_three.y, position_three.z, position_four.x, position_four.y,
					position_four.z, 0, 0, 1, material, Usage.Position | Usage.Normal
							| Usage.TextureCoordinates);

			mBgOne = new ModelInstance(model);
			
		}

		private void initModelTwo() {
			
			
			int width = Gdx.graphics.getWidth();
			int height = Gdx.graphics.getHeight();

			Vector3 position_one = new Vector3(-width / 2, height / 2, 0);
			Vector3 position_two = new Vector3(width / 2, height / 2, 0);
			Vector3 position_three = new Vector3(width / 2, -height / 2, 0);
			Vector3 position_four = new Vector3(-width / 2, -height / 2, 0);

			
			java.io.InputStream is;
			is = mContext.getResources().openRawResource(R.drawable.bg_one);

			//BitmapFactory.Options 类,  允许我们定义图片以何种方式如何读到内存，
			BitmapFactory.Options opts = new BitmapFactory.Options();
			Bitmap bm;

			//这样设置后.就不会产生真实的bitMap回来.而只有高度和宽度.为了解决大图的问题 opts.outHeight 
//			opts.inJustDecodeBounds = true;

			bm = BitmapFactory.decodeStream(is, null, opts);

			
			bm = BoxBlurFilter(bm);
			
			Material material = new Material();
			BlendingAttribute blendingAttributeCube = new BlendingAttribute(GL10.GL_SRC_ALPHA,
					GL10.GL_ONE_MINUS_SRC_ALPHA);
			
			
			mTx = BitMapUtil.convertBitmapToTexture(bm, 100);
			
			TextureAttribute txAttr = TextureAttribute.createDiffuse(mTx);
			material.set(txAttr);
			material.set(blendingAttributeCube);

			IntAttribute intAttr = new IntAttribute(IntAttribute.CullFace, 0);
			material.set(intAttr);

			/**/
			ModelBuilder builder = new ModelBuilder();
			Model model = builder.createRect(position_one.x, position_one.y, position_one.z,
					position_two.x, position_two.y, position_two.z, position_three.x,
					position_three.y, position_three.z, position_four.x, position_four.y,
					position_four.z, 0, 0, 1, material, Usage.Position | Usage.Normal
							| Usage.TextureCoordinates);

			mBgTwo = new ModelInstance(model);
			
			
		}

		private void initModel() {

			int width = Gdx.graphics.getWidth();
			int height = Gdx.graphics.getHeight();

			Vector3 position_one = new Vector3(-width / 2, height / 2, 0);
			Vector3 position_two = new Vector3(width / 2, height / 2, 0);
			Vector3 position_three = new Vector3(width / 2, -height / 2, 0);
			Vector3 position_four = new Vector3(-width / 2, -height / 2, 0);

			Texture tx = new Texture(Gdx.files.internal("data/bg_one.jpg"));

			Material material = new Material();
//			BlendingAttribute blendingAttributeCube = new BlendingAttribute(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);
			TextureAttribute txAttr = TextureAttribute.createDiffuse(tx);
			material.set(txAttr);
//			material.set(blendingAttributeCube);

			IntAttribute intAttr = new IntAttribute(IntAttribute.CullFace, 0);
			material.set(intAttr);

			/**/
			ModelBuilder builder = new ModelBuilder();
			Model model = builder.createRect(position_one.x, position_one.y, position_one.z,
					position_two.x, position_two.y, position_two.z, position_three.x,
					position_three.y, position_three.z, position_four.x, position_four.y,
					position_four.z, 0, 0, 1, material, Usage.Position | Usage.Normal
							| Usage.TextureCoordinates);

			mBgOne = new ModelInstance(model);

		}

		private void initCamera() {

			mCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			mCamera.position.set(0, 0, 100);
			mCamera.lookAt(0, 0, 0);

		}

		private void initBatch() {
			DefaultShaderProvider defaultProvider = new DefaultShaderProvider();
			mBatch = new ModelBatch(defaultProvider);
		}

		@Override
		public void dispose() {

		}

		@Override
		public void pause() {

		}

		@Override
		public void render() {

			Gdx.gl.glClear(Gdx.gl20.GL_COLOR_BUFFER_BIT | Gdx.gl20.GL_DEPTH_BUFFER_BIT);

			mBatch.begin(mCamera);
//			mBatch.render(mBgOne);
			
			mBatch.render(mBgTwo);
			mBatch.end();

		}

		@Override
		public void resize(int width, int height) {

			initCamera();

		}

		@Override
		public void resume() {

		}

	}

	/** 水平方向模糊度 */
	private static float hRadius = 5;
	/** 竖直方向模糊度 */
	private static float vRadius = 5;
	/** 模糊迭代度 */
	private static int iterations = 7;

	public static Bitmap BoxBlurFilter(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
		for (int i = 0; i < iterations; i++) {
			blur(inPixels, outPixels, width, height, hRadius);
			blur(outPixels, inPixels, height, width, vRadius);
		}
		blurFractional(inPixels, outPixels, width, height, hRadius);
		blurFractional(outPixels, inPixels, height, width, vRadius);
		bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
		//		Drawable drawable = new BitmapDrawable(bitmap);
		//		return drawable;
		return bitmap;
	}

	public static void blur(int[] in, int[] out, int width, int height, float radius) {
		int widthMinus1 = width - 1;
		int r = (int) radius;
		int tableSize = 2 * r + 1;
		int divide[] = new int[256 * tableSize];

		for (int i = 0; i < 256 * tableSize; i++)
			divide[i] = i / tableSize;

		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;
			int ta = 0, tr = 0, tg = 0, tb = 0;

			for (int i = -r; i <= r; i++) {
				int rgb = in[inIndex + clamp(i, 0, width - 1)];
				ta += (rgb >> 24) & 0xff;
				tr += (rgb >> 16) & 0xff;
				tg += (rgb >> 8) & 0xff;
				tb += rgb & 0xff;
			}

			for (int x = 0; x < width; x++) {
				out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8)
						| divide[tb];

				int i1 = x + r + 1;
				if (i1 > widthMinus1)
					i1 = widthMinus1;
				int i2 = x - r;
				if (i2 < 0)
					i2 = 0;
				int rgb1 = in[inIndex + i1];
				int rgb2 = in[inIndex + i2];

				ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
				tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
				tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
				tb += (rgb1 & 0xff) - (rgb2 & 0xff);
				outIndex += height;
			}
			inIndex += width;
		}
	}

	public static void blurFractional(int[] in, int[] out, int width, int height, float radius) {
		radius -= (int) radius;
		float f = 1.0f / (1 + 2 * radius);
		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;

			out[outIndex] = in[0];
			outIndex += height;
			for (int x = 1; x < width - 1; x++) {
				int i = inIndex + x;
				int rgb1 = in[i - 1];
				int rgb2 = in[i];
				int rgb3 = in[i + 1];

				int a1 = (rgb1 >> 24) & 0xff;
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;
				int a2 = (rgb2 >> 24) & 0xff;
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = rgb2 & 0xff;
				int a3 = (rgb3 >> 24) & 0xff;
				int r3 = (rgb3 >> 16) & 0xff;
				int g3 = (rgb3 >> 8) & 0xff;
				int b3 = rgb3 & 0xff;
				a1 = a2 + (int) ((a1 + a3) * radius);
				r1 = r2 + (int) ((r1 + r3) * radius);
				g1 = g2 + (int) ((g1 + g3) * radius);
				b1 = b2 + (int) ((b1 + b3) * radius);
				a1 *= f;
				r1 *= f;
				g1 *= f;
				b1 *= f;
				out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
				outIndex += height;
			}
			out[outIndex] = in[width - 1];
			inIndex += width;
		}
	}

	public static int clamp(int x, int a, int b) {
		return (x < a) ? a : (x > b) ? b : x;
	}

}
