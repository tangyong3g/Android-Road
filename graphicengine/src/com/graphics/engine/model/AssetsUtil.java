package com.graphics.engine.model;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

/**
 * 
 * <br>类描述: 操作assets资源的工具类
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-11-25]
 */
public class AssetsUtil {
	
	/**
	 * <br>功能简述: 从assets目录下打开资源文件，以便在NDK中使用
	 * @param assetManager
	 * @param fileName 文件名称，如果不是带".mp3"后缀的，那么需要在build.xml中配置不压缩	//TODO
	 * @return
	 */
	public static OpenAssetFileResult openAssetFile(AssetManager assetManager, String fileName) {
		Method getIntMethod;
		try {
			getIntMethod = FileDescriptor.class.getDeclaredMethod("getInt$", (Class[]) null);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
		
		AssetFileDescriptor afd = null;
		try {
			afd = assetManager.openFd(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FileDescriptor fd = null;
		OpenAssetFileResult result = new OpenAssetFileResult();
		if (afd != null) {
			fd = afd.getFileDescriptor();
		}

		if (fd != null) {
			try {
				Integer ret = (Integer) getIntMethod.invoke(fd, (Object[]) null);
				result.descriptor = ret.intValue();
				result.len = afd.getLength();
				result.offset = afd.getStartOffset();
				return result;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		return null;

	}
	
	/**
	 * 打开Asset文件时返回的结果。
	 * <br>在NDK中使用的示例代码
	 * <pre><code>
	 * #include &lt;unistd.h&gt;
	 * #include &lt;stdio.h&gt;
	 * //static char sBuffer[BUFSIZ];
	 * 
	 * void foo(int despriptor, int offset, int len) {
	 * 	int descriptor = dup(despriptor);
	 *	FILE* file = fdopen(descriptor, "rb");	//"read-binary" mode or others 
	 *	if (file) {
	 *		//setbuf(file, sBuffer);	//optional buffer for optimization
	 *		fseek(file, offset, SEEK_SET);
	 *
	 *		//do something...
	 *
	 *		fclose(file);
	 *	}
	 * }	
	 * </code></pre>
	 */
	public static class OpenAssetFileResult {
		//CHECKSTYLE IGNORE 6 LINES
		/** 文件描述符 */
		int descriptor;
		/** 文件起始位置 */
		long offset;
		/** 文件内容长度 */
		long len;
	}
}
