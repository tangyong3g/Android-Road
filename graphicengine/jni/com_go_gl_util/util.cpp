#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/bitmap.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <android/log.h>

#include "util.h"
//#include "log.h"

#define  LOG_TAG    "NdkUtil"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define MAX(a, b) ((a) > (b) ? (a) : (b))
#define MIN(a, b) ((a) < (b) ? (a) : (b))

//so库的版本号
#define LIB_VERSION 1

const char TGA_UHEADER[] = { 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

#include <pthread.h>
extern pthread_key_t envKey;

void Java_com_go_gl_util_NdkUtil_glVertexAttribPointer(JNIEnv * env, jclass cls,
		jint indx, jint size, jint type, jboolean normalized, jint stride,
		jint offset) {
	if (envKey && pthread_getspecific(envKey) != env) {
		pthread_setspecific(envKey, env);
	}

	glVertexAttribPointer(indx, size, type, normalized, stride,
			(const void*) (offset));
}

void Java_com_graphics_engine_util_NdkUtil_glDrawElements(JNIEnv * env, jclass cls,
		jint mode, jint count, jint type, jint offset) {
	if (envKey && pthread_getspecific(envKey) != env) {
		pthread_setspecific(envKey, env);
	}

	glDrawElements(mode, count, type, (const void*) (offset));
}

void Java_com_graphics_engine_util_NdkUtil_saveScreenshotTGA(JNIEnv * env, jclass cls, jint x,
		jint y, jint w, jint h, jstring fileName) {
	if (envKey && pthread_getspecific(envKey) != env) {
		pthread_setspecific(envKey, env);
	}


	const char *nativeFileName = env->GetStringUTFChars(fileName, 0);

	unsigned short width = w, height = h;
	unsigned char bpp = 32;
	unsigned char c = 0;
	unsigned imageSize = w * h * (bpp / 8);
	unsigned char * data = new unsigned char[imageSize];
	if (!data) {
		LOGE("Cannot allocate memory.");
		return;
	}

	FILE* f = fopen(nativeFileName, "wb");
	if (!f) {
		LOGE("Cannot open file \"%s\"", nativeFileName);
		return;
	}

	glReadPixels(x, y, w, h, GL_RGBA, GL_UNSIGNED_BYTE, data);
	//Swap red and blue bytes
	for (unsigned int i = 0, Bpp = (bpp / 8); i < imageSize; i += Bpp) {
		unsigned char tmp = data[i];
		data[i] = data[i + 2];
		data[i + 2] = tmp;
	}
	fwrite(TGA_UHEADER, 1, sizeof(TGA_UHEADER), f);
	fwrite(&width, 1, 2, f);
	fwrite(&height, 1, 2, f);
	fwrite(&bpp, 1, 1, f);
	fwrite(&c, 1, 1, f);
	fwrite(data, 1, imageSize, f);

	fclose(f);
	delete[] data;

	env->ReleaseStringUTFChars(fileName, nativeFileName);
}

static void bitmapFlipVerticalAndSwapRedBlue(int w, int h, unsigned* pixels) {
	unsigned char bpp = 32;
	int lineWidth = w;
	unsigned int* src = pixels;
	unsigned int* dst = src + lineWidth * (h - 1);
	for (int i = 0; i <= h - 1 - i; ++i) {
		for (int j = 0; j < w; ++j, ++src, ++dst) {
			register unsigned pixel1 = *src;
			register unsigned pixel2 = *dst;
			*dst = ((pixel1 & 0xFF) << 16) | ((pixel1 >> 16) & 0xFF) | (pixel1 & 0xFF00FF00);
			*src = ((pixel2 & 0xFF) << 16) | ((pixel2 >> 16) & 0xFF) | (pixel2 & 0xFF00FF00);
		}
		dst -= lineWidth * 2;
	}
}

static void bitmapFlipVertical(int w, int h, unsigned* pixels) {
	unsigned char bpp = 32;
	int lineWidth = w;
	unsigned int* src = pixels;
	unsigned int* dst = src + lineWidth * (h - 1);
	for (int i = 0; i <= h - 1 - i; ++i) {
		for (int j = 0; j < w; ++j, ++src, ++dst) {
			register unsigned pixel1 = *src;
			register unsigned pixel2 = *dst;
			*dst = pixel1;
			*src = pixel2;
		}
		dst -= lineWidth * 2;
	}
}

void Java_com_graphics_engine_util_NdkUtil_saveScreenshot(JNIEnv *env, jclass cls, jint x,
		jint y, jint w, jint h, jintArray buffer) {
	if (envKey && pthread_getspecific(envKey) != env) {
		pthread_setspecific(envKey, env);
	}

	int* nativeBuffer = (int*) env->GetIntArrayElements(buffer, 0);
	glReadPixels(x, y, w, h, GL_RGBA, GL_UNSIGNED_BYTE, nativeBuffer);
	bitmapFlipVerticalAndSwapRedBlue(w, h, (unsigned*) nativeBuffer);
	env->ReleaseIntArrayElements(buffer, nativeBuffer, 0);
}

void Java_com_graphics_engine_util_NdkUtil_saveScreenshotBitmap
  (JNIEnv * env, jclass cls, jint x, jint y, jint w, jint h, jobject bitmap) {
	if (envKey && pthread_getspecific(envKey) != env) {
		pthread_setspecific(envKey, env);
	}

	AndroidBitmapInfo	info;
    unsigned char*		pixels;
    int					ret;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return;
	}

	if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGB_RGBA !");
		return;
	}

	int sizeOfPixels = info.width * info.height * 4;
	if (sizeOfPixels <= 0) {
		LOGE("Bitmap size is not positive !");
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return;
	}

	glReadPixels(x, y, w, h, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
	bitmapFlipVertical(w, h, (unsigned*) pixels);

    AndroidBitmap_unlockPixels(env, bitmap);
}


jboolean Java_com_graphics_engine_util_NdkUtil_convertToHSVInternal
  (JNIEnv *env, jclass cls, jobject bitmap, jboolean optimized)
{
	if (envKey && pthread_getspecific(envKey) != env) {
		pthread_setspecific(envKey, env);
	}

	AndroidBitmapInfo	info;
    unsigned char*		pixels;
    int					ret;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return false;
	}

	if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGB_RGBA !");
		return false;
	}

	int sizeOfPixels = info.width * info.height * 4;
	if (sizeOfPixels <= 0) {
		LOGE("Bitmap size is not positive !");
		return false;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return false;
	}

    unsigned char* endOfPixels = pixels + sizeOfPixels;

	if (!optimized) {
		while (pixels < endOfPixels) {
			unsigned int R = pixels[0];
			unsigned int G = pixels[1];
			unsigned int B = pixels[2];
			unsigned int A = pixels[3];
			int maxRGB = MAX(R, MAX(G, B));
			int minRGB = MIN(R, MIN(G, B));
			float deltaRGB = maxRGB - minRGB;
			float h = 0;
			float s = 0;
			float V = maxRGB; 						// V = v * a
			if (deltaRGB > 0) {
				s = deltaRGB / V;
				if (R == maxRGB) {
					h = (G - B) / deltaRGB;
				} else if (G == maxRGB) {
					h = (B - R) / deltaRGB + 2.0f;
				} else {
					h = (R - G) / deltaRGB + 4.0f;
				}
			}
			pixels[0] = (int) (h * (255 / 6.0f));	// h
			pixels[1] = (int) (s * V); 				// s * v * a
			pixels[2] = (int) ((1 - s) * V); 		// (1 - s) * v * a

			pixels += 4;
		}
	} else {
		while (pixels < endOfPixels) {
			// if optimized, we assume: R >= G = B,
			// it implies, hue == 0, and we only can set hue rather than shift hue.
			// outR will be ignored, outG = s * V = R - G, outB = (1 - s) * V = B
			pixels[1] = pixels[0] - pixels[1];

			pixels += 4;
		}
	}

    AndroidBitmap_unlockPixels(env, bitmap);

    return true;
}

jint Java_com_graphics_engine_util_NdkUtil_getLibVersionInternal
  (JNIEnv *, jclass) {
	return LIB_VERSION;
}

jint Java_com_graphics_engine_util_NdkUtil_saveBitmapInternal
  (JNIEnv * env, jclass clz, jobject bitmap) {
	if (envKey && pthread_getspecific(envKey) != env) {
		pthread_setspecific(envKey, env);
	}

	AndroidBitmapInfo	info;
    unsigned char*		pixels;
    int					ret;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return 0;
	}

	if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGB_RGBA !");
		return 0;
	}

	int sizeOfPixels = info.width * info.height * 4;
	if (sizeOfPixels <= 0) {
		LOGE("Bitmap size is not positive !");
		return 0;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return 0;
	}

	void* newPixels = 0;
	if (pixels) {
		newPixels = malloc(sizeOfPixels);
		if (newPixels) {
			memcpy(newPixels, pixels, sizeOfPixels);
		}
	}

    AndroidBitmap_unlockPixels(env, bitmap);
    return (jint) newPixels;
}

void Java_com_graphics_engine_util_NdkUtil_releasePixelsInternal
  (JNIEnv *env, jclass clz, jint pixels) {
	if (envKey && pthread_getspecific(envKey) != env) {
		pthread_setspecific(envKey, env);
	}

	free((void*) pixels);
}

jint Java_com_graphics_engine_util_NdkUtil_restorePixelsInternal
  (JNIEnv *env, jclass clz, jobject bitmap, jint oldPixels) {
	if (envKey && pthread_getspecific(envKey) != env) {
		pthread_setspecific(envKey, env);
	}

	AndroidBitmapInfo	info;
    unsigned char*		pixels;
    int					ret;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return 0;
	}

	if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGB_RGBA !");
		return 0;
	}

	int sizeOfPixels = info.width * info.height * 4;
	if (sizeOfPixels <= 0) {
		LOGE("Bitmap size is not positive !");
		return 0;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return 0;
	}

	if (pixels && oldPixels) {
		memcpy((void*) pixels, (void*) oldPixels, sizeOfPixels);
	}

    AndroidBitmap_unlockPixels(env, bitmap);
    return 1;
}

void Java_com_graphics_engine_util_NdkUtil_glTexImage2D
  (JNIEnv * env, jclass clz, jint target, jint level, jint internalformat,
		  jint width, jint height, jint border, jint format, jint type, jint pixels) {
	if (envKey && pthread_getspecific(envKey) != env) {
		pthread_setspecific(envKey, env);
	}

	glTexImage2D(target, level, internalformat, width, height, border, format, type, (const void*) (pixels));
}


void Java_com_graphics_engine_util_NdkUtil_glTexSubImage2D
  (JNIEnv * env, jclass clz, jint target, jint level, jint xoffset, jint yoffset,
		  jint width, jint height, jint format, jint type, jint pixels) {
	if (envKey && pthread_getspecific(envKey) != env) {
		pthread_setspecific(envKey, env);
	}

	glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (const void*) (pixels));
}

jint Java_com_graphics_engine_util_NdkUtil_getPixelInternal(
		JNIEnv *env, jclass clz, jint pixelsPtr, jint offset) {
	if (pixelsPtr == 0) {
		return 0;
	}

	int* pixels = (int*)pixelsPtr;
	return pixels[offset];
}
