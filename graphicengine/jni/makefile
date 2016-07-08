# 环境变量OS在linux下不用设置，Windows已经自带
# 添加系统环境变量NDK指向你的ndk目录
# 在Windows下，将d:\cygwin\bin;添加到系统环境变量Path的最前面（最高优先级）
# 修改环境变量后需要重启eclipse

ifeq (Windows_NT, $(OS))
	LINUXER=
else
	LINUXER=1
endif

all:
ifdef LINUXER
	$(NDK)/ndk-build -C jni
else
	bash -c "'$(NDK)/ndk-build' -C '$(CURDIR)/jni'"
endif
	
clean:
ifdef LINUXER
	$(NDK)/ndk-build clean -C jni
else	
	bash -c "'$(NDK)/ndk-build' clean -C '$(CURDIR)/jni'"
endif


