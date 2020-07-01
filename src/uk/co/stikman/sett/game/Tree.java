package uk.co.stikman.sett.game;

public class Tree implements IsNodeObject {

	private final SceneryType type;

	public Tree(SceneryType type) {
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
		return "Tree: " + type;
	}


}
