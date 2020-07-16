package uk.co.stikman.sett;

import uk.co.stikman.sett.gfx.Sprite;

public class PageBuilding {

	private final int		x;
	private final int		y;
	private final Sprite	sprite;
	private final String	name;

	public PageBuilding(String name, int x, int y, Sprite sprite) {
		super();
		this.name = name;
		this.x = x;
		this.y = y;
		this.sprite = sprite;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Sprite getSprite() {
		return sprite;
	}

	public String getName() {
		return name;
	}

}