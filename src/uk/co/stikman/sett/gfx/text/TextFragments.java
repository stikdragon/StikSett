package uk.co.stikman.sett.gfx.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextFragments implements Iterable<Fragment> {
	

	private static final float[][]	COLOURS	= new float[][] {
		new float[]{0.0f, 0.0f, 0.0f, 1.0f},
		new float[]{0.0f, 0.0f, 0.67f, 1.0f},
		new float[]{0.0f, 0.67f, 0.0f, 1.0f},
		new float[]{0.0f, 0.67f, 0.67f, 1.0f},

		new float[]{0.67f, 0.0f, 0.0f, 1.0f},
		new float[]{0.67f, 0.0f, 0.67f, 1.0f},
		new float[]{0.67f, 0.33f, 0.0f, 1.0f},
		new float[]{0.67f, 0.67f, 0.67f, 1.0f},

		new float[]{0.33f, 0.33f, 0.33f, 1.0f},
		new float[]{0.33f, 0.33f, 1.0f, 1.0f},
		new float[]{0.33f, 1.0f, 0.33f, 1.0f},
		new float[]{0.33f, 1.0f, 1.0f, 1.0f},

		new float[]{1.0f, 0.33f, 0.33f, 1.0f},
		new float[]{1.0f, 0.33f, 1.0f, 1.0f},
		new float[]{1.0f, 1.0f, 0.33f, 1.0f},
		new float[]{1.0f, 1.0f, 1.0f, 1.0f},
	};

	private List<Fragment>			fragments	= new ArrayList<>();
	private String complete; 

	public TextFragments(String source, boolean decode) {
		if (decode) {
			go(source);
		} else {
			Fragment f = new Fragment(source, null);
			fragments.add(f);
		}
		buildComplete();
	}

	private void buildComplete() {
		StringBuilder sb = new StringBuilder();
		for (Fragment f : fragments) 
			sb.append(f.getText());
		complete = sb.toString();
	}

	private void go(String source) {
		int ptr = 0;
		int len = source.length();
		Fragment frag = new Fragment(null, 0);
		boolean expectCode = false;
		int charcnt = 0;
		while (ptr < len) {
			char ch = source.charAt(ptr);
			if (expectCode) {
				if (ch == '^') {
					frag.append('^'); ++charcnt;
				} else if (Character.isDigit(ch)) {
					frag = changeColour(ch - '0', frag, charcnt);
				} else if (ch >= 'a' && ch <= 'f') {
					frag = changeColour(10 + ch - 'a', frag, charcnt);
				} else if (ch >= 'A' && ch < 'F') {
					frag = changeColour(10 + ch - 'A', frag, charcnt);
				} else if (ch == 'x' || ch == 'X') {
					frag = changeColour(-1, frag, charcnt);
				} else {
					frag.append('?'); ++charcnt;
					frag.append(ch); ++charcnt;
				}
				expectCode = false;

			} else {
				if (ch == '^') {
					expectCode = true;
				} else {
					frag.append(ch); ++charcnt;
				}
			}
			++ptr;
		}
		if (!frag.isEmpty())
			fragments.add(frag);
	}

	private Fragment changeColour(int colouridx, Fragment frag, int charindex) {
		fragments.add(frag);
		if (colouridx == -1)
			frag = new Fragment(null, charindex);
		else
			frag = new Fragment(COLOURS[colouridx], charindex);
		return frag;
	}

	@Override
	public Iterator<Fragment> iterator() {
		return fragments.iterator();
	}

	@Override
	public String toString() {
		return complete;
	}
	
	
}