package uk.co.stikman.sett.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettInputStream;

public class Flag extends PlayerObject {

	private transient List<HasFlag> connections = new ArrayList<>();

	public Flag(Game game) {
		super(game);
	}

	public Flag(Game game, Player owner) {
		super(game, owner, -1);
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
		out.writeInt(connections.size());
		for (HasFlag x : connections)
			out.writeObject(x);
	}

	@Override
	public void fromStream(SettInputStream str) throws IOException {
		super.fromStream(str);
		int cnt = str.readInt();
		while (cnt-- > 0)
			connections.add(str.readObject(HasFlag.class));
	}

}
