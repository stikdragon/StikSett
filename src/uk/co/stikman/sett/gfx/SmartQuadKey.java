package uk.co.stikman.sett.gfx;

import uk.co.stikman.utils.math.Vector4;

public class SmartQuadKey {

	public Vector4		colour;
	public int			h;
	public int			w;
	public RectSprite	image;
	private StretchMode	stretch;

	public void set(RectSprite image, int w, int h, Vector4 colour, StretchMode stretch) {
		this.image = image;
		this.w = w;
		this.h = h;
		this.colour = colour;
		this.stretch = stretch;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colour == null) ? 0 : colour.hashCode());
		result = prime * result + h;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((stretch == null) ? 0 : stretch.hashCode());
		result = prime * result + w;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SmartQuadKey other = (SmartQuadKey) obj;
		if (colour == null) {
			if (other.colour != null)
				return false;
		} else if (!colour.equals(other.colour))
			return false;
		if (h != other.h)
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (stretch == null) {
			if (other.stretch != null)
				return false;
		} else if (!stretch.equals(other.stretch))
			return false;
		if (w != other.w)
			return false;
		return true;
	}

}
