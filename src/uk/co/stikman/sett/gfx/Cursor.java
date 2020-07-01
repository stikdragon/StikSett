package uk.co.stikman.sett.gfx;

import java.awt.image.BufferedImage;

import uk.co.stikman.sett.gfx.lwjgl.Window3DNative;


public abstract class Cursor {

	private String			name;
	private BufferedImage	image;
	private int				hotspotX;
	private int				hotspotY;
	private Window3DNative		window;

	public Cursor(Window3DNative window) {
		this.window = window;
	}

	public String getName() {
		return name;
	}

	public BufferedImage getImage() {
		return image;
	}

	public int getHotspotX() {
		return hotspotX;
	}

	public void setHotspotX(int hotspotX) {
		this.hotspotX = hotspotX;
	}

	public int getHotspotY() {
		return hotspotY;
	}

	public void setHotspotY(int hotspotY) {
		this.hotspotY = hotspotY;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public Window3DNative getWindow() {
		return window;
	}

	public abstract void apply();

}
