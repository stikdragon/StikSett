package uk.co.stikman.sett.game;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import uk.co.stikman.sett.VoxelModel;

public class NoddyType {
	private final String		name;
	private String				description;
	private List<NoddySequence>	sequences	= new ArrayList<>();

	public NoddyType(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void addSequence(String name, VoxelModel model) {
		this.sequences.add(new NoddySequence(name, model));
	}

	public List<NoddySequence> getSequences() {
		return sequences;
	}

	public NoddySequence getSequence(String name) {
		for (NoddySequence s : sequences)
			if (name.equals(s.getName()))
				return s;
		throw new NoSuchElementException(name);
	}

}
