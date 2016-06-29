package com.ty.example_unit_2.opengl_2.loadmodel;

public class ControlerThread extends Thread{
	
	private boolean flag = false;
	private static final int internal = 12;
	LoadedObjectVertexOnly object = null;
	
	public ControlerThread(LoadedObjectVertexOnly obj) {
		this.object = obj;
	}
	
	@Override
	public void run() {
		while (flag) {
			
		//	object.senorRatio();
			
			try {
				Thread.sleep(internal);	
			} catch (Exception e) {
			}
		}
	}
	
	public void setFlag(boolean flag){
		this.flag = flag;
	}

}
