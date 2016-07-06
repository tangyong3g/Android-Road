package com.graphics.engine.gl.math3d;


import com.graphics.engine.gl.util.StackPool;

/**
 * 
 * <br>类描述: 几何对象池
 * <br>功能详细描述:
 * <p>因为几何对象的运算会生成临时对象，为了避免造成内存碎片和引起GC，使用这个对象池来管理
 * 这些临时对象，只需要调用{@link #acquirePoint()}等方法申请，不需要显式释放每个对象。</p> 
 * <p>申请前要调用{@link #saveStack()}，之后可以多次调用acquire*方法，或者进行几何运算，
 * 使用完要调用{@link #restoreStack()}或者{@link #restoreStackToCount(int)}将期间
 * 申请的临时对象都释放掉。</p>
 * <p>注意不要在外部保存这些临时对象的引用，因为释放后会被后续申请者覆写，应该使用对象的set/setTo方法拷贝
 * 内容到本地对象（或成员变量）。</p>
 * 
 * <p>使用示例：
 * <pre><code>
 * 	Point mPoint = new Point();	//声明一个成员变量，使用new的方式
 * 	void method() {
 * 		final int saveCount = GeometryPools.saveStack();
 * 		Point point = GeometryPools.acquirePoint().set(1, 0, 0);
 * 		Matrix matrix = GeometryPools.acquireMatrix()
 * 			.setTranslate(1, 0)
 * 			.rotate(Math3D.HALF_DEGREES)
 * 			.scale(2, 2);
 * 		matrix = matrix.translate(1, 1);		//注意translate生成一个新的临时对象，不是修改matrix
 * 		point.transform(matrix).setTo(mPoint);	//如果后续需要使用计算结果，使用setTo方法保存到mPoint
 * 		GeometryPools.restoreStackToCount(saveCount);
 * 	}
 * </code></pre>
 * </p>
 * 
 * <p>在循环语句内申请对象或者进行几何运算，很可能会使得对象池超出容量限制，也会造成内存碎片，改进的办法是
 * 在循环体内包含save和restore，或者隔几次循环就调用一次：
 * <pre><code>
 * int oldSaveCount = GeometryPools.saveStack();
 * int saveCount = oldSaveCount;
 * for (int i = 0; i < count; ++i) {
 * 	if (i % 8 == 0) {	//每8次循环释放一次
 * 		if (i > 0) {
 * 			GeometryPools.restoreStackToCount(saveCount);
 * 		}
 * 		saveCount = GeometryPools.saveStack();
 * 	}
 * 	//do sth.
 * }
 * GeometryPools.restoreStackToCount(oldSaveCount);
 * </code></pre>
 * </p>
 * 
 * <p><em>【警告】</em>：不要在非主线程使用这个类的方法，以及这个包里面其他类会间接使用的方法，否则会造成混乱。</p>
 * 
 * @author  dengweiming
 * @date  [2013-7-1]
 */
public class GeometryPools {
	
	static final int POINT_POOL_LIMIT = 512;
	static final String POINT_POOL_NAME = "PointPool";;

	static final int VECTOR_POOL_LIMIT = 512;
	static final String VECTOR_POOL_NAME = "VectorPool";
	
	static final int QUATERNION_POOL_LIMIT = 256;
	static final String QUATERNION_POOL_NAME = "QuaternionPool";
	
	static final int MATRIX_POOL_LIMIT = 256;
	static final String MATRIX_POOL_NAME = "MatrixPool";
	
	static final int RAY_POOL_LIMIT = 128;
	static final String RAY_POOL_NAME = "RayPool";
	
	static final int SPHERE_POOL_LIMIT = 128;
	static final String SPHERE_POOL_NAME = "SpherePool";
	
	static final int AABB_POOL_LIMIT = 128;
	static final String AABB_POOL_NAME = "AABBPool";

	/**
	 * <br>功能简述: 获取一个点
	 * <br>功能详细描述: 不会重设，需要调用set方法
	 * <br>注意:
	 * @return
	 */
	public static Point acquirePoint() {
		return (Point) sPointPool.acquireData();
	}
	
	/**
	 * <br>功能简述: 获取一个向量
	 * <br>功能详细描述: 不会重设，需要调用set方法
	 * <br>注意:
	 * @return
	 */
	public static Vector acquireVector() {
		return (Vector) sVectorPool.acquireData();
	}
	
	/**
	 * <br>功能简述: 获取一个四元数
	 * <br>功能详细描述: 会重设为零四元数
	 * <br>注意:
	 * @return
	 */
	public static Quaternion acquireQuaternion() {
		return ((Quaternion) sQuaternionPool.acquireData()).reset();
	}
	
	static Quaternion acquireRawQuaternion() {
		return (Quaternion) sQuaternionPool.acquireData();
	}
	
	/**
	 * <br>功能简述: 获取一个矩阵
	 * <br>功能详细描述: 会重设为单位矩阵
	 * <br>注意:
	 * @return
	 */
	public static Matrix acquireMatrix() {
		return ((Matrix) sMatrixPool.acquireData()).identity();
	}
	
	static Matrix acquireZeroMatrix() {
		return ((Matrix) sMatrixPool.acquireData()).zero();
	}
	
	static Matrix acquireRawMatrix() {
		return (Matrix) sMatrixPool.acquireData();
	}
	
	public static Ray acquireRay() {
		return (Ray) sRayPool.acquireData(); 
	}
	
	/**
	 * <br>功能简述: 获取一个球体
	 * <br>功能详细描述:  不会重设，需要调用set方法
	 * <br>注意:
	 * @return
	 */
	public static Sphere acquireSphere() {
		return (Sphere) sSpherePool.acquireData();
	}
	
	/**
	 * <br>功能简述: 获取一个AABB包围盒
	 * <br>功能详细描述:  不会重设，需要调用set方法
	 * <br>注意:
	 * @return
	 */
	public static AABB acquireAABB() {
		return (AABB) sAABBPool.acquireData();
	}

	/**
	 * <br>功能简述: 保存对象池的状态
	 * <br>功能详细描述:
	 * <br>注意:
	 * 
	 * @see {@link #restoreStack()}
	 * @see {@link #restoreStackToCount(int)}
	 */
	public static int saveStack() {
		final int saveCount = sPointPool.saveStack();
		sVectorPool.saveStack();
		sQuaternionPool.saveStack();
		sMatrixPool.saveStack();
		sRayPool.saveStack();
		sSpherePool.saveStack();
		sAABBPool.saveStack();
		return saveCount;
	}

	/**
	 * <br>功能简述: 还原对象池的状态
	 * <br>功能详细描述: 在{@link #saveStack()} 之后调用 acquire* 申请到的临时对象都会被回收
	 * <br>注意:
	 * 
	 * @see {@link #saveStack()}
	 * @see {@link #restoreStackToCount(int)}
	 */
	public static void restoreStack() {
		sPointPool.restoreStack();
		sVectorPool.restoreStack();
		sQuaternionPool.restoreStack();
		sMatrixPool.restoreStack();
		sRayPool.restoreStack();
		sSpherePool.restoreStack();
		sAABBPool.restoreStack();
	}
	
	/**
	 * <br>功能简述: 还原对象池的状态
	 * <br>功能详细描述: 在{@link #saveStack()} 之后调用 acquire* 申请到的临时对象都会被回收
	 * <br>注意:
	 * @param saveCount	之前调用{@link #saveStack()}返回的参数
	 * 
	 * @see {@link #saveStack()}
	 * @see {@link #restoreStack()}
	 */
	public static void restoreStackToCount(int saveCount) {
		sPointPool.restoreStackToCount(saveCount);
		sVectorPool.restoreStackToCount(saveCount);
		sQuaternionPool.restoreStackToCount(saveCount);
		sMatrixPool.restoreStackToCount(saveCount);
		sRayPool.restoreStackToCount(saveCount);
		sSpherePool.restoreStackToCount(saveCount);
		sAABBPool.restoreStackToCount(saveCount);
	}

	static StackPool sPointPool = new StackPool(new StackPool.DataManager<Point>() {

		@Override
		public Point newInstance() {
			return new Point();
		}

		@Override
		public void onAcquired(Point data) {
		}

		@Override
		public void onReleased(Point data) {
		}

	}, POINT_POOL_LIMIT, POINT_POOL_NAME);

	static StackPool sVectorPool = new StackPool(new StackPool.DataManager<Vector>() {

		@Override
		public Vector newInstance() {
			return new Vector();
		}

		@Override
		public void onAcquired(Vector data) {
		}

		@Override
		public void onReleased(Vector data) {
		}

	}, VECTOR_POOL_LIMIT, VECTOR_POOL_NAME);
	
	static StackPool sQuaternionPool = new StackPool(new StackPool.DataManager<Quaternion>() {
		
		@Override
		public Quaternion newInstance() {
			return new Quaternion();
		}
		
		@Override
		public void onAcquired(Quaternion data) {
		}
		
		@Override
		public void onReleased(Quaternion data) {
		}
		
	}, QUATERNION_POOL_LIMIT, QUATERNION_POOL_NAME);
	
	static StackPool sMatrixPool = new StackPool(new StackPool.DataManager<Matrix>() {
		
		@Override
		public Matrix newInstance() {
			return new Matrix(0);
		}
		
		@Override
		public void onAcquired(Matrix data) {
		}
		
		@Override
		public void onReleased(Matrix data) {
		}
		
	}, MATRIX_POOL_LIMIT, MATRIX_POOL_NAME);
	
	static StackPool sRayPool = new StackPool(new StackPool.DataManager<Ray>() {
		
		@Override
		public Ray newInstance() {
			return new Ray();
		}
		
		@Override
		public void onAcquired(Ray data) {
		}
		
		@Override
		public void onReleased(Ray data) {
		}
		
	}, RAY_POOL_LIMIT, RAY_POOL_NAME);
	
	static StackPool sSpherePool = new StackPool(new StackPool.DataManager<Sphere>() {
		
		@Override
		public Sphere newInstance() {
			return new Sphere();
		}
		
		@Override
		public void onAcquired(Sphere data) {
		}
		
		@Override
		public void onReleased(Sphere data) {
		}
		
	}, SPHERE_POOL_LIMIT, SPHERE_POOL_NAME);
	
	static StackPool sAABBPool = new StackPool(new StackPool.DataManager<AABB>() {
		
		@Override
		public AABB newInstance() {
			return new AABB();
		}
		
		@Override
		public void onAcquired(AABB data) {
		}
		
		@Override
		public void onReleased(AABB data) {
		}
		
	}, AABB_POOL_LIMIT, AABB_POOL_NAME);

}