package uk.co.stikman.sett.game;

import uk.co.stikman.sett.BaseGame;
import uk.co.stikman.utils.math.Vector3;

public class Player {
	private String			name;
	private Vector3			colour	= new Vector3(0, 0, 0.8f);
	private final BaseGame	game;

	public Player(BaseGame game, String name, Vector3 colour) {
		this.game = game;
		this.name = name;
		this.colour = colour;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector3 getColour() {
		return colour;
	}

	public void setColour(Vector3 colour) {
		this.colour.copy(colour);
	}

	@Override
	public String toString() {
		return "Player [name=" + name + ", colour=" + colour + "]";
	}

	public BaseGame getGame() {
		return game;
	}
}
