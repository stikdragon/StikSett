package uk.co.stikman.sett.gfx.ui;

import uk.co.stikman.sett.gfx.text.RenderTextOptions;

public class Label extends Component {

	private String				caption;
	private RenderTextOptions	rto	= new RenderTextOptions();

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
		getOwner().getWindow().drawText(getBounds(), getCaption(), theme().getFont(), rto, theme().getFontColour());
	}

}
