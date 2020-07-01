package uk.co.stikman.sett.gfx;

import uk.co.stikman.utils.math.Vector4;

public class VectorColours {

	public static final Vector4	BLACK	= new Vector4(0, 0, 0, 1);
	public static final Vector4	WHITE	= new Vector4(1, 1, 1, 1);
	public static final Vector4	RED		= new Vector4(1, 0, 0, 1);

	public static Vector4 parseCSS(String s) {
		if (s.startsWith("#"))
			s = s.substring(1);
		if (s.length() != 6)
			throw new IllegalArgumentException("Colour must be RRGGBB");
		int n = Integer.parseInt(s, 16);
		return new Vector4((n & 0xff0000) >> 16, (n & 0x00ff00) >> 8, n & 0x0000ff, 0).divide(256.0f);
	}

}
