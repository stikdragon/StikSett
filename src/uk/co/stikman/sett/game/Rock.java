package uk.co.stikman.sett.game;

import java.io.IOException;

import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettInputStream;

public class Rock implements GameObject, IsNodeObject {

	private SceneryType	type;
	private int			id;
	private Game	game;

	public Rock(Game game) {
		this.game = game;
	}

	public Rock(Game game, int id, SceneryType type) {
		this.game = game;
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
	public void fromStream(SettInputStream str) throws IOException {
		id = str.readInt();
		type = game.getWorld().getScenaryDef(str.readString());
	}

	@Override
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
