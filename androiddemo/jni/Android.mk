LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

#LOCAL_LDLIBS := -lstagefright


LOCAL_ARM_MODE := arm

LOCAL_MODULE := testlib

LOCAL_SRC_FILES := \
	testlib.c \

LOCAL_C_INCLUDES := \

include $(BUILD_SHARED_LIBRARY)