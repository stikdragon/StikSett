package uk.co.stikman.sett.gameevents;

import java.io.IOException;

import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettInputStream;
import uk.co.stikman.sett.game.Building;
import uk.co.stikman.sett.game.SettOutputStream;
import uk.co.stikman.utils.math.Vector2i;

public class BuildingOrderedEvent extends GameEvent {

	private String		playerName;
	private int			id;
	private String		type;
	private Vector2i	position;
	private float		stateConstructed;
	private float		stateLevelled;

	public BuildingOrderedEvent(Building b) {
		super();

		id = b.getId();
		type = b.getType().getName();
		position = b.getPosition();
		stateConstructed = b.getStateConstructed();
		stateLevelled = b.getStateLevelled();
		playerName = b.getOwner().getName();

	}

	public BuildingOrderedEvent() {
	}

	@Override
	public void toStream(SettOutputStream str) throws IOException {
		super.toStream(str);
		str.writeInt(id);
		str.writeString(type);
		str.writeVec2i(position);
		str.writeFloat(stateConstructed);
		str.writeFloat(stateLevelled);
		str.writeString(playerName);
	}

	@Override
	public void fromStream(SettInputStream str) throws IOException {
		super.fromStream(str);
		id = str.readInt();
		type = str.readString();
		position = str.readVec2i(new Vector2i());
		stateConstructed = str.readFloat();
		stateLevelled = str.readFloat();
		playerName = str.readString();
	}

	@Override
	public void applyTo(Game game) {
		Building b = new Building(game, game.getPlayer(playerName), id, game.getBuildingDef(type));
		b.setPosition(position.x, position.y);
		b.setStateConstructed(stateConstructed);
		b.setStateLevelled(stateLevelled);
		game.getWorld().getBuildings().put(b);
		game.getWorld().getTerrain().get(b.getPosition()).setObject(b);
	}

}
