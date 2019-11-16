package entities;

import java.io.Serializable;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import renderEngine.DisplayManager;
import terrains.Terrain;

public class Tank extends Entity {

	private static final float RUN_SPEED = 20;
	private static final float TURN_SPEED = 40;
	private static final float SHOOT_DELAY = 1;
	public static float GRAVITY = -50;
	private float upwardsSpeed = 0;

	private static final float TERRAIN_HEIGHT = 0;
	private static final float Y_OFFSET = 1.5f;

	public static final float TANK_X_SIZE = 2;
	public static final float TANK_Z_SIZE = 3;
	public static final float TANK_Y_SIZE = 2;
			
	private float currentSpeed = 0;
	private float currentTurnSpeed = 0;
	private boolean IsShootAvailable = true;
	private float lastShootTime = 0;
	private int hp = 100;
	private boolean isDead = false;
	private boolean isFired = false;
	private String id;

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

	public Tank(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, String id,
			int hp) {
		super(model, position, rotX, rotY, rotZ, scale);
		setHp(hp);
		setId(id);
	}

	public void move(Terrain terrain) {
		checkInputs();
		super.increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		float distance = currentSpeed * DisplayManager.getFrameTimeSeconds();
		float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
		super.increasePosition(dx, 0, dz);
		upwardsSpeed += GRAVITY * DisplayManager.getFrameTimeSeconds();
		super.increasePosition(0, upwardsSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		float terrainHeight = terrain.getHeightOfTerrain(super.getPosition().x, super.getPosition().z);
		if (super.getPosition().y < terrainHeight + Y_OFFSET) {
			upwardsSpeed = 0;
			super.getPosition().y = terrainHeight + Y_OFFSET;
		}

		if (super.getPosition().x >= terrain.getTerrainSize()) {
			super.getPosition().x = terrain.getTerrainSize();
		}
		if (super.getPosition().x <= 0) {
			super.getPosition().x = 0;
		}
		if (super.getPosition().z <= 0) {
			super.getPosition().z = 0;
		}
		if (super.getPosition().z >= terrain.getTerrainSize()) {
			super.getPosition().z = terrain.getTerrainSize();
		}
	}

	private void checkInputs() {
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			this.currentSpeed = -RUN_SPEED;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			this.currentSpeed = RUN_SPEED;
		} else {
			this.currentSpeed = 0;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			this.currentTurnSpeed = -TURN_SPEED;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			this.currentTurnSpeed = TURN_SPEED;
		} else {
			this.currentTurnSpeed = 0;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			if (IsShootAvailable) {
				isFired = true;
				lastShootTime = Sys.getTime();
			}
		}
	}

	public void checkBulletTime() {
		if (Sys.getTime() - lastShootTime >= SHOOT_DELAY * 1000) {
			IsShootAvailable = true;
		} else {
			IsShootAvailable = false;
		}
	}

	public boolean getIsFired() {
		return this.isFired;
	}

	public void setIsFired(boolean isFired) {
		this.isFired = isFired;
	}
}
