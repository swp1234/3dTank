package entities;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

	private float distanceFromPlayer = 40;
	private float angleAroundPlayer = -180;
	private final float MAX_DISTANCE = 55; 
	private final float MIN_DISTANCE = 10;
	
	
	private Vector3f position = new Vector3f(0, 0, 0);
	private float pitch = 20;
	private float yaw;
	private float roll;
	private Tank tank;

	public Camera(Tank tank) {
		this.tank = tank;
	}

	public void move() {
		calculateZoom();
		calculatePitch();
		calculateAngleAroundPlayer();
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
		this.yaw = 180 - (tank.getRotY() + angleAroundPlayer);
	}

	private void calculateCameraPosition(float horizontalDistance, float verticalDistance) {
		float theta = tank.getRotY() + angleAroundPlayer;
		float offsetX = (float) (horizontalDistance * Math.sin(Math.toRadians(theta)));
		float offsetZ = (float) (horizontalDistance * Math.cos(Math.toRadians(theta)));
		position.x = tank.getPosition().x - offsetX;
		position.z = tank.getPosition().z - offsetZ;
		position.y = tank.getPosition().y + verticalDistance;

	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}

	private void calculateZoom() {
		float zoomLevel = Mouse.getDWheel() * 0.1f;
		if (distanceFromPlayer - zoomLevel <= MIN_DISTANCE) {
			distanceFromPlayer = MIN_DISTANCE;
		} else if (distanceFromPlayer - zoomLevel >= MAX_DISTANCE) {
			distanceFromPlayer = MAX_DISTANCE;
		} else {
			distanceFromPlayer -= zoomLevel;
		}
	}

	private void calculatePitch() {
		if (Mouse.isButtonDown(0)) {
			float pitchChange = Mouse.getDY() * 0.1f;
			pitch -= pitchChange;
		}
	}

	private void calculateAngleAroundPlayer() {
		if (Mouse.isButtonDown(1)) {
			float angleChange = Mouse.getDX() * 0.3f;
			angleAroundPlayer -= angleChange;
		}
	}

	private float calculateHorizontalDistance() {
		return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
	}

	private float calculateVerticalDistance() {
		return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
	}

}
