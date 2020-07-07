package uk.co.stikman.sett.game;

import java.io.IOException;

import uk.co.stikman.sett.BaseGame;
import uk.co.stikman.sett.SettInputStream;

public abstract class PlayerObject implements GameObject, HasOwner, IsNodeObject {
	private transient final BaseGame	game;
	private int							id;
	private Player						owner;

	public PlayerObject(BaseGame game) {
		this(game, null, -1);
	}

	public PlayerObject(BaseGame game, Player owner, int id) {
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

	public BaseGame getGame() {
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
