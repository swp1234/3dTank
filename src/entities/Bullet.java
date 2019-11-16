package entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import renderEngine.DisplayManager;
import terrains.Terrain;

public class Bullet extends Entity {

	public Bullet(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, String id) {
		super(model, position, rotX, rotY, rotZ, scale);
		this.id = new String(id);
	}

	public static final float OFFSET_X = 0;
	public static final float OFFSET_Y = 2;
	public static final float OFFSET_Z = 0;
	private static final float SPEED = -230;
	private static String id = "";
	private static float GRAVITY = -65;
	private float upwardsSpeed = 0;
	private boolean isAttacked = false;
	private boolean isParticleSpawned = false;

	public boolean getIsParticleSpawned() {
		return isParticleSpawned;
	}

	public void setParticleSpawned(boolean isParticleSpawned) {
		this.isParticleSpawned = isParticleSpawned;
	}

	public boolean getIsAttacked() {
		return isAttacked;
	}

	public void setAttacked(boolean isAttacked) {
		this.isAttacked = isAttacked;
	}

	private int damage = 50;

	public void move() {
		float distance = SPEED * DisplayManager.getFrameTimeSeconds();
		float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
		super.increasePosition(dx, 0, dz);
		upwardsSpeed += GRAVITY * DisplayManager.getFrameTimeSeconds();
		super.increasePosition(0, upwardsSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		setIsDamageable();
	}

	public void setIsDamageable() {
		if(this.getPosition().y<=-Terrain.MAX_HEIGHT) {
			isAttacked = true;
		}
	}

	public boolean isDamageable() {
		return !this.isAttacked;
	}

	public Tank inflictDamageonTank(HashMap<String, Tank> tanks) {
		Tank attackedTank = null;
		for (String id : tanks.keySet()) {
			Tank tank = tanks.get(id);
			if (!tank.getId().equals(this.id)) {
				float x = this.getPosition().x;
				float z = this.getPosition().z;
				float y = this.getPosition().y;
				if (x <= tank.getPosition().x + Tank.TANK_X_SIZE && x >= tank.getPosition().x - Tank.TANK_X_SIZE
						&& z >= tank.getPosition().z - Tank.TANK_Z_SIZE && z <= tank.getPosition().z + Tank.TANK_Z_SIZE
						&& y >= tank.getPosition().y - Tank.TANK_Y_SIZE
						&& y <= tank.getPosition().y + Tank.TANK_Y_SIZE) {
					int hp = tank.getHp();
					tank.setHp(hp - damage);
					this.isAttacked = true;
					attackedTank = tank;
					break;
				}
			}
		}
		return attackedTank;
	}

	public String getId() {
		return this.id;
	}

}
