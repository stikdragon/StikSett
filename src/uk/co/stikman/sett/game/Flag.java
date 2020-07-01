package uk.co.stikman.sett.game;

import java.util.ArrayList;
import java.util.List;

public class Flag implements HasId, IsNodeObject {
	private final int			id;
	private List<HasFlag>		connections	= new ArrayList<>();
	private VoxelModelParams	vmp;

	public Flag(int id) {
		super();
		this.id = id;
		vmp = new VoxelModelParams();
		vmp.setName(null);// TODO: ...
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
	public VoxelModelParams getVoxelModelInfo() {
		return vmp;
	}

}
