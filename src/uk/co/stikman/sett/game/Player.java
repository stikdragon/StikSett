package uk.co.stikman.sett.game;

import java.io.IOException;

import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettInputStream;
import uk.co.stikman.utils.math.Vector3;

public class Player implements IsSerializable {
	private int				id;
	private String			name;
	private Vector3			colour	= new Vector3(0, 0, 0.8f);
	private final Game	game;

	public Player(Game game) {
		this.game = game;
	}
	
	public Player(Game game, int id, String name, Vector3 colour) {
		this.game = game;
		this.name = name;
		this.id = id;
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
		return "Player [id=" + id + ", name=" + name + ", colour=" + colour + ", game=" + game + "]";
	}

	public Game getGame() {
		return game;
	}

	public int getId() {
		return id;
	}

	@Override
	public void toStream(SettOutputStream str) throws IOException {
		str.writeInt(id);
		str.writeString(name);
		str.writeVec3(colour);
	}

	@Override
	public void fromStream(SettInputStream str) throws IOException {
		id = str.readInt();
		name = str.readString();
		str.readVec3(colour);
	}

}
