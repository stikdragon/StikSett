package uk.co.stikman.sett.gfx.ui;

import uk.co.stikman.sett.gfx.RectSprite;
import uk.co.stikman.sett.gfx.StretchMode;
import uk.co.stikman.sett.gfx.VectorColours;
import uk.co.stikman.sett.gfx.text.BitmapFont;
import uk.co.stikman.utils.math.Vector4;

public class WindowTheming {
	private Vector4		backgroundColour	= VectorColours.WHITE;
	private Vector4		fontColour			= new Vector4(.8f, .8f, .8f, 1.0f);
	private RectSprite	backgroundSprite;
	private BitmapFont	font;
	private BitmapFont	titleFont;
	private StretchMode	stretchMode;
	private RectSprite	buttonSprite;
	private RectSprite	buttonSpriteDown;
	private StretchMode	buttonSpriteSM;
	private RectSprite	flatButtonSpriteDown;

	public Vector4 getBackgroundColour() {
		return backgroundColour;
	}

	public void setBackgroundColour(Vector4 backgroundColour) {
		this.backgroundColour = backgroundColour;
	}

	public Vector4 getFontColour() {
		return fontColour;
	}

	public void setFontColour(Vector4 fontColour) {
		this.fontColour = fontColour;
	}

	public RectSprite getBackgroundSprite() {
		return backgroundSprite;
	}

	public BitmapFont getFont() {
		return font;
	}

	public BitmapFont getTitleFont() {
		return titleFont;
	}

	public void setTitleFont(BitmapFont titleFont) {
		this.titleFont = titleFont;
	}

	/**
	 * Set the background smart sprite to use. Give it a StretchMode, or null if
	 * you don't want it to be a smart sprite, in which case it'll just draw it
	 * in place (so you must make sure the image is the correct size for your
	 * window)
	 * 
	 * @param sprite
	 * @param stretch
	 */
	public void setBackgroundSprite(RectSprite sprite, StretchMode stretch) {
		backgroundSprite = sprite;
		stretchMode = stretch;
	}

	public void setFont(BitmapFont font) {
		this.font = font;
		if (titleFont == null)
			titleFont = font;
	}

	public StretchMode getStretchMode() {
		return stretchMode;
	}

	public void setStretchMode(StretchMode stretchMode) {
		this.stretchMode = stretchMode;
	}

	public RectSprite getButtonSprite() {
		return buttonSprite;
	}

	public RectSprite getButtonSpriteDown() {
		return buttonSpriteDown;
	}

	public void setButtonSprite(RectSprite buttonSprite) {
		this.buttonSprite = buttonSprite;
	}

	public StretchMode getButtonSpriteSM() {
		return buttonSpriteSM;
	}

	public void setButtonSpriteSM(StretchMode buttonSpriteSM) {
		this.buttonSpriteSM = buttonSpriteSM;
	}

	public void setButtonSpriteDown(RectSprite buttonSpriteDown) {
		this.buttonSpriteDown = buttonSpriteDown;
	}

	public RectSprite getFlatButtonSpriteDown() {
		return this.flatButtonSpriteDown;
	}

	public void setFlatButtonSpriteDown(RectSprite flatButtonSpriteDown) {
		this.flatButtonSpriteDown = flatButtonSpriteDown;
	}

}
