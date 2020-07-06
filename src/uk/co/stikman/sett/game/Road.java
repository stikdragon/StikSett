package uk.co.stikman.sett.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.stikman.utils.math.Vector2i;

public class Road implements GameObject, HasFlag {

	private final int					id;
	private Flag						flagA;
	private Flag						flagB;
	private transient List<Vector2i>	path	= new ArrayList<>();

	public Road(int id) {
		super();
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
	}

}
