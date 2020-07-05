package uk.co.stikman.sett.game;

import uk.co.stikman.sett.BaseGame;

public class Building extends PlayerObject implements HasFlag {

	private final BuildingType	type;
	private Flag				flag;

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

}
