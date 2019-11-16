package particles;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Tank;
import renderEngine.DisplayManager;

public class Particle {
	private Vector3f position;
	private Vector3f velocity;
	private float gravityEffect;
	private float lifeLength;

	private float rotation;
	private float scale;

	private Vector2f textOffset1 = new Vector2f();
	private Vector2f textOffset2 = new Vector2f();
	private float blend;

	public Vector2f getTextOffset1() {
		return textOffset1;
	}

	public Vector2f getTextOffset2() {
		return textOffset2;
	}

	public float getBlend() {
		return blend;
	}

	public void setBlend(float blend) {
		this.blend = blend;
	}

	private ParticleTexture texture;

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

	private float elapsedTime = 0;

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

	protected boolean update() {
		velocity.y += Tank.GRAVITY * gravityEffect * DisplayManager.getFrameTimeSeconds();
		Vector3f change = new Vector3f(velocity);
		change.scale(DisplayManager.getFrameTimeSeconds());
		Vector3f.add(change, position, position);
		updateTextureCoordInfo();
		elapsedTime += DisplayManager.getFrameTimeSeconds();
		return elapsedTime < lifeLength;
	}

	private void updateTextureCoordInfo() {
		float lifeFactor = elapsedTime / lifeLength;
		int stageCount = texture.getNumberOfRows() * texture.getNumberOfRows();
		float atlasProgression = lifeFactor * stageCount;
		int index1 = (int) Math.floor(atlasProgression);
		int index2 = index1 < stageCount - 1 ? index1 + 1 : index1;
		this.blend = atlasProgression % 1;
		setTextureOffset(textOffset1, index1);
		setTextureOffset(textOffset2, index2);
	}

	private void setTextureOffset(Vector2f offset, int index) {
		int column = index % texture.getNumberOfRows();
		int row = index / texture.getNumberOfRows();
		offset.x = (float) column / texture.getNumberOfRows();
		offset.y = (float) row / texture.getNumberOfRows();
	}

}
