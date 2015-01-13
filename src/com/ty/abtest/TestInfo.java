package com.ty.abtest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 测试信息
 * 
 * 类名称：TestInfo
 * 类描述：
 * 创建人：makai
 * 修改人：makai
 * 修改时间：2014年11月24日 下午3:04:11
 * 修改备注：
 * @version 1.0.0
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestInfo {
	
	/**
	 * 发布时间
	 * releaseTime(这里用一句话描述这个方法的作用)
	 * (这里描述这个方法适用条件 – 可选)
	 * @return 
	 *String
	 * @exception 
	 * @since  1.0.0
	 */
	String startTime();
	
	/**
	 * 过期时间
	 * expireTime(这里用一句话描述这个方法的作用)
	 * (这里描述这个方法适用条件 – 可选)
	 * @return 
	 *int
	 * @exception 
	 * @since  1.0.0
	 */
	String endTime();
	
	/**
	 * 是否需要重新生产测试用例
	 * rebuild(这里用一句话描述这个方法的作用)
	 * (这里描述这个方法适用条件 – 可选)
	 * @return 
	 *boolean
	 * @exception 
	 * @since  1.0.0
	 */
	boolean rebuild() default false;
}
