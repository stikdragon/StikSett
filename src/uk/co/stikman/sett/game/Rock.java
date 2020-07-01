package uk.co.stikman.sett.game;

public class Rock implements IsNodeObject {

	private final SceneryType type;

	public Rock(SceneryType type) {
		this.type = type;
	}

	@Override
	public VoxelModelParams getVoxelModelInfo() {
		return type.getVoxelModelInfo();
	}
	
	public SceneryType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Rock: " + type;
	}

	@Override
	public ObstructionType getObstructionType() {
		return ObstructionType.ALL;
	}


}
