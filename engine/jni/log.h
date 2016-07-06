/*
 * log.h
 *
 *  Created on: 2012-1-6
 *      Author: dengweiming
 *
 *  Usage:
 *  1. 在#include "log.h" 之前可以定义以下宏，来实施对文件的独立控制：
 *  	LOG_IO		输出方式：LOG_IO_STD（标准输出），LOG_IO_FILE（日志文件），LOG_IO_ANDROID（android日志）
 * 		LOG_TAG		日志的标签
 *  	LOG_FILE	日志文件（如果输出到文件）
 *  	日志等级		按照优先级从低往高列出，定义了该日志等级则该等级及更高等级的日志会被打印（如果没定以则不会打印）
 *  		LOG_LEVEL_V	verbose
 *  		LOG_LEVEL_D debug
 *  		LOG_LEVEL_I info
 *  		LOG_LEVEL_W warn
 *  		LOG_LEVEL_E error
 *  2. 如果没有对以上宏进行定义，则使用对应的（全局）默认值
 *  	LOG_DEFAULT_IO
 *  	LOG_DEFAULT_TAG
 *  	LOG_DEFAULT_FILE
 *  3. 在其他非系统头文件之前#include "log.h"，防止其他头文件也包含了并且先包含了，导致以上宏不起作用
 *  4. 可供使用的打印方法
 *  	LOGV(...)	打印verbose等级的日志
 *  	LOGD(...)	打印debug等级的日志
 *  	LOGI(...)	打印info等级的日志
 *  	LOGW(...)	打印warn等级的日志
 *  	LOGE(...)	打印error等级的日志
 *  	LOGFILE()	打印源文件名称及行号（debug等级）
 *  	LOGFUNC()	打印函数名称及行号（debug等级）
 *  	LOGENTER()	打印进入函数的指示（debug等级）
 *  	LOGLEAVE()	打印离开函数的指示（debug等级）
 *  	LOGASSERT(expression)	检查expression是否为true，否则退出程序（在LOG_IO不为0并且日志等级有定义时才有效）
 *  	LOGCLEAR()	清除日志文件内容
 *  	LOGE_DETAIL(format, ...) 在打印的错误日志中加入函数名，文件名和行号信息
 *  5. 将以下的这句取消注释，可以全局禁止日志打印
 *  	#undef LOG_IO
 *  6. 将以下宏注释掉，可以全局禁止对应等级的日志打印
 *  	#define LOG_ENUM_V ...
		#define LOG_ENUM_D ...
		#define LOG_ENUM_I ...
		#define LOG_ENUM_W ...
		#define LOG_ENUM_E ...
 */

#ifndef LOG_H_
#define LOG_H_

#include <stdarg.h>

#ifdef __cplusplus
extern "C" {
#endif

//输出类型
#define LOG_IO_INVALID	0			/* 禁止打印日志 */
#define LOG_IO_STD		1			/* 标准输出流 */
#define LOG_IO_FILE		2			/* 输出到文件 */
#define LOG_IO_ANDROID	3			/* android ndk的日志输出 */

//TODO:修改默认值
//默认输出类型
#define LOG_DEFAULT_IO LOG_IO_ANDROID
//默认标签
#define LOG_DEFAULT_TAG "Launchershell"
//默认日志文件（如果输出到文件）
#define LOG_DEFAULT_FILE "/sdcard/log.txt"

//使用的输出类型
#ifndef LOG_IO
	#define LOG_IO LOG_DEFAULT_IO
#endif
//使用的标签
#ifndef LOG_TAG
	#define LOG_TAG LOG_DEFAULT_TAG
#endif
//使用的日志文件（如果输出到文件）
#ifndef LOG_FILE
	#define LOG_FILE LOG_DEFAULT_FILE
#endif

//TODO:全局禁止日志打印
//#undef LOG_IO

//对不同输出类型，定义不同的输出函数
#if LOG_IO == LOG_IO_STD

	#include <stdio.h>
	#define LOG_ENUM_V "V"
	#define LOG_ENUM_D "D"
	#define LOG_ENUM_I "I"
	#define LOG_ENUM_W "W"
	#define LOG_ENUM_E "E"
	#define LOG_PRINT(PRIO, TAG, ...) \
		do{ \
			printf("%s/%s\t", PRIO, TAG);\
			printf(__VA_ARGS__);\
			printf("\n");\
		}while(0)

#elif LOG_IO == LOG_IO_FILE
	#include <stdio.h>
	#define LOG_ENUM_V "V"
	#define LOG_ENUM_D "D"
	#define LOG_ENUM_I "I"
	#define LOG_ENUM_W "W"
	#define LOG_ENUM_E "E"
	#define LOG_PRINT(PRIO, TAG, ...) \
		do{ \
			FILE* f = fopen(LOG_FILE, "a"); \
			if(f){ \
				fprintf(f, "%s/%s\t", PRIO, TAG);\
				fprintf(f, __VA_ARGS__);\
				fprintf(f, "\n");\
				fclose(f); \
			} \
		}while(0)

#elif LOG_IO == LOG_IO_ANDROID

	#include <android/log.h>
	#define LOG_ENUM_V ANDROID_LOG_VERBOSE
	#define LOG_ENUM_D ANDROID_LOG_DEBUG
	#define LOG_ENUM_I ANDROID_LOG_INFO
	#define LOG_ENUM_W ANDROID_LOG_WARN
	#define LOG_ENUM_E ANDROID_LOG_ERROR
	#define LOG_PRINT(PRIO, TAG, ...) \
		__android_log_print(PRIO, TAG, __VA_ARGS__)

#else
	#define LOG_PRINT(...)
#endif

//根据使用输出等级定义日志函数
#ifdef LOG_LEVEL_V
	#ifdef LOG_ENUM_V
		#define LOGV(...) LOG_PRINT(LOG_ENUM_V, LOG_TAG, __VA_ARGS__)
	#endif
	#define LOG_LEVEL_D
#endif
#ifndef LOGV
	#define LOGV(...)
#endif

#ifdef LOG_LEVEL_D
	#ifdef LOG_ENUM_D
		#define LOGD(...) LOG_PRINT(LOG_ENUM_D, LOG_TAG, __VA_ARGS__)
	#endif
	#define LOG_LEVEL_I
#endif
#ifndef LOGD
	#define LOGD(...)
#endif

#ifdef LOG_LEVEL_I
	#ifdef LOG_ENUM_I
		#define LOGI(...) LOG_PRINT(LOG_ENUM_I, LOG_TAG, __VA_ARGS__)
	#endif
	#define LOG_LEVEL_W
#endif
#ifndef LOGI
	#define LOGI(...)
#endif

#ifdef LOG_LEVEL_W
	#ifdef LOG_ENUM_W
		#define LOGW(...) LOG_PRINT(LOG_ENUM_W, LOG_TAG, __VA_ARGS__)
	#endif
	#define LOG_LEVEL_E
#endif
#ifndef LOGW
	#define LOGW(...)
#endif

#ifdef LOG_LEVEL_E
	#ifdef LOG_ENUM_E
		#define LOGE(...) LOG_PRINT(LOG_ENUM_E, LOG_TAG, __VA_ARGS__)
	#endif
#endif
#ifndef LOGE
	#define LOGE(...)
#endif

//一些扩展的日志函数
#define LOGFILE()  \
	LOGD("%s : %d", __FILE__, __LINE__)
#define LOGFUNC()  \
	LOGD("%s : %4d", __PRETTY_FUNCTION__, __LINE__)
#define LOGENTER() \
	LOGD("%s : %4d v", __PRETTY_FUNCTION__, __LINE__)
#define LOGLEAVE() \
	LOGD("%s : %4d ^", __PRETTY_FUNCTION__, __LINE__)
#define LOGE_DETAIL(format, ...) \
	LOGE("In function \'%s\':\n\t%s:%d: "format, __PRETTY_FUNCTION__, __FILE__, __LINE__, __VA_ARGS__)

#if LOG_IO == LOG_IO_INVALID
	#define LOGASSERT(EXPRESSION)
#else
	#ifndef LOG_LEVEL_E
		#define LOGASSERT(EXPRESSION)
	#else
		#include <assert.h>
		#define LOGASSERT(EXPRESSION) \
		  if(!(EXPRESSION)){ \
			  LOGE("Assertion failed: %s, %s:%d\n", #EXPRESSION, __FILE__, __LINE__); \
			  exit(1); \
		  }
//		  (void)((EXPRESSION) \
//				  || (LOGE("Assertion failed: %s, %s:%d\n", #EXPRESSION, __FILE__, __LINE__), false) \
//				  || (exit(1), true))
	#endif
#endif

#if LOG_IO == LOG_IO_FILE
	#define LOGCLEAR() \
		do{ \
			FILE* f = fopen(LOG_FILE, "w"); \
			fclose(f); \
		}while(0)
#else
	#define LOGCLEAR()
#endif

#ifdef __cplusplus
}
#endif

#endif /* LOG_H_ */
