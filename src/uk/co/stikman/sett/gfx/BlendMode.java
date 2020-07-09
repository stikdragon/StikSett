package uk.co.stikman.sett.gfx;

public enum BlendMode {
	OFF,
	ON,

	/**
	 * you generally want to use this if you're rendering to a texture since the
	 * alpha values won't be premultiplied
	 */
	FBO_SEPARATE;
}
