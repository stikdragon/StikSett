package uk.co.stikman.sett.gfx.text;

public class Fragment {
	private StringBuilder	text	= new StringBuilder();
	private float[]			colour	= new float[4];
	private int				startIndex;
	private int				endIndex;

	public String getText() {
		return text.toString();
	}

	public float[] getColour() {
		return colour;
	}

	public Fragment(float[] colour, int startindex) {
		super();
		this.colour = colour;
		this.startIndex = startindex;
		this.endIndex = startindex;
	}

	public Fragment(String text, float[] colour) {
		super();
		this.colour = colour;
		this.text.append(text);
		endIndex += text.length();
	}

	public Fragment() {
		super();
	}

	void append(char ch) {
		text.append(ch);
		++endIndex;
	}

	public boolean isReset() {
		return colour == null;
	}

	public boolean isEmpty() {
		return text.toString().isEmpty();
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

}