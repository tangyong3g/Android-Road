#include "Ms3dModel_jni.h"
#include "ms3d.h"
#include "ms3dAnimation.h"

#include <unistd.h>
#include <stdio.h>

static char sReadBuffer[BUFSIZ];
static char sErrorMessage[256];

jint Java_com_go_gl_model_Ms3dModel_loadModel
  (JNIEnv * env, jclass clazz, jint descriptor, jlong offset, jlong len) {
	descriptor = dup(descriptor);
	FILE* file = fdopen(descriptor, "rb");
	if (!file) {
		sprintf(sErrorMessage, "%s", "loadModel: can not open model file.");
		return 0;
	}
	setbuf(file, sReadBuffer);
	fseek(file, offset, SEEK_SET);

	CMs3d* model = new CMs3d(true);
	bool loaded = model->Load(file, len);
	if (!loaded) {
		delete model;
	}
	return (jint) model;
}

jint Java_com_go_gl_model_Ms3dModel_loadAnimation
  (JNIEnv * env, jclass clazz, jint modelPointer, jint descriptor, jlong offset, jlong len) {
	if (!modelPointer) {
		return 0;
	}
	CMs3d* model = (CMs3d*) modelPointer;
	descriptor = dup(descriptor);
	FILE* file = fdopen(descriptor, "rb");
	if (!file) {
		sprintf(sErrorMessage, "%s", "loadAnimation: can not open animation file.");
		return 0;
	}
	setbuf(file, sReadBuffer);
	fseek(file, offset, SEEK_SET);

	model->LoadPsa(file, len);
	CMs3dAnimation* animation = new CMs3dAnimation(model);
	return (jint) animation;
}

jstring Java_com_go_gl_model_Ms3dModel_getErrorMessage
  (JNIEnv * env, jclass clazz) {

	jstring msg = env->NewStringUTF(sErrorMessage);
	return msg;
}

void Java_com_go_gl_model_Ms3dModel_releaseModel
  (JNIEnv * env, jclass clazz, jint modelPointer) {
	if (!modelPointer) {
		return;
	}
	delete (CMs3d*) modelPointer;
}

void Java_com_go_gl_model_Ms3dModel_releaseAnimation
  (JNIEnv * env, jclass clazz, jint animPointer) {
	if (!animPointer) {
		return;
	}
	delete (CMs3dAnimation*) animPointer;
}

jint Java_com_go_gl_model_Ms3dModel_getGroupCount
  (JNIEnv * env, jclass clazz, jint modelPointer) {
	if (!modelPointer) {
		return -1;
	}
	CMs3d* model = (CMs3d*) modelPointer;
	return model->GetGroupCount();
}


jstring Java_com_go_gl_model_Ms3dModel_getTextureNames
  (JNIEnv * env, jclass clazz, jint modelPointer) {
	if (!modelPointer) {
		return 0;
	}
	CMs3d* model = (CMs3d*) modelPointer;
	char* buf = model->GetTextureNames();
	jstring names = env->NewStringUTF(buf);
	delete [] buf;
	return names;
}


jint Java_com_go_gl_model_Ms3dModel_getGroupTextureIndex
  (JNIEnv * env, jclass clazz, jint modelPointer, jint group) {
	if (!modelPointer) {
		return -1;
	}
	CMs3d* model = (CMs3d*) modelPointer;
	return model->GetGroupTextureIndex(group);
}


void Java_com_go_gl_model_Ms3dModel_renderGroup
  (JNIEnv * env, jclass clazz, jint modelPointer, jint group, jint positionHandle, jint texcoordHandle) {
	if (!modelPointer) {
		return;
	}
	CMs3d* model = (CMs3d*) modelPointer;
	model->RenderGroup(group, positionHandle, texcoordHandle);
}

jint Java_com_go_gl_model_Ms3dModel_playAnimation
  (JNIEnv * env, jclass clazz, jint animPointer, jint animId, jboolean accumulate, jfloat speed) {
	if (!animPointer) {
		return -1;
	}
	CMs3dAnimation* animation = (CMs3dAnimation*) animPointer;
	return animation->Play(animId, accumulate, speed, NULL);
}

void Java_com_go_gl_model_Ms3dModel_onAnimationRepeat
  (JNIEnv * env, jclass clazz, jint animPointer) {
	if (!animPointer) {
		return;
	}
	CMs3dAnimation* animation = (CMs3dAnimation*) animPointer;
	animation->OnAnimationRepeat();
}

void Java_com_go_gl_model_Ms3dModel_onAnimationEnd
  (JNIEnv * env, jclass clazz, jint animPointer) {
	if (!animPointer) {
		return;
	}
	CMs3dAnimation* animation = (CMs3dAnimation*) animPointer;
	animation->OnAnimationEnd();
}

void Java_com_go_gl_model_Ms3dModel_onAnimationUpdate
  (JNIEnv * env, jclass clazz, jint animPointer, jfloat normalizedTime) {
	if (!animPointer) {
		return;
	}
	CMs3dAnimation* animation = (CMs3dAnimation*) animPointer;
	animation->DoAnimate(normalizedTime);
}

void Java_com_go_gl_model_Ms3dModel_fixAnimationTranslation
  (JNIEnv * env, jclass clazz, jint modelPointer, jint animId, jint xyzMask, jfloat dx, jfloat dy, jfloat dz) {
	if (!modelPointer) {
		return;
	}
	CMs3d* model = (CMs3d*) modelPointer;
	CVector3 translation = model->GetAnimationTranslation(animId);
	float* value = translation.Get();
	if (xyzMask & 1)
		value[0] = dx;
	if (xyzMask & 2)
		value[1] = dy;
	if (xyzMask & 4)
		value[2] = dz;
	model->FixAnimationTranslation(animId, translation);
}


void Java_com_go_gl_model_Ms3dModel_fixAnimationRotation
  (JNIEnv * env, jclass clazz, jint modelPointer, jint animId, jfloat x, jfloat y, jfloat z, jfloat w) {
	if (!modelPointer) {
		return;
	}
	CMs3d* model = (CMs3d*) modelPointer;
	CQuaternion q(x, y, z, w);
	model->FixAnimationRotation(animId, q);

}
