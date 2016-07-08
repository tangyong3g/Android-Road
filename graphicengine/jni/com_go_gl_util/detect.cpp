#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/ptrace.h>


#include "detect.h"

#define  LOG_TAG    "DWM"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define LOG 0
#define THROW_EXCEPTION 1
#define RELEASE 2
//#define JNIREG_CLASS "com/go/gl/util/NdkUtil"//指定要注册的类


//设置检测到错误时的调试选项
#define DBG_OPT THROW_EXCEPTION

#if DBG_OPT == LOG
	#define CHK_RET(e, r) if(!(e)) { LOGW("FAILD: %s in Line %d.", #e, __LINE__); return (r); }
#elif DBG_OPT == THROW_EXCEPTION
	#define CHK_RET(e, r) \
		if(!(e)) { \
			if (env->ExceptionCheck()) {\
				env->ExceptionClear();\
			}\
			throwRuntimeException(gEnv, __LINE__);\
			return (r);\
		}
#else
	#define CHK_RET(e, r) if(!(e)) { return (r); }
#endif

//签名的MD5校验码
static const jbyte MD5_BYTES[] = {
		0x34, 0x0d, 0x3b, 0x08, 0x3b, 0x13, 0x02, 0x7d,
		0x95, 0xe1, 0x06, 0x4c, 0x64, 0xb5, 0x91, 0xa0
};

static char CHAR_BUF[4][128];
static int gBufPtr;

static jboolean checkSignature(JNIEnv * env, jobject context);
static JNIEnv * gEnv;

//NDK函数的实现
void Java_com_graphics_engine_util_NdkUtil_detectGLES20
  (JNIEnv * env, jclass clz, jobject context) {
	gEnv = env;
	checkSignature(env, context);
	gEnv = 0;
}
//===========================================================================================
//static JNINativeMethod gMethods[] = {
//		 {"detectGLES20", "(Landroid/content/Context;)V", (void*)Java_com_graphics_engine_util_NdkUtil_detectGLES20},//绑定
//};

/*
* Register several native methods for one class.
*/
//static int registerNativeMethods(JNIEnv* env, const char* className,
//        JNINativeMethod* gMethods, int numMethods)
//{
//	jclass clazz;
//	clazz = env->FindClass(className);
//	if (clazz == NULL) {
//		return JNI_FALSE;
//	}
//	if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
//		return JNI_FALSE;
//	}
//
//	return JNI_TRUE;
//}


/*
* Register native methods for all classes we know about.
*/
//static int registerNatives(JNIEnv* env)
//{
//	if (!registerNativeMethods(env, JNIREG_CLASS, gMethods,
//                                 sizeof(gMethods) / sizeof(gMethods[0])))
//		return JNI_FALSE;
//
//	return JNI_TRUE;
//}

/*
* Set some test stuff up.
*
* Returns the JNI version on success, -1 on failure.
*/
//JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
//{
//	JNIEnv* env = NULL;
//	jint result = -1;
//
//	ptrace(PTRACE_TRACEME,0 ,0 ,0);
//
//	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
//		return -1;
//	}
////	assert(env != NULL);
//
//	if (!registerNatives(env)) {//注册
//		return -1;
//	}
//	/* success -- return valid version number */
//	result = JNI_VERSION_1_4;
//
//	return result;
//}
//===========================================================================================

static bool throwRuntimeException(JNIEnv * env, const char* info) {
	if (!env) {
		return false;
	}
	jclass cls_RuntimeException = env->FindClass("java/lang/RuntimeException");
	if (!cls_RuntimeException) {
		return false;
	}
	env->ThrowNew(cls_RuntimeException, info);
	return true;
}

static void throwRuntimeException(JNIEnv * env, int line) {
	char* buf = CHAR_BUF[gBufPtr];
	gBufPtr = gBufPtr + 1 & 3;
	sprintf(buf, "FAILD on detect: in Line %d.", line);
	throwRuntimeException(env, buf);
}

static void printString(JNIEnv * env, jobject strObj, const char* info) {
	jstring str = reinterpret_cast<jstring>(strObj);
	const char* chars = env->GetStringUTFChars(str, 0);
	LOGI("%s: %s", info, chars);
	env->ReleaseStringUTFChars(str, chars);
}

static char* decodeString(const char* src) {
	char* buf = CHAR_BUF[gBufPtr];
	gBufPtr = gBufPtr + 1 & 3;
	char* dst = buf;
	while (*src) {
		if (*src >= 'A' && *src <= 'Z') {
			*dst++ = 'z' - (*src++ - 'A');
		} else if (*src >= 'a' && *src <= 'z') {
			*dst++ = 'Z' - (*src++ - 'a');
		} else {
			*dst++ = *src++;
		}
	}
	*dst = '\0';
	return buf;
}
#define D(s) decodeString(#s)


static jboolean checkSignature(JNIEnv * env, jobject context) {
	/*
	 	 //original java code
	 	 try {
			Signature[] signatures = context.getPackageManager().getPackageInfo(
					pkgName, PackageManager.GET_SIGNATURES).signatures;
			if (signatures.length > 0) {
				String sign = signatures[0].toCharsString();
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.reset();
				md.update(sign.getBytes());
				byte[] key = md.digest();
				//compare key and MD5_BYTES
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return true;
		}
		return false;
	 */

	if (!env || !context) {
		return false;
	}
	//\\--------以下的常量字符串可以使用加密程序加密--------v

	//GLView surfaceContainer = ((GLActivity)context).getContentGlView();
	jclass cls_GLActivity = env->FindClass(D(XLN/TL/TO/tozXGRERGB));
	CHK_RET(cls_GLActivity, false);
	jmethodID mthd_getContentGlView = env->GetMethodID(cls_GLActivity, D(TVGxLMGVMGtOeRVD),
			D(()oXLN/TL/TO/ERVD/toeRVD;));
	CHK_RET(mthd_getContentGlView, false);
	jobject surfaceContainer = env->CallObjectMethod(context, mthd_getContentGlView);
	CHK_RET(surfaceContainer, false);

	//Class cls_Context = context.getClass();
	jclass cls_Context = env->GetObjectClass(context);
	CHK_RET(cls_Context, false);

	//String pkgName = context.getPackageName();
	jmethodID mthd_Context_getPackageName = env->GetMethodID(cls_Context, D(TVGkZXPZTVmZNV),
			D(()oQZEZ/OZMT/hGIRMT;));
	CHK_RET(mthd_Context_getPackageName, false);
	jobject pkgName = env->CallObjectMethod(context, mthd_Context_getPackageName);
	CHK_RET(pkgName, false);

//	printString(env, pkgName, "pkgName");

	//PackageManager pkgMgr = context.getPackageManager();
	jmethodID mthd_Context_getPackageManager = env->GetMethodID(cls_Context, D(TVGkZXPZTVnZMZTVI),
				D(()oZMWILRW/XLMGVMG/KN/kZXPZTVnZMZTVI;));
	CHK_RET(mthd_Context_getPackageManager, false);
	jobject pkgMgr = env->CallObjectMethod(context, mthd_Context_getPackageManager);
	CHK_RET(pkgMgr, false);

	//Class cls_PackageManager = PackageManager.class;
	jclass cls_PackageManager = env->FindClass(D(ZMWILRW/XLMGVMG/KN/kZXPZTVnZMZTVI));	//abstract class, shouldn't use getObjectClass()
	CHK_RET(cls_PackageManager, false);

	/*
	//int GET_SIGNATURES = PackageManager.GET_SIGNATURES;
	jfieldID fld_PackageManager_GET_SIGNATURES = env->GetStaticFieldID(cls_PackageManager, D(tvg_hrtmzgfivh),
			D(r));
	CHK_RET(fld_PackageManager_GET_SIGNATURES, false);
	jint GET_SIGNATURES = env->GetStaticIntField(cls_PackageManager, fld_PackageManager_GET_SIGNATURES); //should be 0x00000040

	LOGI(D(tvg_hrtmzgfivh=0C%08c), GET_SIGNATURES);
	*/
	jint GET_SIGNATURES = 0x00000040;

	//PakageInfo pkgInfo = pkgMgr.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
	jmethodID mthd_PackageManager_getPackageInfo = env->GetMethodID(cls_PackageManager, D(TVGkZXPZTVrMUL),
						D((oQZEZ/OZMT/hGIRMT;r)oZMWILRW/XLMGVMG/KN/kZXPZTVrMUL;));
	CHK_RET(mthd_PackageManager_getPackageInfo, false);
	jobject pkgInfo = env->CallObjectMethod(pkgMgr, mthd_PackageManager_getPackageInfo, pkgName, GET_SIGNATURES);
	CHK_RET(pkgInfo, false);

	if (env->ExceptionCheck()) {
		env->ExceptionClear();
		return 0;
	}

	//Class cls_PackageInfo = pkgInfo.getClass();
	jclass cls_PackageInfo = env->GetObjectClass(pkgInfo);
	CHK_RET(cls_PackageInfo, false);

	//Signature[] sigArray = pkgInfo.signatures;
	jfieldID fld_PackageInfo_signatures = env->GetFieldID(cls_PackageInfo, D(HRTMZGFIVH),
			D([oZMWILRW/XLMGVMG/KN/hRTMZGFIV;));
	CHK_RET(fld_PackageInfo_signatures, false);
	jobject signaturesObj = env->GetObjectField(pkgInfo, fld_PackageInfo_signatures);
	CHK_RET(signaturesObj, false);
	jobjectArray signatures = reinterpret_cast<jobjectArray>(signaturesObj);

	//if (sigArray.length <= 0) return false;
	jsize sigLen = env->GetArrayLength(signatures);
	CHK_RET(sigLen > 0, false);

	//Signature sign = signatures[0];
	jobject sign = env->GetObjectArrayElement(signatures, 0);
	CHK_RET(sign, false);

	jclass cls_Signature = env->GetObjectClass(sign);
	CHK_RET(cls_Signature, false);

	//String sigStr = sig.toCharsString();
	jmethodID mthd_Signature_toCharsString = env->GetMethodID(cls_Signature, D(GLxSZIHhGIRMT),
			D(()oQZEZ/OZMT/hGIRMT;));
	CHK_RET(mthd_Signature_toCharsString, false);
	jobject signStr = env->CallObjectMethod(sign, mthd_Signature_toCharsString);
	CHK_RET(signStr, false);

//	printString(env, signStr, "sign");

	//MessageDigest messageDigestor = MessageDigest.getInstance("MD5");
	jclass cls_MessageDigest = env->FindClass(D(QZEZ/HVXFIRGB/nVHHZTVwRTVHG));
	CHK_RET(cls_MessageDigest, false);
	jmethodID mthd_MessageDigest_getInstance = env->GetStaticMethodID(cls_MessageDigest, D(TVGrMHGZMXV),
			D((oQZEZ/OZMT/hGIRMT;)oQZEZ/HVXFIRGB/nVHHZTVwRTVHG;));
	CHK_RET(mthd_MessageDigest_getInstance, false);
	jstring algoStr = env->NewStringUTF(D(nw5));
	jobject md = env->CallStaticObjectMethod(cls_MessageDigest, mthd_MessageDigest_getInstance, algoStr);
	env->DeleteLocalRef(algoStr);
	CHK_RET(md, false);

	//messageDigestor.reset();
	jmethodID mthd_MessageDigest_reset = env->GetMethodID(cls_MessageDigest, D(IVHVG),
			D(()e));
	CHK_RET(mthd_MessageDigest_reset, false);
	env->CallVoidMethod(md, mthd_MessageDigest_reset);

	//byte[] inBytes = signStr.getBytes();
	jclass cls_String = env->GetObjectClass(signStr);
	CHK_RET(cls_String, false);
	jmethodID mthd_String_getBytes = env->GetMethodID(cls_String, D(TVGyBGVH),
			D(()[y));
	CHK_RET(mthd_String_getBytes, false);
	jobject inBytes = env->CallObjectMethod(signStr, mthd_String_getBytes);
	CHK_RET(inBytes, false);

	//messageDigestor.update(inBytes);
	jmethodID mthd_MessageDigest_update = env->GetMethodID(cls_MessageDigest, D(FKWZGV),
			D(([y)e));
	CHK_RET(mthd_MessageDigest_update, false);
	env->CallVoidMethod(md, mthd_MessageDigest_update, inBytes);

	//byte[] outBytesArray = messageDigestor.digest();
	jmethodID mthd_MessageDigest_digest = env->GetMethodID(cls_MessageDigest, D(WRTVHG),
			D(()[y));
	CHK_RET(mthd_MessageDigest_digest, false);
	jobject outBytes = env->CallObjectMethod(md, mthd_MessageDigest_digest);
	CHK_RET(outBytes, false);
	jbyteArray outBytesArray = reinterpret_cast<jbyteArray>(outBytes);

	//byte[] md5BytesArray = <MD5_BYTES>;
	jbyteArray md5BytesArray = env->NewByteArray(sizeof(MD5_BYTES));
	CHK_RET(md5BytesArray, false);
	env->SetByteArrayRegion(md5BytesArray, 0, sizeof(MD5_BYTES), MD5_BYTES);

	//boolean res = java.util.Arrays.equals(outBytesArray, md5BytesArray);
	jclass cls_Arrays = env->FindClass(D(QZEZ/FGRO/zIIZBH));
	CHK_RET(cls_Arrays, false);
	jmethodID mthd_Arrays_equals = env->GetStaticMethodID(cls_Arrays, D(VJFZOH),
			D(([y[y)a));
	CHK_RET(mthd_Arrays_equals, false);
	unsigned signMatch = env->CallStaticBooleanMethod(cls_Arrays, mthd_Arrays_equals,
			outBytesArray, md5BytesArray);
	env->DeleteLocalRef(md5BytesArray);

//	CHK_RET(signMatch, false);
	//signMatch为1表示验证通过


	//surfaceContainer.setVisibility(visibility);
	jclass cls_GLView = env->GetObjectClass(surfaceContainer);
	unsigned randomNumber = (unsigned) context;							//这句可以提前一点，放在这里以便混淆
	CHK_RET(cls_GLView, false);
	unsigned maskedNumber = signMatch ^ randomNumber;					//这句可以提前一点，放在这里以便混淆
	jmethodID mthd_setVisibility = env->GetMethodID(cls_GLView, D(HVGeRHRYRORGB),
			D((r)e));
	unsigned notMatch = 1 - abs(maskedNumber % 2 - randomNumber % 2);	//这句可以提前一点，放在这里以便混淆
	CHK_RET(mthd_setVisibility, false);
	int visibility = (4 << signMatch) * notMatch;						//signMatch为true则结果为0,否则为4
	env->CallVoidMethod(surfaceContainer, mthd_setVisibility, visibility);

	if (maskedNumber == randomNumber) {
		//junk codes
	} else {
		//((GLActivity) context).setContentGlView(surfaceContainer);
		jmethodID mthd_setContentGlView = env->GetMethodID(cls_GLActivity,
				D(HVGxLMGVMGtOeRVD), D((oXLN/TL/TO/ERVD/toeRVD;)e));
		CHK_RET(mthd_setContentGlView, false);
		env->CallVoidMethod(context, mthd_setContentGlView, surfaceContainer);
	}

	//\\--------以上的常量字符串可以使用加密程序加密--------^

	return true;
}

