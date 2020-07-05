package uk.co.stikman.sett.game;

import uk.co.stikman.sett.BaseGame;

public abstract class PlayerObject implements HasId, HasOwner, IsNodeObject {
	private BaseGame	game;
	private int			id;
	private Player		owner;

	public PlayerObject(BaseGame game, Player owner) {
		this(game, owner, game.nextId());
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

	public BaseGame getGame() {
		return game;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + Integer.toString(id);
	}

}
