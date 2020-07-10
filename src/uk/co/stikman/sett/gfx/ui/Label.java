package uk.co.stikman.sett.gfx.ui;

import uk.co.stikman.sett.gfx.text.RenderTextOptions;
import uk.co.stikman.utils.math.Vector4;

public class Label extends Component {

	private String				caption;
	private RenderTextOptions	rto		= new RenderTextOptions();
	private Vector4				colour	= null;

	public Label(SimpleWindow owner, String caption) {
		super(owner, null);
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	@Override
	public void render() {
		Vector4 c = theme().getFontColour();
		if (colour != null)
			c = colour;
		getOwner().getWindow().drawText(getBounds(), getCaption(), theme().getFont(), rto, c);
	}

	/**
	 * if <code>null</code> then uses the theme colour
	 * 
	 * @param col
	 */
	public void setColour(Vector4 col) {
		this.colour = col;
	}

	public Vector4 getColour() {
		return colour;
	}

}
