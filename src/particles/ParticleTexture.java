package particles;

public class ParticleTexture {
	private int textureID;
	private int numOfRows;

	protected int getTextureID() {
		return textureID;
	}

	protected int getNumOfRows() {
		return numOfRows;
	}

	public ParticleTexture(int textureID, int numOfRows) {
		this.textureID = textureID;
		this.numOfRows = numOfRows;
	}

}
