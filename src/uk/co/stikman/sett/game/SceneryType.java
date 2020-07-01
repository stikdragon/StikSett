package uk.co.stikman.sett.game;

public class SceneryType {
	private final String		name;
	private VoxelModelParams	voxParms	= new VoxelModelParams();

	public SceneryType(String name) {
		super();
		this.name = name;
	}

	public void setModelName(String modelName) {
		voxParms.setName(modelName);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public void setRotation(char rotation) {
		voxParms.setRotation(rotation);
	}

	public VoxelModelParams getVoxelModelInfo() {
		return voxParms;
	}

}
