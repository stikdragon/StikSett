package uk.co.stikman.sett.gfx;

public class StretchMode {

	public enum Mode {
		SCALE, TILE, SMART
	}

	public final static StretchMode	SCALE	= new StretchMode(Mode.SCALE);
	public final static StretchMode	TILE	= new StretchMode(Mode.TILE);
	private Mode					mode;
	private int						marginLeft;
	private int						marginRight;
	private int						marginTop;
	private int						marginBottom;

	StretchMode(Mode mode) {
		this.mode = mode;
	}

	public static StretchMode createSmart(int margins) {
		StretchMode res = new StretchMode(Mode.SMART);
		res.marginLeft = margins;
		res.marginRight = margins;
		res.marginTop = margins;
		res.marginBottom = margins;
		return res;
	}

	public static StretchMode createSmart(int left, int right, int top, int bottom) {
		StretchMode res = new StretchMode(Mode.SMART);
		res.marginLeft = left;
		res.marginRight = right;
		res.marginTop = top;
		res.marginBottom = bottom;
		return res;
	}

	public static StretchMode getScale() {
		return SCALE;
	}

	public static StretchMode getTile() {
		return TILE;
	}

	public Mode getMode() {
		return mode;
	}

	public int getMarginLeft() {
		return marginLeft;
	}

	public int getMarginRight() {
		return marginRight;
	}

	public int getMarginTop() {
		return marginTop;
	}

	public int getMarginBottom() {
		return marginBottom;
	}

	@Override
	public String toString() {
		return mode + "[" + marginLeft + "," + marginTop + "," + marginBottom + "," + marginRight + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + marginBottom;
		result = prime * result + marginLeft;
		result = prime * result + marginRight;
		result = prime * result + marginTop;
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
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
		StretchMode other = (StretchMode) obj;
		if (marginBottom != other.marginBottom)
			return false;
		if (marginLeft != other.marginLeft)
			return false;
		if (marginRight != other.marginRight)
			return false;
		if (marginTop != other.marginTop)
			return false;
		if (mode != other.mode)
			return false;
		return true;
	}

}
