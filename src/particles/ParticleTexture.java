package particles;

public class ParticleTexture {
	private int textureID;
	private int numberOfRows;
	public ParticleTexture(int textureID, int numberOfRows) {
		this.textureID = textureID;
		this.numberOfRows = numberOfRows;
	}
	public int getTextureID() {
		return textureID;
	}
	public void setTextureID(int textureID) {
		this.textureID = textureID;
	}
	public int getNumberOfRows() {
		return numberOfRows;
	}
	public void setNumberOfRows(int numberOfRows) {
		this.numberOfRows = numberOfRows;
	}
	
}
