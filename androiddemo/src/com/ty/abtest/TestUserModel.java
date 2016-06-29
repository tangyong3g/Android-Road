package com.ty.abtest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 测试用户模型
 * 
 * 类名称：TestUserModel
 * 类描述：
 * 创建人：makai
 * 修改人：makai
 * 修改时间：2014年11月24日 下午2:29:28
 * 修改备注：
 * @version 1.0.0
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestUserModel {

	/**
	 * 是否需要测试
	 * isTest(这里用一句话描述这个方法的作用)
	 * (这里描述这个方法适用条件 – 可选)
	 * @return 
	 *boolean
	 * @exception 
	 * @since  1.0.0
	 */
	boolean isTestUser() default true;
	
	/**
	 * 是否是默认用户
	 * isDefault(这里用一句话描述这个方法的作用)
	 * (这里描述这个方法适用条件 – 可选)
	 * @return 
	 *boolean
	 * @exception 
	 * @since  1.0.0
	 */
	boolean isDefaultUser() default false; 
	
	/**
	 * 是否是升级用户
	 * isUpGradeUser(这里用一句话描述这个方法的作用)
	 * (这里描述这个方法适用条件 – 可选)
	 * @return 
	 *boolean
	 * @exception 
	 * @since  1.0.0
	 */
	boolean isUpGradeUser() default false;
	
	/**
	 * 出现的概率
	 * odds(这里用一句话描述这个方法的作用)
	 * (这里描述这个方法适用条件 – 可选)
	 * @return 
	 *int
	 * @exception 
	 * @since  1.0.0
	 */
	int odds() default 1;
}
