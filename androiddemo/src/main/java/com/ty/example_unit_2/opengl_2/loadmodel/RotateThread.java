package com.ty.example_unit_2.opengl_2.loadmodel;

/**
 * 
 * @author tangyong
 * 
 */
public class RotateThread implements Runnable {

	LoadedObjectVertexOnly obj;
	private boolean flag = false;
	private static final int timeSpan = 35;

	public RotateThread(LoadedObjectVertexOnly obj) {
		this.obj = obj;
	}

	@Override
	public void run() {

		while (flag) {
		//	obj.senorRatio();//调用使所有球运动的方法
			
			try{
				Thread.sleep(timeSpan);//一段时间后再运动
			}
			catch(Exception e){
				e.printStackTrace();//打印异常
			}
		}
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

}
