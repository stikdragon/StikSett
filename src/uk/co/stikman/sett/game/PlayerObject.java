package uk.co.stikman.sett.game;

import java.io.IOException;

import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettInputStream;

public abstract class PlayerObject implements GameObject, HasOwner, IsNodeObject {
	private transient final Game	game;
	private int						id;
	private Player					owner;

	public PlayerObject(Game game) {
		this(game, null, -1);
	}

	public PlayerObject(Game game, Player owner, int id) {
		super();
		this.id = id;
		this.owner = owner;
		this.game = game;
	}

	@Override
	public Player getOwner() {
		return owner;
	}

	@Override
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Game getGame() {
		return game;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + Integer.toString(id);
	}

	@Override
	public void toStream(SettOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeObject(owner);
	}

	@Override
	public void fromStream(SettInputStream str) throws IOException {
		id = str.readInt();
		owner = str.readObject(Player.class);
	}

}
