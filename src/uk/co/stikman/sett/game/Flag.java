package uk.co.stikman.sett.game;

import java.util.ArrayList;
import java.util.List;

import uk.co.stikman.sett.BaseGame;

public class Flag extends PlayerObject  {

	private List<HasFlag>	connections	= new ArrayList<>();

	public Flag(BaseGame game, Player owner, int id) {
		super(game, owner, id);
	}

	public Flag(BaseGame game, Player owner) {
		super(game, owner);
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
