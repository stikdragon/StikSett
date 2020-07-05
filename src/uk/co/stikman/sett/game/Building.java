package uk.co.stikman.sett.game;

public class Building implements HasId, HasFlag, IsNodeObject {

	private final int			id;
	private final BuildingType	type;
	private Flag				flag;

	public Building(int id, BuildingType type) {
		super();
		this.id = id;
		this.type = type;
	}

	@Override
	public int getId() {
		return id;
	}

	public BuildingType getType() {
		return type;
	}

	@Override
	public String toString() {
		return type + ": " + id;
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
