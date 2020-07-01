package uk.co.stikman.sett.game;

public class BuildingType {
	private final String		name;
	private String				display;
	private String				modelName;
	private String				description;
	private VoxelModelParams	voxParms	= new VoxelModelParams();

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getName() {
		return name;
	}

	public BuildingType(String name, String display) {
		super();
		this.name = name;
		this.display = display;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public VoxelModelParams getVoxelModelInfo() {
		return voxParms;
	}

	public void setVoxelModelInfo(VoxelModelParams voxParms) {
		this.voxParms = voxParms;
	}

}
