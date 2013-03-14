package com.ty.libgdxusers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

/**
 * 
 * 　 扩展了透视投影相机
 * 
 * @author tangyong
 * 
 */
public class GuPerspCam extends PerspectiveCamera implements InputProcessor,
		GuCamera {

	private static final float DEG = (float) (Math.PI / 180f);
	private static final int CAMSPEED = 40;
	private static Vector3 campos;
	private Vector3 dir;

	private static float FOV = 50f;
	private static final Vector3 CAM_POS_INITIAL = new Vector3(15f, 15f, 8f);
	private static final float CAM_NEAR_INITIAL = 0.1f;
	private static final float CAM_FAR_INITIAL = 200f;
	private static Vector3 CAM_LOOKAT_INITIAL;
	private static Vector3 CAM_UP_INITIAL = new Vector3(0.0f, 1.0f, 0.0f);
	private float relative_rotation_angle = 0;

	// viewsize should be related to the size of your scene
	public GuPerspCam(float VP_WIDTH, float VP_HEIGHT, float viewSize) {
		super(FOV, VP_WIDTH, VP_HEIGHT);
		Log.out("Cam-Up:        " + up.x + ", " + up.y + ", " + up.z);
		Log.out("Cam-Position:  " + position.x + ", " + position.y + ", "
				+ position.z);
		Log.out("Cam-Direction: " + direction.x + ", " + direction.y + ", "
				+ direction.z);

		relative_rotation_angle = 0;
		position.set(CAM_POS_INITIAL);
		up.set(CAM_UP_INITIAL);
		CAM_LOOKAT_INITIAL = new Vector3(VP_WIDTH / 2, 0, VP_HEIGHT / 2);

		// Log.out("Cam-Up: " + up.x + up.y + up.z);
		// direction.set(-1, -1, -1);
		lookAt(CAM_LOOKAT_INITIAL.x, CAM_LOOKAT_INITIAL.y, CAM_LOOKAT_INITIAL.z);
	}

	public void push() {
		campos = position;
		dir = this.direction;
	}

	public void pop() {
		position.set(campos);
		this.direction.set(dir);
	}

	@Override
	public void update() {
		super.update();
	}

	public void handleKeys() {
		float amt = CAMSPEED * Gdx.graphics.getDeltaTime();

		if (Gdx.input.isKeyPressed(Keys.W)) {
			// moves camera along z axis (world coordinates) into screen
			// translate(0, 0, -amt);

			// moves camera along its direction vector
			// Vector3 newCamPos = position.cpy();
			// Vector3 newCamDir = direction.cpy().nor();

			// newCamPos.add(newCamDir);
			// Log.out("new cam pos: " + newCamPos + "(length = " +
			// direction.len() + ")");
			// translate(newCamPos.x, position.y, newCamPos.z);
			translate(direction.x, 0, direction.z);
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
		if (Gdx.input.isKeyPressed(Keys.S)) {
			// moves camera along z axis (world coordinates) out of screen
			// translate(0, 0, amt);

			// moves camera along its reversed direction vector
			translate(-direction.x, 0, -direction.z);
		}

		if (Gdx.input.isKeyPressed(Keys.A)) {
			translate(-amt, 0, 0);
		}
		if (Gdx.input.isKeyPressed(Keys.D)) {
			translate(amt, 0, 0);
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
			// Log.out("Skalarprodukt: " + position.dot(new Vector3(1,0,0)));
			orbit(relative_rotation_angle += amt / 100, new Vector3(0, 0, 0));
		}
		// Orbit around target right
		if (Gdx.input.isKeyPressed(Keys.M)) {
			orbit(relative_rotation_angle -= amt / 100, new Vector3(0, 0, 0));
		}

		// Reset scene / reload starting values
		if (Gdx.input.isKeyPressed(Keys.R)) {
			init();
		}
		update();
	}

	public void init() {
		// TODO NOT WORKING YET :-(
		relative_rotation_angle = 0;
		position.set(CAM_POS_INITIAL);
		// position.set(0, 0, 0);

		// up.set(CAM_UP_INITIAL);
		up.set(0, 1.0f, 0);

		lookAt(CAM_LOOKAT_INITIAL.x, CAM_LOOKAT_INITIAL.y, CAM_LOOKAT_INITIAL.z);

		// direction.set(5, 0, 5);
		// direction.set(0, 0, -1.0f);

		update();
		Log.out("Reset Camera:");
		Log.out("Cam-Up:        " + up.x + ", " + up.y + ", " + up.z);
		Log.out("Cam-Position:  " + position.x + ", " + position.y + ", "
				+ position.z);
		Log.out("Cam-Direction: " + direction.x + ", " + direction.y + ", "
				+ direction.z);
		Log.out("Cam-LookAt: " + CAM_LOOKAT_INITIAL.x + ", "
				+ CAM_LOOKAT_INITIAL.y + ", " + CAM_LOOKAT_INITIAL.z);
	}

	public void orbit(float amt_rotate, Vector3 target) {
		float altitude = 10f;
		float radius = 10f;

		// TODO WIP
		// TODO: target set to origin - can't get player pos yet
		/**
		 * Rotate camera around a target, cam looks always at target
		 */
		float x = (float) Math.sin((position.x + amt_rotate * 360) * DEG)
				* radius;
		float z = (float) Math.cos((position.z + amt_rotate * 360) * DEG)
				* radius;
		Log.out("amt value: " + amt_rotate);
		Log.out("orbit values: " + x + ", " + z);
		Log.out("position: " + position.x + ", " + position.y + ", "
				+ position.y);
		position.set(x, altitude, z);
		lookAt(target.x, target.y, target.z);
		update();
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void spin(float delta, float dir) {
		// TODO Auto-generated method stub
		Log.out("spin pCam");
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
