package uk.co.stikman.sett.game;

public class Tree implements IsNodeObject {

	private final SceneryType type;

	public Tree(SceneryType type) {
		this.type = type;
	}

	public SceneryType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Tree: " + type;
	}

	@Override
	public ObstructionType getObstructionType() {
		return ObstructionType.BUILDINGS;
	}

	@Override
	public String getModelName() {
		return type.getModelName();
	}

}
