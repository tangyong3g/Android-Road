/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphics.engine.gl.util;

/**
 * 对象池接口
 * @hide
 * @param <T>
 */
public interface Pool<T extends Poolable<T>> {
	/**
	 * <br>功能简述: 申请一个对象
	 * <br>功能详细描述:
	 * <br>注意: 使用完毕需要调用{@link #release(Poolable)}，将对象放回对象池。
	 * @return
	 */
	public abstract T acquire();
	
	/**
	 * <br>功能简述: 释放对象
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param element
	 */
	public abstract void release(T element);
}
