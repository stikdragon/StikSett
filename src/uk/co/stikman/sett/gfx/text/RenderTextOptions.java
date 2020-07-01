package uk.co.stikman.sett.gfx.text;

import uk.co.stikman.sett.gfx.VectorColours;
import uk.co.stikman.utils.math.Vector4;

public class RenderTextOptions {

	public static final RenderTextOptions	DEFAULT				= new RenderTextOptions(HAlign.LEFT, VAlign.TOP);
	private VAlign							alignV				= VAlign.TOP;
	private HAlign							alignH				= HAlign.LEFT;
	private WordWrap						wrap				= WordWrap.NONE;
	private boolean							colourFormatting	= true;
	private OutlineMode						outlineMode			= OutlineMode.NONE;
	private float							outlineBlendFactor	= 1.0f;
	private Vector4							outlineColour		= VectorColours.BLACK;

	public RenderTextOptions() {

	}

	public RenderTextOptions(RenderTextOptions copy) {
		this.alignH = copy.alignH;
		this.alignV = copy.alignV;
		this.wrap = copy.wrap;
		this.colourFormatting = copy.colourFormatting;
		this.outlineMode = copy.outlineMode;
		this.outlineBlendFactor = copy.outlineBlendFactor;
		this.outlineColour = new Vector4(copy.outlineColour);
	}

	public RenderTextOptions(HAlign textAlign, VAlign valign) {
		this.alignH = textAlign;
		this.alignV = valign;
	}

	public RenderTextOptions(HAlign textAlign, VAlign valign, OutlineMode outline) {
		this.alignH = textAlign;
		this.alignV = valign;
		this.outlineMode = outline;
	}

	public RenderTextOptions(HAlign textAlign, VAlign valign, WordWrap wrap) {
		this.alignH = textAlign;
		this.alignV = valign;
		this.wrap = wrap;
	}

	public RenderTextOptions(HAlign textAlign, VAlign valign, OutlineMode outline, WordWrap wrap) {
		this.alignH = textAlign;
		this.alignV = valign;
		this.outlineMode = outline;
		this.wrap = wrap;
	}

	public VAlign getAlignV() {
		return alignV;
	}

	public void setAlignV(VAlign alignV) {
		this.alignV = alignV;
	}

	public HAlign getAlignH() {
		return alignH;
	}

	public void setAlignH(HAlign alignH) {
		this.alignH = alignH;
	}

	public OutlineMode getOutlineMode() {
		return outlineMode;
	}

	public void setOutlineMode(OutlineMode outline) {
		this.outlineMode = outline;
	}

	public WordWrap getWrap() {
		return wrap;
	}

	public void setWrap(WordWrap wrap) {
		this.wrap = wrap;
	}

	public boolean isColourFormatting() {
		return colourFormatting;
	}

	public void setColourFormatting(boolean colourFormatting) {
		this.colourFormatting = colourFormatting;
	}

	public float getOutlineBlendFactor() {
		return outlineBlendFactor;
	}

	/**
	 * Along with {@link #setOutlineColour(Colour)}, this controls how much of
	 * the text's current colour is merged with the outline colour. Obviously
	 * only has an effect when outline is on. 1.0f means it's entirely the
	 * outline colour, 0.0f means entirely the text colour (you probably don't
	 * want this). Having it somewhere between means you get a darker outline,
	 * but it still retains some of the current text colour too
	 * 
	 * @param outlineBlendFactor
	 */
	public void setOutlineBlendFactor(float outlineBlendFactor) {
		this.outlineBlendFactor = outlineBlendFactor;
	}

	public Vector4 getOutlineColour() {
		return outlineColour;
	}

	/**
	 * The colour used to render text outlines. It's done in a bit of a stupid
	 * way, which involves a lot of overlapping renders, so setting a
	 * transparent colour here will look really bad
	 * 
	 * @param outlineColour
	 */
	public void setOutlineColour(Vector4 outlineColour) {
		this.outlineColour = outlineColour;
	}

	public void copy(RenderTextOptions copy) {
		this.alignV = copy.alignV;
		this.alignH = copy.alignH;
		this.wrap = copy.wrap;
		this.colourFormatting = copy.colourFormatting;
		this.outlineMode = copy.outlineMode;
		this.outlineBlendFactor = copy.outlineBlendFactor;
		this.outlineColour = copy.outlineColour;
	}

}