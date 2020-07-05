package uk.co.stikman.sett.gfx;

import uk.co.stikman.utils.math.Vector3;
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

	public static Vector3 HSBtoRGB(Vector3 in) {
		int r = 0, g = 0, b = 0;
		float h = (in.x - (float) Math.floor(in.x)) * 6.0f;
		float f = h - (float) java.lang.Math.floor(h);
		float p = in.z * (1.0f - in.y);
		float q = in.z * (1.0f - in.y * f);
		float t = in.z * (1.0f - (in.y * (1.0f - f)));
		switch ((int) h) {
		case 0:
			r = (int) (in.z * 255.0f + 0.5f);
			g = (int) (t * 255.0f + 0.5f);
			b = (int) (p * 255.0f + 0.5f);
			break;
		case 1:
			r = (int) (q * 255.0f + 0.5f);
			g = (int) (in.z * 255.0f + 0.5f);
			b = (int) (p * 255.0f + 0.5f);
			break;
		case 2:
			r = (int) (p * 255.0f + 0.5f);
			g = (int) (in.z * 255.0f + 0.5f);
			b = (int) (t * 255.0f + 0.5f);
			break;
		case 3:
			r = (int) (p * 255.0f + 0.5f);
			g = (int) (q * 255.0f + 0.5f);
			b = (int) (in.z * 255.0f + 0.5f);
			break;
		case 4:
			r = (int) (t * 255.0f + 0.5f);
			g = (int) (p * 255.0f + 0.5f);
			b = (int) (in.z * 255.0f + 0.5f);
			break;
		case 5:
			r = (int) (in.z * 255.0f + 0.5f);
			g = (int) (p * 255.0f + 0.5f);
			b = (int) (q * 255.0f + 0.5f);
			break;
		}
		return new Vector3(r, g, b).normalize();
	}

	// largely from https://stackoverflow.com/questions/2997656/how-can-i-use-the-hsl-colorspace-in-java
	public static Vector3 HSLtoRGB(Vector3 in) {
		float q, p, r, g, b;

		if (in.y == 0) {
			r = g = b = in.z; // achromatic
		} else {
			q = in.z < 0.5 ? (in.z * (1 + in.y)) : (in.z + in.y - in.z * in.y);
			p = 2 * in.z - q;
			r = hue2rgb(p, q, in.x + 1.0f / 3);
			g = hue2rgb(p, q, in.x);
			b = hue2rgb(p, q, in.x - 1.0f / 3);
		}
		return new Vector3(r, g, b);
	}

	private static float hue2rgb(float p, float q, float h) {
		if (h < 0) 
			h += 1;
		if (h > 1) 
			h -= 1;
		if (6 * h < 1) 
			return p + ((q - p) * 6 * h);
		if (2 * h < 1) 
			return q;
		if (3 * h < 2) 
			return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
		return p;
	}
}
