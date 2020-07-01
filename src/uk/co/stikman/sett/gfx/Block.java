package uk.co.stikman.sett.gfx;

import java.util.ArrayList;
import java.util.List;

class Block {
	private final String	name;
	private List<String>	items	= new ArrayList<>();

	public String getName() {
		return name;
	}

	public List<String> getItems() {
		return items;
	}

	public Block(String name) {
		super();
		this.name = name;
	}

}
