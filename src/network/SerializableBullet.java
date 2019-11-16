package network;

import java.io.Serializable;

import org.lwjgl.util.vector.Vector3f;

import entities.Bullet;

public class SerializableBullet implements Serializable {

	private static final long serialVersionUID = 2L;
	private float x, y, z;
	private float rotX, rotY, rotZ;
	private float scale;
	private String id;
	private boolean isAttacked = false;
	private boolean isParticleSpawned = false;

	public SerializableBullet(Bullet bullet) {
		this.x = bullet.getPosition().x;
		this.y = bullet.getPosition().y;
		this.z = bullet.getPosition().z;
		this.rotX = bullet.getRotX();
		this.rotY = bullet.getRotY();
		this.rotZ = bullet.getRotZ();
		this.scale = bullet.getScale();
		this.id = new String(bullet.getId());
		this.isAttacked = bullet.getIsAttacked();
		this.isParticleSpawned = bullet.getIsParticleSpawned();
	}

	public boolean getIsParticleSpawned() {
		return isParticleSpawned;
	}

	public void setParticleSpawned(boolean isParticleSpawned) {
		this.isParticleSpawned = isParticleSpawned;
	}

	public Vector3f getPosition() {
		return new Vector3f(x, y, z);
	}

	public float getRotX() {
		return rotX;
	}

	public void setRotX(float rotX) {
		this.rotX = rotX;
	}

	public float getRotY() {
		return rotY;
	}

	public void setRotY(float rotY) {
		this.rotY = rotY;
	}

	public float getRotZ() {
		return rotZ;
	}

	public void setRotZ(float rotZ) {
		this.rotZ = rotZ;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public boolean getIsAttacked() {
		return isAttacked;
	}

	public void setAttacked(boolean isAttacked) {
		this.isAttacked = isAttacked;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getId() {
		return this.id;
	}
}
