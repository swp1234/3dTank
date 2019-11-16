package network;

import java.io.Serializable;

import org.lwjgl.util.vector.Vector3f;

import entities.Tank;

public class SerializableTank implements Serializable {

	private static final long serialVersionUID = 1L;
	private int hp = 100;
	private boolean isDead = false;
	private float x, y, z;
	private float rotX, rotY, rotZ;
	private float scale;
	private String id;

	public SerializableTank(Tank tank) {
		this.hp = tank.getHp();
		this.isDead = tank.isDead();
		this.x = tank.getPosition().x;
		this.y = tank.getPosition().y;
		this.z = tank.getPosition().z;
		this.rotX = tank.getRotX();
		this.rotY = tank.getRotY();
		this.rotZ = tank.getRotZ();
		this.scale = tank.getScale();
		this.id = new String(tank.getId());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

	public Vector3f getPosition() {
		return new Vector3f(x, y, z);
	}

	public void setPosition(Vector3f position) {
		this.x = position.x;
		this.y = position.y;
		this.z = position.z;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
