package com.ty.example_unit_3.libgdx;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

/**
 * 相机自动移动控制类
 * 
 * 
 * @author tang
 * 
 */
public class AutoRunCameraControler {

	public int mIndexOfDistrict = 0;

	// 第一
	public static final Vector3 SIX_POSITION_VECTOR3 = new Vector3(-10.8f,
			-9.2f, 39.9f);
	public static final Vector3 SIX_LOOK_AT_VECTOR3 = new Vector3(-10.3f,
			-9.2f, 19.5f);

	// 第二
	public static final Vector3 ONE_POSITION_VECTOR3 = new Vector3(42.7f,
			-3.7f, 1.7f);
	public static final Vector3 ONE_LOOK_AT_VECTOR3 = new Vector3(42.4f, -8.6f,
			-20.1f);

	// 第三个
	public static final Vector3 TWO_POSITION_VECTOR3 = new Vector3(-4.6f,
			-3.7f, -54.2f);
	public static final Vector3 TWO_LOOK_AT_VECTOR3 = new Vector3(-21.4f,
			-8.6f, -74.6f);

	// 第四个
	public static final Vector3 THREE_POSITION_VECTOR3 = new Vector3(-92.6f,
			41.4f, -28.1f);
	public static final Vector3 THREE_LOOK_AT_VECTOR3 = new Vector3(-86.8f,
			36.2f, -60f);

	// 第五个
	public static final Vector3 FOUR_POSITION_VECTOR3 = new Vector3(-40.8f,
			6.3f, -58.3f);
	public static final Vector3 FOUR_LOOK_AT_VECTOR3 = new Vector3(-64.4f,
			0.6f, -67.3f);

	// 第6个
	public static final Vector3 FIVE_POSITION_VECTOR3 = new Vector3(-32f, 1.4f,
			-11f);
	public static final Vector3 FIVE_LOOK_AT_VECTOR3 = new Vector3(-14.9f,
			-3.6f, -20.4f);

	private ArrayList<District> mDistrics;
	private AttributeListenr mYellowDunk;
	
	//当前相机位置运动单位向量
	private Vector3 mCurCameraRunUnit;
	//当前视点运动单位向量
	private Vector3 mCurLookAtRunUnit;

	class District {

		// 相机参数
		Vector3 mCameraStartPosition;
		Vector3 mCameraEndPosition;

		// 视点的参数
		Vector3 mLookAtStartPosition;
		Vector3 mLookAtEndPosition;

		int mDistrict;

		// 相机位置的单位向量
		Vector3 mCameraPositionUnit;

		// 视点的单位向量
		Vector3 mLookAtDirectUnit;

		// 每次相机位置移动的距离
		float mCameraUnitInstance;
		
		// 相机位置两点距离
		float mCameraTotalInstance;
		float mLookAtTotalInstance;
		float mLookAtUnitInstance;

		
		//初始化
		public void init(Vector3 cameraStartPosition,
				Vector3 cameraEndPosition, Vector3 lookAtStartPosition,
				Vector3 lookAtEndPosition, int distirct,
				AttributeListenr yellowDown) {

			mCameraStartPosition = cameraStartPosition;
			mCameraEndPosition = cameraEndPosition;

			mLookAtStartPosition = lookAtStartPosition;
			mLookAtEndPosition = lookAtEndPosition;

			mDistrict = distirct;

		
			mYellowDunk = yellowDown;
			
			
			mCameraTotalInstance = mCameraStartPosition.cpy().sub(mCameraEndPosition).len();
			mLookAtTotalInstance = mLookAtEndPosition.cpy().sub(mLookAtStartPosition).len();
			
			float rate = mCameraTotalInstance/mLookAtTotalInstance;
			
			mCameraPositionUnit = mCameraEndPosition.cpy().sub(mCameraStartPosition).nor().scl(0.1f);
			mLookAtDirectUnit = mLookAtEndPosition.cpy().sub(mLookAtStartPosition).nor().scl(0.1f*1/rate);
			
			mCameraUnitInstance = mCameraPositionUnit.len();
			mLookAtUnitInstance = mLookAtDirectUnit.len();

		}


		public boolean calCurCameraLookAtPos() {
			
			mYellowDunk.mCurrentCameraPos.add(mCameraPositionUnit);
			mYellowDunk.mCureentLookAtPos.add(mLookAtDirectUnit);
			
			float rate = mCameraTotalInstance/mLookAtTotalInstance;
			float  step  = mCameraPositionUnit.len();
			
			float  lastInstance =  mYellowDunk.mCurrentCameraPos.cpy().sub(mCameraEndPosition).len();
			float  lastLookAtInstance =  mYellowDunk.mCureentLookAtPos.cpy().sub(mLookAtEndPosition).len();
			
			String string = "区间:"+mIndexOfDistrict+"位置:["+mYellowDunk.mCurrentCameraPos+"]\t["+mYellowDunk.mCureentLookAtPos+"]";
			
			Gdx.app.log("test", string+"相机距离:"+ mYellowDunk.mCurrentCameraPos.cpy().sub(mCameraEndPosition).len()+":\t"+"\t视点距离:"+lastLookAtInstance+"\t相距:"+mCameraTotalInstance+"\t视距:"+mLookAtTotalInstance+"\t相机步频:"+mCameraUnitInstance+"\t视点步频:"+mLookAtUnitInstance+":\trate:"+rate);
			
			
			boolean isEnd = lastInstance < step;
			
			if(isEnd){
				
				mIndexOfDistrict += 1;
				mIndexOfDistrict = mIndexOfDistrict <= 5 ? mIndexOfDistrict : 0;
				
				District dis = mDistrics.get(mIndexOfDistrict);
				mYellowDunk.setCameraLookatPos(dis.mCameraStartPosition,dis.mLookAtStartPosition);
			}
			return isEnd;
		}
		
		public boolean isNextDistrict(){
			float instance = mYellowDunk.mCurrentCameraPos.cpy().sub(mCameraStartPosition).len();
			float maxInstance = mCameraTotalInstance;
			
			return instance > maxInstance;
		}

	}

	public void init(AttributeListenr yellowDunk) {

		if (mDistrics == null) {
			mDistrics = new ArrayList<District>();
		}

		District dis_0 = new District();
		dis_0.init(SIX_POSITION_VECTOR3, ONE_POSITION_VECTOR3,SIX_LOOK_AT_VECTOR3, ONE_LOOK_AT_VECTOR3, 0, yellowDunk);

		District dis_1 = new District();
		dis_1.init(ONE_POSITION_VECTOR3, TWO_POSITION_VECTOR3,ONE_LOOK_AT_VECTOR3, TWO_LOOK_AT_VECTOR3, 1, yellowDunk);

		District dis_2 = new District();
		dis_2.init(TWO_POSITION_VECTOR3, THREE_POSITION_VECTOR3,TWO_LOOK_AT_VECTOR3, THREE_LOOK_AT_VECTOR3, 2, yellowDunk);

		District dis_3 = new District();
		dis_3.init(THREE_POSITION_VECTOR3, FOUR_POSITION_VECTOR3,THREE_LOOK_AT_VECTOR3, FOUR_LOOK_AT_VECTOR3, 3, yellowDunk);

		District dis_4 = new District();
		dis_4.init(FOUR_POSITION_VECTOR3, FIVE_POSITION_VECTOR3,FOUR_LOOK_AT_VECTOR3, FIVE_LOOK_AT_VECTOR3, 4, yellowDunk);

		District dis_5 = new District();
		dis_5.init(FIVE_POSITION_VECTOR3, SIX_POSITION_VECTOR3,FIVE_LOOK_AT_VECTOR3, SIX_LOOK_AT_VECTOR3, 5, yellowDunk);

		mDistrics.add(dis_0);
		mDistrics.add(dis_1);
		mDistrics.add(dis_2);
		mDistrics.add(dis_3);
		mDistrics.add(dis_4);
		mDistrics.add(dis_5);

	}

	/**
	 * 计算当前时间的相机位置，目标点位置
	 */
	public void calculateCurPosition() {

		District mCurDistrict;

		switch (mIndexOfDistrict) {

		case 0:

			mCurDistrict = mDistrics.get(0);
			boolean isNextDistrict = mCurDistrict.calCurCameraLookAtPos();

			break;

		case 1:
			
			mCurDistrict = mDistrics.get(1);
			isNextDistrict = mCurDistrict.calCurCameraLookAtPos();

			break;

		case 2:
			
			mCurDistrict = mDistrics.get(2);
			isNextDistrict = mCurDistrict.calCurCameraLookAtPos();

			break;
		case 3:
			
			mCurDistrict = mDistrics.get(3);
			isNextDistrict = mCurDistrict.calCurCameraLookAtPos();

			break;
		case 4:
			
			mCurDistrict = mDistrics.get(4);
			isNextDistrict = mCurDistrict.calCurCameraLookAtPos();

			break;
			
		case 5:

			mCurDistrict = mDistrics.get(5);
			isNextDistrict = mCurDistrict.calCurCameraLookAtPos();

			break;

		default:
			break;
		}

	}

}
