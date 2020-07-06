package uk.co.stikman.sett.gfx;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import uk.co.stikman.sett.gfx.util.ResourceLoadError;


public class Image {

	private Texture	texture;
	private int		width;
	private int		height;
	private float	imgU;
	private float	imgV;
	private String	name;

	public Image(Window3D wnd, InputStream is, String name) throws ResourceLoadError {
		this.name = name;
		wnd.loadImage(name, this, is, wnd.isUseLinearTextures());
	}

	public Image(Window3D wnd, BufferedImage img, String name) throws ResourceLoadError {
		this.name = name;
		wnd.loadImage(name, this, img, wnd.isUseLinearTextures());
	}

	public void setTexture(Texture txt) {
		this.texture = txt;
	}

	public void setDimensions(int w, int h, float u, float v) {
		this.width = w;
		this.height = h;
		this.imgU = u;
		this.imgV = v;
	}

	public Texture getTexture() {
		return texture;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public float getImgU() {
		return imgU;
	}

	public float getImgV() {
		return imgV;
	}

	@Override
	public String toString() {
		return "Image [name=" + name + ", width=" + width + ", height=" + height + "]";
	}

	public String getName() {
		return name;
	}

}
