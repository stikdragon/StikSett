package uk.co.stikman.sett.gfx.text;
public enum VAlign {
	TOP, CENTRE, BOTTOM, NONE;

	

	@Override
	public String toString() {
		switch (this) {
		case CENTRE:
			return "Centre";
		case TOP:
			return "Top";
		case NONE:
			return "None";
		case BOTTOM:
			return "Bottom";
		}
		return "???";
	}

	public static VAlign parse(String s) {
		if ("center".equalsIgnoreCase(s))
			return CENTRE;
		return valueOf(s.toUpperCase());
	}
}