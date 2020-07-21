package uk.co.stikman.sett.game;

import uk.co.stikman.sett.VoxelModel;

public class NoddySequence {
	private final String	name;
	private VoxelModel		model;

	public NoddySequence(String name, VoxelModel model) {
		super();
		this.name = name;
		this.model = model;
	}

	public VoxelModel getModel() {
		return model;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "NoddySequence [name=" + name + ", model=" + model + "]";
	}

}
