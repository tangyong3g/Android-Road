#include <jni.h>
#include<malloc.h>
#include<string.h>

JNIEXPORT int JNICALL Java_com_ou_memory_Jni_malloc
  (JNIEnv * env, jobject thiz)
{
	int* test = (int*) malloc(sizeof(int) * 1024 * 1024 * 5);
	int size = sizeof(test);
	int i;
	int* p = test;
	for(i = 0; i < size; i++) {
		*p = 1;
		p++;
	}
	memset(test, 1, 1024 * 1024 * 20);
	return (int) test;
}

JNIEXPORT void JNICALL Java_com_ou_memory_Jni_free
  (JNIEnv * env, jobject thiz, jint p)
{
	free((int *)p);
}
