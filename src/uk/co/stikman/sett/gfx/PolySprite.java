package uk.co.stikman.sett.gfx;

import uk.co.stikman.sett.gfx.util.Coord;

public class PolySprite extends Sprite {

	public PolySprite(GameResources owner, String name) {
		super(owner, name);
	}

	public void addVertex(Coord c) {
		addVertexInt(c);
	}
	
	

}
