/*
 ============================================================================
 Name        : detect-encrypt-string.c
 Author      : dengweiming
 Version     :
 Copyright   : Your copyright notice
 Description : 将detect-in.cpp中的常量字符串加密，生成detect.cpp。
 	 	 	   在开发阶段，在detect.cpp完成功能后，重命名成detect-in.cpp，使用gcc编译
 	 	 	   本文件并执行，将生成新的加密的detect.cpp，以避免攻击者用十六进制编辑器可以
 	 	 	   直接查看到so库中的常量字符串。即执行以下命令：
			   mv detect.cpp detect-in.cpp && gcc detect-encrypt-string.c && ./a.out && rm a.out
 	 	 	   最后使用ndk-build编译成最终的库（可以将makefile拷贝到工程根目录下再执行make）。
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int encode(char* src, char* dst) {
	char* start = dst;
	while (*src) {
		if (*src >= 'A' && *src <= 'Z') {
			*dst++ = 'z' - (*src++ - 'A');
		} else if (*src >= 'a' && *src <= 'z') {
			*dst++ = 'Z' - (*src++ - 'a');
		} else {
			*dst++ = *src++;
		}
	}
	return dst - start;
}

int main(void) {
	freopen("detect-in.cpp", "r", stdin);
	freopen("detect.cpp", "w", stdout);
	char inBuf[1024];
	char outBuf[1024];

	int enableConvert = 0; //use //\\ switch convert
	while (gets(inBuf)) {
		int i = 0;
		int j = 0;
		int slash = 0;
		while (inBuf[i]) {
			char ch = inBuf[i];
			if (ch == '/') {
				if (++slash == 2) {
					if (inBuf[i + 1] == '\\' && inBuf[i + 2] == '\\') {
						enableConvert = 1 - enableConvert;
					}
					break;
				}
			} else {
				slash = 0;
			}

			if (enableConvert && ch == '"') {
				int k;
				for (k = i + 1; inBuf[k] != 0 && inBuf[k] != '"'; ++k)
					;
				if (inBuf[k] != '"') {
					exit(0); //multilines string
				}

				outBuf[j++] = 'D';
				outBuf[j++] = '(';
				inBuf[k] = 0;
				//[i..k]
				j += encode(&inBuf[i + 1], &outBuf[j]);
				outBuf[j++] = ')';
				i = k + 1;
				continue;
			}

			outBuf[j++] = inBuf[i++];
		}

//		if(!slash) strcpy(&outBuf[j], &inBuf[i]);
//		else{
//			outBuf[j++] = '/';
//			outBuf[j++] = 0;
//		}
		strcpy(&outBuf[j], &inBuf[i]);
		puts(outBuf);
	}
	return 0;
}
