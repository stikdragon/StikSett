package uk.co.stikman.sett.gfx;

import uk.co.stikman.sett.gfx.util.Coord;
import uk.co.stikman.utils.math.Vector2;

public class RectSprite extends Sprite {

	private int		height;
	private int		width;
	private Vector2	uvOffset;
	private Vector2	uvSize;

	public RectSprite(GameResources owner, String name) {
		super(owner, name);
	}

	public void setRect(int w, int h, float u1, float v1, float u2, float v2) {
		addVertexInt(new Coord(0, 0, u1, v1));
		addVertexInt(new Coord(w, 0, u2, v1));
		addVertexInt(new Coord(w, h, u2, v2));
		addVertexInt(new Coord(0, h, u1, v2));
		this.width = w;
		this.height = h;
		this.uvOffset = new Vector2(u1, v1);
		this.uvSize = new Vector2(u2 - u1, v2 - v1);
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public Vector2 getUvOffset() {
		return uvOffset;
	}

	public Vector2 getUvSize() {
		return uvSize;
	}

}
