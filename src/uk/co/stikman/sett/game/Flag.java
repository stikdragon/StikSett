package uk.co.stikman.sett.game;

import java.util.ArrayList;
import java.util.List;

import uk.co.stikman.sett.BaseGame;

public class Flag implements HasId, IsNodeObject {
	private final int			id;
	private List<HasFlag>		connections	= new ArrayList<>();
	private BaseGame			game;

	public Flag(BaseGame owner) {
		this(owner, owner.nextId());
	}

	public Flag(BaseGame owner, int id) {
		super();
		this.id = id;
		this.game = owner;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Flag: " + Integer.toString(id);
	}

	public List<HasFlag> getConnections() {
		return connections;
	}


	@Override
	public ObstructionType getObstructionType() {
		return ObstructionType.ALL;
	}

	@Override
	public String getModelName() {
		return "flag";
	}

}
