package uk.co.stikman.sett.game;

import java.io.IOException;

import uk.co.stikman.sett.BaseGame;
import uk.co.stikman.sett.SettInputStream;

public class Building extends PlayerObject implements HasFlag {

	private BuildingType	type;
	private Flag			flag;

	public Building(BaseGame game) {
		super(game);
	}

	public Building(BaseGame game, Player owner, int id, BuildingType type) {
		super(game, owner, id);
		this.type = type;
	}

	public BuildingType getType() {
		return type;
	}

	@Override
	public String toString() {
		return type + ": " + getId();
	}

	public Flag getFlag() {
		return flag;
	}

	public void setFlag(Flag flag) {
		this.flag = flag;
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
	public void toStream(SettOutputStream out) throws IOException {
		super.toStream(out);
		out.writeString(type.getName());
		out.writeObject(flag);
	}

	@Override
	public void fromStream(SettInputStream str) throws IOException {
		super.fromStream(str);
		type = getGame().getWorld().getBuildingDef(str.readString());
		flag = str.readObject(Flag.class);
	}

}
