package uk.co.stikman.sett.game;

import java.io.IOException;

import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettInputStream;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector3;

public class Noddy implements GameObject {

	private final Game	game;
	private int			id;
	private Player		owner;
	private NoddyType	type;
	private Vector3		position	= new Vector3();

	public Noddy(Game game) {
		this.game = game;
	}

	public Noddy(Game game, int id) {
		this.game = game;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}

	public void toStream(SettOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeString(type.getName());
		out.writeVec3(position);
		out.writeObject(owner);
	}

	public void fromStream(SettInputStream str) throws IOException {
		id = str.readInt();
		type = getGame().getNoddyDef(str.readString());
		str.readVec3(position);
		owner = str.readObject(Player.class);
	}

	public NoddyType getType() {
		return type;
	}

	public void setType(NoddyType type) {
		this.type = type;
	}

	public Game getGame() {
		return game;
	}

	public Vector3 getPosition() {
		return position;
	}

	public void setPosition(Vector3 position) {
		this.position.copy(position);
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public void setPosition(int x, int y, int z) {
		position.set(x, y, z);
	}

}
