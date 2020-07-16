package uk.co.stikman.sett.game;

import java.io.IOException;

import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettInputStream;
import uk.co.stikman.utils.math.Vector2i;

public class Building extends PlayerObject implements HasFlag {

	private BuildingType	type;
	private Flag			flag;
	private float			stateLevelled		= 0.0f;
	private float			stateConstructed	= 0.0f;
	private Vector2i		position			= new Vector2i();

	public Building(Game game) {
		super(game);
	}

	public Building(Game game, Player owner, int id, BuildingType type) {
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
		out.writeFloat(stateConstructed);
		out.writeFloat(stateLevelled);
		out.writeVec2i(position);
	}

	@Override
	public void fromStream(SettInputStream str) throws IOException {
		super.fromStream(str);
		type = getGame().getBuildingDef(str.readString());
		flag = str.readObject(Flag.class);
		stateConstructed = str.readFloat();
		stateLevelled = str.readFloat();
		str.readVec2i(position);
	}

	public float getStateLevelled() {
		return stateLevelled;
	}

	public void setStateLevelled(float stateLevelled) {
		this.stateLevelled = stateLevelled;
	}

	public float getStateConstructed() {
		return stateConstructed;
	}

	public void setStateConstructed(float stateConstructed) {
		this.stateConstructed = stateConstructed;
	}

	public void setPosition(int x, int y) {
		this.position.x = x;
		this.position.y = y;
	}

	public Vector2i getPosition() {
		return position;
	}

}
