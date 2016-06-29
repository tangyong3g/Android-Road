package com.ty.libgdxusers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
//import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderOld;
import com.badlogic.gdx.math.Vector3;

public class MeshHelper {

	public Mesh mesh;
	public Vector3 size;
	public float scale;
	public Vector3 pos = new Vector3(0, 0, 0);
	public Vector3 bbox = new Vector3(10f, 10f, 10f); // default area
	public Vector3 color = new Vector3(1, 1, 1);
	public Vector3 moveBy = new Vector3(0, 0, 0);

	public boolean shadow = true;

	public MeshHelper() {
		// overloaded constructor
	}

	public MeshHelper(String fpath) {
		load(fpath);
	}

	protected MeshHelper load(String fpath) {
		//还不知道在9.8里面哪里东西来　升级原来　的modelLoaderOld
//		mesh = ModelLoaderOld.loadObj(Gdx.files.internal(fpath).read());
		return this;
	}

	public MeshHelper scale(float sc) {
		scale = sc;
		mesh.scale(sc, sc, sc);
		return this;
	}

	public MeshHelper scale(float sx, float sy, float sz) {
		mesh.scale(sx, sy, sz);
		return this;
	}

	public void render(GL10 gl, int renderType) {
		// Log.out("render at:", pos);
		gl.glPushMatrix();
		gl.glTranslatef(pos.x, pos.y, pos.z);
		mesh.render(renderType);
		gl.glPopMatrix();
	}

	public void renderWireframe(GL10 gl) {
		gl.glPushMatrix();
		gl.glColor4f(color.x, color.y, color.z, 1);
		gl.glTranslatef(pos.x, pos.y, pos.z);
		mesh.render(GL10.GL_LINE_STRIP);
		gl.glPopMatrix();
	}

	// public BoundingBox getSize() {
	// // bbox = mesh.calculateBoundingBox();
	// Log.out("bbox:" + bbox);
	// return bbox;
	// }

	public Vector3 getPos() {
		return pos;
	}

	public MeshHelper setPos(Vector3 pos) {
		this.pos = pos;
		return this;
	}

	public MeshHelper setPos(float x, float y, float z) {
		setPos(new Vector3(x, y, z));
		return this;
	}

	// for line drawing
	public MeshHelper setColor(float r, float g, float b) {
		color = new Vector3(r, g, b);
		return this;
	}

	public void rotateX(GL10 gl, float angle) {
		// TODO: does not work work like this!
		gl.glPushMatrix();
		gl.glColor4f(color.x, color.y, color.z, 1);
		gl.glRotatef(angle, 1, 0, 0);
		mesh.render(GL10.GL_LINE_STRIP);
		gl.glPopMatrix();
	}

	public MeshHelper setMotion(float x, float y, float z, float speed) {
		moveBy = new Vector3(x * speed, y * speed, z * speed);
		return this;
	}

	public void update(float delta) {
		pos.add(moveBy);
		if (pos.x < 0 || pos.x > bbox.x) {
			moveBy.x = -moveBy.x;
		}
		if (pos.z < 0 || pos.z > bbox.z) {
			moveBy.z = -moveBy.z;
		}
	}

	public void setTexture() {
		// Texture texture = new Texture(Gdx.files.internal("data/image.png"),
		// true);
		// texture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
		// Material material = new Material("Material1", new
		// TextureAttribute(texture, 0, "part"));
		// mesh.material = material;
		// Texture texture = Gdx.graphics.newTexture( Gdx.files.getFileHandle(
		// "data/badlogic.jpg", FileType.Internal), TextureFilter.MipMap,
		// TextureFilter.Linear, TextureWrap.ClampToEdge,
		// TextureWrap.ClampToEdge );
	}

}
