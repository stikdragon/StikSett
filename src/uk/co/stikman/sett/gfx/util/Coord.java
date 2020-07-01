package uk.co.stikman.sett.gfx.util;

public class Coord {
	public int		x;
	public int		y;
	public float	u;
	public float	v;

	public Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public Coord(int x, int y, float u, float v) {
		this.x = x;
		this.y = y;
		this.u = u;
		this.v = v;
	}
	

	public Coord() {

	}

	public static Coord parse(String s) {
		String[] bits = s.split(", *");
		Coord r = new Coord();
		if (bits.length != 2 && bits.length != 4)
			throw new IllegalArgumentException("Expected 2 or 4 coordinates (X,Y,[U,V])");

		if (bits.length >= 2) {
			r.x = Integer.parseInt(bits[0]);
			r.y = Integer.parseInt(bits[1]);
		}
		if (bits.length == 4) {
			r.u = Integer.parseInt(bits[2]);
			r.v = Integer.parseInt(bits[3]);
		} else {
			r.u = r.x;
			r.v = r.y;
		}
		return r;
	}
}
