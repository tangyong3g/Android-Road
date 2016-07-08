
#ifndef UTIL_H
#define UTIL_H

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>
#include <string.h>		// for memset, memcpy, etc
#include <stdio.h>

#ifndef NULL
#define NULL 0
#endif

#define FREE(p) { delete p; p = NULL; }
#define FREEARR(p) { delete [] p; p = NULL; }

//#include "glu.h"

/**
 * 每次从java到ndk的调用都带有JNIEnv* env参数（一般和上一次的值不同），需要更新
 */
void updateEnv(JNIEnv* env);

void updateAssetManager();

FILE* openAssetFile(const char* fileName, int* pLen, const char* mode = "rb");

void updateApkPath(const char *apkPath);

const char *getApkPath();

/**
 * 解锁
 */
void unlockScreen();

/**
 * 使手机震动
 */
void vibrate();

#ifdef __cplusplus
}
#endif
#endif
