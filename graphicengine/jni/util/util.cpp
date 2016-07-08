/*
 * util.cpp
 *
 *  Created on: 2011-1-4
 *      Author: dengweiming
 */

#include <unistd.h>
#include <stdlib.h>

#define LOG_LEVEL_E
#include "util/log.h"
#include "util.h"

#define AUTO_APPEND_DOT_MP3 0

static JNIEnv* gEnv = NULL;
static jobject gAssetManager = NULL;
static char g_apkPath[256];

void updateEnv(JNIEnv* env)
{
	gEnv = env;
}

void updateApkPath(const char *apkPath){
	strcpy(g_apkPath, apkPath);
}

const char *getApkPath(){
	return g_apkPath;
}

void updateAssetManager()
{
	JNIEnv* env = gEnv;
	if(env){
		// gAssetManager = RGE.mManager;
		jclass cls_RGE = env->FindClass("com/jiubang/rge/RGE");
		LOGASSERT(cls_RGE);
		jfieldID fID_mManager = env->GetStaticFieldID(cls_RGE, "mManager",
				"Landroid/content/res/AssetManager;");
		assert(fID_mManager);
		jobject manager = env->GetStaticObjectField(cls_RGE, fID_mManager);
		assert(manager);
		gAssetManager = manager;
	}
	else{
		gAssetManager = NULL;
	}
}

FILE* openAssetFile(const char* fileName, int* pLen, const char* mode)
{
	JNIEnv* env = gEnv;
	jobject assetManager = gAssetManager;
	LOGASSERT(env);
	assert(assetManager);
	assert(fileName);
	assert(pLen);

#if AUTO_APPEND_DOT_MP3
	char fileName2[256];
	assert(strlen(fileName) <= 250);
	strcpy(fileName2, fileName);
	strcat(fileName2, ".mp3");
	fileName = fileName2;
#endif

	// AssetFileDescriptor afd = assetManager.openFd(fileName);
    jclass cls_AssetManager = env->GetObjectClass(assetManager);
    assert(cls_AssetManager);
    jmethodID mID_openFd = env->GetMethodID(cls_AssetManager, "openFd",
    		"(Ljava/lang/String;)Landroid/content/res/AssetFileDescriptor;");
    assert(mID_openFd);
    jstring jStr_fileName = env->NewStringUTF(fileName);
    jobject afd = env->CallObjectMethod(assetManager, mID_openFd, jStr_fileName);
    if(!afd){
    	LOGE("Cannot open file %s in assetManager!", fileName);
    	return NULL;
    }

    // FileDescriptor fd = afd.getFileDescriptor();
    jclass cls_AssetFileDescriptor = env->GetObjectClass(afd);
    assert(cls_AssetFileDescriptor);
    jmethodID mID_getFileDescriptor = env->GetMethodID(cls_AssetFileDescriptor,
    		"getFileDescriptor", "()Ljava/io/FileDescriptor;");
    assert(mID_getFileDescriptor);
    jobject fd = env->CallObjectMethod(afd, mID_getFileDescriptor);
    assert(fd);

    // long offset = afd.getStartOffset();
    jmethodID mID_getStartOffset = env->GetMethodID(cls_AssetFileDescriptor,
    		"getStartOffset", "()J");
    assert(mID_getStartOffset);
    jlong offset = env->CallLongMethod(afd, mID_getStartOffset);

    // long len = afd.getLength();
    jmethodID mID_getLength = env->GetMethodID(cls_AssetFileDescriptor,
    		"getLength", "()J");
    assert(mID_getLength);
    *pLen = env->CallLongMethod(afd, mID_getLength);

    // int descriptor = fd.descriptor;
    jclass cls_FileDescriptor = env->GetObjectClass(fd);
    assert(cls_FileDescriptor);
    jfieldID fid_descriptor = env->GetFieldID(cls_FileDescriptor, "descriptor", "I");
    assert(fid_descriptor);
    jint descriptor = env->GetIntField(fd, fid_descriptor);
    descriptor = dup(descriptor);

    FILE* file = fdopen(descriptor, mode);
    if(!file){
    	LOGE("Cannot open file %s !", fileName);
    	return NULL;
    }
    fseek(file, offset, SEEK_SET);
    return file;
}

void unlockScreen()
{
	JNIEnv* env = gEnv;
	if(!env) return;

	jclass cls_RGE = env->FindClass("com/jiubang/rge/RGE");
	assert(cls_RGE);
	jmethodID mID_unlock = env->GetStaticMethodID(cls_RGE, "unlock", "()V");
	assert(mID_unlock);
	env->CallStaticVoidMethod(cls_RGE, mID_unlock);
}

void vibrate()
{
	JNIEnv* env = gEnv;
	if(!env) return;

	jclass order_class = env->FindClass("com/jiubang/rge/JavaService");
	jmethodID mid;

	/* call the vibrate */
	mid = env->GetStaticMethodID(order_class, "Vibrate", "()V" );
	if(mid != 0){
		env->CallStaticVoidMethod(order_class, mid);
	}
}
