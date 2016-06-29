package com.ty.libgdxusers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * 扩展了正交投影相机　
 * 
 * @author tangyong
 * 
 */
public class GuOrthoCam extends OrthographicCamera implements InputProcessor,
		GuCamera {

	private static final float DEG = (float) (Math.PI / 180f);
	private static final int CAMSPEED = 40;
	private static Vector3 campos;
	private Vector3 dir;
	private Vector2 fieldSize;
	private Vector3 targetVec = new Vector3(); // this equals (0,0,0)
	private static float vpHeight;
	private static final float CAM_NEAR_INITIAL = 0.1f;
	private static final float CAM_FAR_INITIAL = 200f;

	// viewSize should be related to the size of your scene
	// scale compared to grid_size - bigger means more area around the playing
	// field
	private static final float VIEW_ZOOM = .5f;
	private static final float CAM_HEIGHT = 10; // how high up the cam is

	private static Vector3 camPos;

	private static Vector3 CAM_LOOKAT_INITIAL;
	private static Vector3 CAM_UP_INITIAL = new Vector3(0, 1, 0);
	private static float relative_rotation_angle = 0;

	// for InputProcessor methods
	// final Plane xzPlane = new Plane(new Vector3(0, 1, 0), 0);
	final Plane xzPlane = new Plane(new Vector3(0, 0, 1), 0);
	final Vector3 intersection = new Vector3();
	final Vector3 curr = new Vector3();
	final Vector3 last = new Vector3(-1, -1, -1);
	final Vector3 delta = new Vector3();
	private MeshHelper targetObj;

	public GuOrthoCam(float vpw, float vph, Vector2 field) {
		// super method calls an update() so all better be ready in our update
		super(field.x * VIEW_ZOOM, field.x * VIEW_ZOOM * (vph / vpw));
		vpHeight = vph;
		fieldSize = field;
		init();
	}

	public void setTargetObj(MeshHelper mh) {
		targetObj = mh;
	}

	public void setTargetVec(float x, float y, float z) {
		Log.out("set TargetVector to: " + x + "," + y + "," + z);
		targetVec.set(x, y, z);
	}

	public void init() {
		relative_rotation_angle = 0;
		up.set(CAM_UP_INITIAL);
		camPos = new Vector3(-fieldSize.x, CAM_HEIGHT, 2 * fieldSize.y);
		position.set(camPos);
		setTargetVec(fieldSize.x / 2, 0, fieldSize.y / 2); // set default pos
		// updateLookAt();
		// update();
		logInfo();
	}

	@Override
	public void update() {
		updateLookAt();
		super.update();
	}

	public void updateLoc() {

	}

	private void updateLookAt() {
		// if (targetVec == null) {
		// Log.out("caught updateLookAt before init()");
		// return;
		// }
		// lookAt(targetVec.x, targetVec.y, targetVec.z);
		if (targetObj != null) {
			lookAt(targetObj.pos.x, targetObj.pos.y, targetObj.pos.z);
		}
	}

	private void logInfo() {
		Log.out("Reset Camera:");
		Log.out("Cam-Up:        " + up);
		Log.out("Cam-Position:  " + position);
		Log.out("Cam-Direction: " + direction);
		Log.out("Cam-LookAt: " + CAM_LOOKAT_INITIAL);
		Log.out("VP_HEIGHT: " + vpHeight);
	}

	public void push() {
		campos = position;
		dir = this.direction;
	}

	public void pop() {
		position.set(campos);
		this.direction.set(dir);
	}

	public void handleKeys() {
		float amt = CAMSPEED * Gdx.graphics.getDeltaTime();

		if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
			translate(direction.x, 0, direction.z);
		}
		if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) {
			translate(-direction.x, 0, -direction.z);
		}

		if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
			goLeft();
		}
		if (Gdx.input.isKeyPressed(Keys.D)
				|| Gdx.input.isKeyPressed(Keys.RIGHT)) {
			goRight();
		}
		if (Gdx.input.isKeyPressed(Keys.E)) {
			rotate(-amt, 0, 1, 0);
		}
		if (Gdx.input.isKeyPressed(Keys.Q)) {
			rotate(amt, 0, 1, 0);
		}

		if (Gdx.input.isKeyPressed(Keys.U)) {
			rotate(-amt, 1, 0, 0);
		}
		if (Gdx.input.isKeyPressed(Keys.J)) {
			rotate(amt, 1, 0, 0);
		}
		if (Gdx.input.isKeyPressed(Keys.H)) {
			rotate(amt, 0, 0, 1);
		}
		if (Gdx.input.isKeyPressed(Keys.K)) {
			rotate(-amt, 0, 0, 1);
		}

		// moves NEAR away from cam
		if (Gdx.input.isKeyPressed(Keys.PLUS)) {
			near += amt;
			if (near > CAM_FAR_INITIAL) {
				near = CAM_FAR_INITIAL;
			}
			Log.out("NEAR:" + near);
		}
		// moves NEAR closer to cam
		if (Gdx.input.isKeyPressed(Keys.MINUS)) {
			near -= amt;
			if (near < CAM_NEAR_INITIAL) {
				near = CAM_NEAR_INITIAL;
			}
			Log.out("NEAR:" + near);
		}

		// Zoom in
		if (Gdx.input.isKeyPressed(Keys.T)) {
			zoom(amt);
		}
		// Zoom out
		if (Gdx.input.isKeyPressed(Keys.G)) {
			zoom(-amt);
		}

		// Orbit around target left
		if (Gdx.input.isKeyPressed(Keys.N)) {
			orbit(relative_rotation_angle += amt / 100, targetVec);
		}
		// Orbit around target right
		if (Gdx.input.isKeyPressed(Keys.M)) {
			orbit(relative_rotation_angle -= amt / 100, targetVec);
		}

		// Reset scene / reload starting values
		if (Gdx.input.isKeyPressed(Keys.R)) {
			init();
		}

		if (Gdx.input.isKeyPressed(Keys.I)) {
			printInfo();
		}

		update();
	}

	private void goRight() {
		Vector3 right = new Vector3(direction);
		right.crs(up);
		translate(right.x, 0, right.z);
	}

	private void goLeft() {
		Vector3 right = new Vector3(direction);
		right.crs(up);
		translate(-right.x, 0, -right.z);
	}

	private void printInfo() {
		Log.out("Key Info (I): \n ESC = quit demo");
		Log.out("A, D = move cam left / right");
		Log.out("W, S = move cam forward / backward");
		Log.out("Q, E = yaw (turn) cam left / right");
		Log.out("U, J = pitch cam up / down");
		Log.out("H, K = roll cam counter-clockwise / clockwise");
		Log.out("N, M = orbit cam around origin (TODO: player) counter-clockwise / clockwise");
		Log.out("C, SPACE = print cam / player position");
		Log.hr();
		Log.out("cam_pos:  " + position);
		Log.out("cam_up:   " + up);
		Log.out("cam_dir:  " + direction);
		Log.out("targetVec:  " + targetVec);
		Log.hr();
	}

	public void orbit(float amt_rotate, Vector3 target) {
		float altitude = 10f;
		float radius = 10f;

		float x = (float) Math.sin((position.x + amt_rotate * 360) * DEG)
				* radius;
		float z = (float) Math.cos((position.z + amt_rotate * 360) * DEG)
				* radius;
		position.set(x, altitude, z);
		updateLookAt();
	}

	public void zoom(float amt_zoom) {
		// TODO WIP
		update();
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		last.set(-1, -1, -1);
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// if (Gdx.input.isKeyPressed(Keys.W)) {
		// translate(direction.x, 0, direction.z);
		// }
		// if (Gdx.input.isKeyPressed(Keys.S)) {
		// translate(-direction.x, 0, -direction.z);
		// }
		//
		// if (Gdx.input.isKeyPressed(Keys.A)) {
		// Vector3 right = new Vector3(direction);
		// right.crs(up);
		// translate(-right.x, 0, -right.z);
		// }
		// if (Gdx.input.isKeyPressed(Keys.D)) {
		// Vector3 right = new Vector3(direction);
		// right.crs(up);
		// translate(right.x, 0, right.z);
		// }
		// TODO: WIP, this does not work this way...
		Ray pickRay = this.getPickRay(x, y);
		Intersector.intersectRayPlane(pickRay, xzPlane, curr);

		if (!(last.x == -1 && last.y == -1 && last.z == -1)) {
			pickRay = this.getPickRay(last.x, last.y);
			Intersector.intersectRayPlane(pickRay, xzPlane, delta);
			delta.sub(curr);
			this.position.add(delta.x, 0, delta.z);
		}
		last.set(x, y, 0);

		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	public void spin(float dir, float delta) {
		if (dir > 0) {
			goLeft();
		} else {
			goRight();
		}
		// Log.out("spin oCam");
	}

	@Override
	public void spin(float delta, Vector3 dir) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean mouseMoved(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
