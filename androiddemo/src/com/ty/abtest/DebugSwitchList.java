package com.ty.abtest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * debug开关列表
 * 
 * 类名称：DebugSwitchList
 * 类描述：请不要人为进行相应的修改
 * 创建人：makai
 * 修改人：makai
 * 修改时间：2014年11月24日 下午5:53:08
 * 修改备注：
 * @version 1.0.0
 *
 */
public class DebugSwitchList {

	@DebugInfo(key = "is_test_base_service")
	public static boolean sIsTestBaseService = false;

	@DebugInfo(key = "is_test_shop_app_download_service")
	public static boolean sIsShopApkDownloadTestService = false;

	@DebugInfo(key = "abtest_user_type")
	public static String sABTest_USER_TYPE = ABTest.AUTO_USER;

	/**
	 * debug的key信息
	 * 
	 * 类名称：DebugInfo
	 * 类描述：
	 * 创建人：makai
	 * 修改人：makai
	 * 修改时间：2014年11月24日 下午5:53:37
	 * 修改备注：
	 * @version 1.0.0
	 *
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface DebugInfo {

		String key();

	}

}
