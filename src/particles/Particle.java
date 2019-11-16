package particles;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Tank;
import renderEngine.DisplayManager;

public class Particle {
	private Vector3f position;
	private Vector3f velocity;
	private float gravityEffect;
	private float lifeLength;

	private float rotation;
	private float scale;

	private ParticleTexture texture;
	private float elapsedTime = 0;

	public Particle(ParticleTexture texture, Vector3f position, Vector3f velocity, float gravityEffect,
			float lifeLength, float rotation, float scale) {
		this.texture = texture;
		this.position = position;
		this.velocity = velocity;
		this.gravityEffect = gravityEffect;
		this.lifeLength = lifeLength;
		this.rotation = rotation;
		this.scale = scale;
		ParticleMaster.addParticle(this);
	}

	public ParticleTexture getTexture() {
		return texture;
	}

	public void setTexture(ParticleTexture texture) {
		this.texture = texture;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public Vector3f getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector3f velocity) {
		this.velocity = velocity;
	}

	public float getGravityEffect() {
		return gravityEffect;
	}

	public void setGravityEffect(float gravityEffect) {
		this.gravityEffect = gravityEffect;
	}

	public float getLifeLength() {
		return lifeLength;
	}

	public void setLifeLength(float lifeLength) {
		this.lifeLength = lifeLength;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public float getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(float elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	protected boolean update(Camera camera) {
		velocity.y += Tank.GRAVITY * gravityEffect * DisplayManager.getFrameTimeSeconds();
		Vector3f change = new Vector3f(velocity);
		change.scale(DisplayManager.getFrameTimeSeconds());
		Vector3f.add(change, position, position);
		elapsedTime += DisplayManager.getFrameTimeSeconds();
		return elapsedTime < lifeLength;
	}

}
