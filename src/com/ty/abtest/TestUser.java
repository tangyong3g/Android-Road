package com.ty.abtest;
/**
 * 测试用户集合
 * 
 * 类名称：TestUser
 * 类描述：
 * 创建人：makai
 * 修改人：makai
 * 修改时间：2014年11月24日 下午2:29:07
 * 修改备注：
 * @version 1.0.0
 *
 */
public class TestUser {

	@TestUserModel
	public static final String USER_A = "a";
	
	@TestUserModel
	public static final String USER_B = "b";
	
	@TestUserModel(isTestUser = true)
	public static final String USER_C = "c";
	
	@TestUserModel(isTestUser = false)
	public static final String USER_D = "d";
	
	@TestUserModel(isTestUser = false)
	public static final String USER_E = "e";
	
	@TestUserModel(isTestUser = false)
	public static final String USER_F = "f";
	
	@TestUserModel(isTestUser = false)
	public static final String USER_G = "g";
	
	@TestUserModel(isTestUser = false)
	public static final String USER_H = "h";
	
	@TestUserModel(isTestUser = false)
	public static final String USER_I = "i";
	
	@TestUserModel(isTestUser = false)
	public static final String USER_J = "j";
	
	@TestUserModel(isTestUser = false, isUpGradeUser = true)
	public static final String USER_Z = "z";
	
}
