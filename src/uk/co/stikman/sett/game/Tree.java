package uk.co.stikman.sett.game;

import java.io.IOException;

import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettInputStream;

public class Tree implements GameObject, IsNodeObject {

	private final Game	game;
	private SceneryType	type;
	private int			id;

	public Tree(Game game,int id, SceneryType type) {
		this.game = game;
		this.type = type;
		this.id = id;
	}

	public Tree(Game game) {
		this.game = game;
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
	public int getId() {
		return id;
	}

	@Override
	public void toStream(SettOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeString(type.getName());
	}

	@Override
	public void fromStream(SettInputStream str) throws IOException {
		this.id = str.readInt();
		this.type = game.getScenaryDef(str.readString());
	}

}
