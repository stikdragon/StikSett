package uk.co.stikman.sett.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.stikman.sett.SettInputStream;
import uk.co.stikman.utils.math.Vector2i;

public class Road implements GameObject, HasFlag {

	private int				id;
	private Flag			flagA;
	private Flag			flagB;
	private List<Vector2i>	path	= new ArrayList<>();

	public Road() {

	}

	public Road(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}

	public Flag getFlagA() {
		return flagA;
	}

	public void setFlagA(Flag flagA) {
		this.flagA = flagA;
	}

	public Flag getFlagB() {
		return flagB;
	}

	public void setFlagB(Flag flagB) {
		this.flagB = flagB;
	}

	public List<Vector2i> getPath() {
		return path;
	}

	public void toStream(SettOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeObject(flagA);
		out.writeObject(flagA);
		out.writeInt(path.size());
		for (Vector2i v : path)
			out.writeVec2i(v);
	}

	@Override
	public void fromStream(SettInputStream str) throws IOException {
		id = str.readInt();
		flagA = str.readObject(Flag.class);
		flagB = str.readObject(Flag.class);
		int cnt = str.readInt();
		while (cnt-- > 0)
			path.add(str.readVec2i(new Vector2i()));
	}

}
