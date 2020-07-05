package uk.co.stikman.sett.game;

public class Rock implements IsNodeObject {

	private final SceneryType type;

	public Rock(SceneryType type) {
		this.type = type;
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


	@Override
	public String getModelName() {
		return type.getModelName();
	}


}
