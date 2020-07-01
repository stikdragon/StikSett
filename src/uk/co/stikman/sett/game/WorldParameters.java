package uk.co.stikman.sett.game;


public class WorldParameters {
	private int size;

	public WorldParameters() {
		super();
	}

	public WorldParameters(int size) {
		setSize(size);
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		if (size < 1 || size > 96)
			throw new IllegalArgumentException("World Size must be between 1 and 96");
		this.size = size;
	}

}
