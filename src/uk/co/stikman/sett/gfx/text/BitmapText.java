package uk.co.stikman.sett.gfx.text;

import uk.co.stikman.sett.gfx.Shader;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.text.BitmapFont.BMChar;
import uk.co.stikman.utils.math.Matrix4;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector4;

public abstract class BitmapText {
	protected String			text	= "";

	protected BitmapFont		font;
	protected RenderTextOptions	options;
	protected int				maxwidth;
	protected TextFragments		textFragments;
	protected Shader			shader;
	protected Window3D			window;

	/**
	 * You can force the font to be a certain colour. You can also set the mix
	 * to control how much of this colour is used. At <code>1.0f</code> it'll
	 * use this colour exclusively, at <code>0.0f</code> it will ignore this
	 * colour. <code>0.5f</code> will blend it half with this colour and half
	 * with the text's own specified colours
	 * 
	 */
	public abstract void render(Matrix4 projmat, Vector4 overrideColour, float overrideColourMix);

	public BitmapText(Window3D window, Shader shader, BitmapFont font, RenderTextOptions options, int maxwidth) {
		this.window = window;
		this.font = font;
		this.options = options;
		this.maxwidth = maxwidth;
		this.shader = shader;
	}

	public void setText(String text) {
		if (text == null)
			text = "";
		if (text.equals(this.text))
			return;
		this.text = text;
		this.textFragments = null;
		invalidate();
	}

	protected abstract void invalidate();

	protected TextFragments getFragments() {
		if (textFragments == null)
			textFragments = new TextFragments(text, options.isColourFormatting());
		return textFragments;
	}

	public String getText() {
		return text;
	}

	public Vector2 measure(Vector2 res) {
		TextFragments fragments = getFragments();
		String text = fragments.toString();

		float maxx = 0.0f;
		float curx = 0.0f;
		float cury = 0.0f;
		int lineheight = getLineHeight();
		int lastSpace = -1;
		for (int i = 0; i < text.length(); ++i) {
			char c = text.charAt(i);
			if (c == ' ' && curx > 0)
				lastSpace = i;
			if (c == 13) {
				maxx = Math.max(maxx, curx);
				curx = 0.0f;
				cury += lineheight;
				continue;
			}

			BMChar ch = font.get(c);
			if (ch == null)
				continue;

			curx += ch.xadvance;

			if (curx > maxwidth && options.getWrap() != WordWrap.NONE) {
				if (options.getWrap() == WordWrap.BREAK_WORD) {
					maxx = Math.max(maxx, curx);
					curx = 0.0f;
					cury += lineheight;
				} else if (options.getWrap() == WordWrap.WRAP && lastSpace != -1) {
					//
					// Little more fiddly, need to rewind to the previous space
					//
					maxx = Math.max(maxx, curx);
					curx = 0.0f;
					cury += lineheight;
					i = lastSpace;
					lastSpace = -1;
					continue;
				}
			}
		}
		res.x = Math.max(maxx, curx);
		res.y = cury + lineheight;
		return res;
	}

	public RenderTextOptions getOptions() {
		return options;
	}

	public int getLineHeight() {
		return font.getLineHeight();
	}

	public boolean isLoaded() {
		return font != null && font.isLoaded();
	}

	public int getLineWidth() {
		float curx = 0.0f;
		if (!font.isLoaded())
			return 0;
		//		float cury = 0.0f;
		float max = 0.0f;
		for (int i = 0; i < text.length(); ++i) {
			char c = text.charAt(i);
			if (c == 13) {
				if (max < curx)
					max = curx;
				curx = 0.0f;
				continue;
			}
			BMChar ch = font.get(c);
			if (ch != null)
				curx += ch.xadvance;
		}
		if (max < curx)
			max = curx;
		return (int) max;
	}

	public void destroy() {
	}

}