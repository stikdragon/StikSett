package uk.co.stikman.sett;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import uk.co.stikman.sett.game.NoddyType;
import uk.co.stikman.utils.math.Vector3i;

public class NoddyDefs {
	private Map<String, NoddyType>			defs		= new HashMap<>();
	private Map<VoxelModelFrame, Vector3i>	attachments	= new HashMap<>();

	public NoddyType get(String name) {
		NoddyType t = defs.get(name);
		if (t == null)
			throw new NoSuchElementException("NoddyType " + name + " does not exist");
		return t;
	}

	public void setAttach(VoxelModel model, int frame, Vector3i pos) {
		VoxelModelFrame k = new VoxelModelFrame();
		k.setFrame(frame);
		k.setModel(model);
		attachments.put(k, pos);
	}

	public NoddyType find(String name) {
		return defs.get(name);
	}

	public Set<String> getKeys() {
		return defs.keySet();
	}

}
