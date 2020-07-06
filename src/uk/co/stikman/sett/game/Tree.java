package uk.co.stikman.sett.game;

import java.io.IOException;

public class Tree implements GameObject, IsNodeObject {

	private final SceneryType	type;
	private final int			id;

	public Tree(int id, SceneryType type) {
		this.type = type;
		this.id = id;
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

	@Override
	public void toStream(SettOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeString(type.getName());
	}

	@Override
	public int getId() {
		return id;
	}

}
