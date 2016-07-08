/*
 * android.c: Android front end for my puzzle collection.
 */

#include <jni.h>

#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <pthread.h>
#include <android/log.h>

#include "util.h"

#define  LOG_TAG    "NdkUtil"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

pthread_key_t envKey;
static jclass crashHandlerClass;
static jmethodID onNativeCrashed;

static struct sigaction old_sa[NSIG];

void android_sigaction(int signal, siginfo_t *info, void *reserved)
{
	LOGE("android_sigaction: signal=%d, info=%08X", signal, info);
	JNIEnv *env = (JNIEnv*)pthread_getspecific(envKey);
//	jclass crashHandlerClass = env->FindClass("com/go/gl/util/NdkUtil");
//	jmethodID onNativeCrashed = env->GetStaticMethodID(crashHandlerClass, "onNativeCrashed", "()V");
	env->CallStaticVoidMethod(crashHandlerClass, onNativeCrashed, signal);
	if (signal >= 0 && signal < NSIG) {
		old_sa[signal].sa_handler(signal);
	}
}

void Java_com_graphics_engine_util_NdkUtil_init
	(JNIEnv * env, jclass cls) {
	int res = pthread_key_create(&envKey, NULL);
	pthread_setspecific(envKey, env);

	crashHandlerClass = cls;
	onNativeCrashed = env->GetStaticMethodID(cls, "onNativeCrashed", "(I)V");

	// Try to catch crashes...
	struct sigaction handler;
	memset(&handler, 0, sizeof(handler));
	handler.sa_sigaction = android_sigaction;
	handler.sa_flags = SA_RESETHAND;
#define CATCHSIG(X) sigaction(X, &handler, &old_sa[X])
	CATCHSIG(SIGILL);
	CATCHSIG(SIGABRT);
	CATCHSIG(SIGBUS);
	CATCHSIG(SIGFPE);
	CATCHSIG(SIGSEGV);
//mips平台架构下没有tkflt这个信号量，如果需要编译mips平台的库，将其屏蔽注释掉
	CATCHSIG(SIGSTKFLT);
	CATCHSIG(SIGPIPE);
}
