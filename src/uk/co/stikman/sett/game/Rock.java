package uk.co.stikman.sett.game;

import java.io.IOException;

public class Rock implements GameObject, IsNodeObject {

	private final SceneryType	type;
	private int					id;

	public Rock(int id, SceneryType type) {
		this.type = type;
		this.id = id;
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

	@Override
	public void toStream(SettOutputStream str) throws IOException {
		str.writeInt(id);
		str.writeString(type.getName());
	}

	@Override
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
