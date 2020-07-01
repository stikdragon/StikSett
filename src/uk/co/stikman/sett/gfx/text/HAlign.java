package uk.co.stikman.sett.gfx.text;
public enum HAlign {
	LEFT, CENTRE, RIGHT, NONE;

	@Override
	public String toString() {
		switch (this) {
			case CENTRE:
				return "Centre";
			case LEFT:
				return "Left";
			case NONE:
				return "None";
			case RIGHT:
				return "Right";
		}
		return "???";
	}

	public static HAlign parse(String s) {
		if ("center".equalsIgnoreCase(s))
			return CENTRE;
		return valueOf(s.toUpperCase());
	}
}
