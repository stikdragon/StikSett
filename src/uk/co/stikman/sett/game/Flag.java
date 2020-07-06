package uk.co.stikman.sett.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.stikman.sett.BaseGame;

public class Flag extends PlayerObject {

	private transient List<HasFlag>	connections			= new ArrayList<>();

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

	@Override
	public void toStream(SettOutputStream out) throws IOException {
		super.toStream(out);
		out.write(connections.size());
		for (HasFlag x : connections)
			out.writeObject(x);
	}

}
