package com.tcl.mig.staticssdk;
/**
 * 
 * <br>类描述:DB异步TASK
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2014年6月9日]
 */
public class DBAsyncTask implements Runnable {

	private Runnable mRunnable;
	private AsyncCallBack mCallBack;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (mRunnable != null) {
			mRunnable.run();
		}
		if (mCallBack != null) {
			mCallBack.onFinish();
		}
	}

	public void addTask(Runnable runnable) {
		mRunnable = runnable;
	}
	public void addCallBack(AsyncCallBack callBack) {
		mCallBack = callBack;
	}

	/**
	 * 
	 * <br>类描述:回调
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2014年6月9日]
	 */
	public interface AsyncCallBack {
		public void onFinish();
	}
}
