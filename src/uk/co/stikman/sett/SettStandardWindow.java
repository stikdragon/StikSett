package uk.co.stikman.sett;

import uk.co.stikman.sett.gfx.GameResources;
import uk.co.stikman.sett.gfx.RectSprite;
import uk.co.stikman.sett.gfx.StretchMode;
import uk.co.stikman.sett.gfx.ui.SimpleWindow;
import uk.co.stikman.sett.gfx.ui.WindowTheming;
import uk.co.stikman.sett.gfx.util.Rect;

public class SettStandardWindow extends SimpleWindow {

	private final SettApp	app;
	private int				borderLeft		= 8;
	private int				borderRight		= 8;
	private int				borderTop		= 9;
	private int				borderBottom	= 7;

	public SettStandardWindow(SettApp v, int background) {
		super(v.getUI(), v.getUIResources());
		this.app = v;
		GameResources res = v.getUIResources();
		WindowTheming theme = new WindowTheming();
		if (background == 0)
			theme.setBackgroundSprite((RectSprite) res.findSprite("dlgbox3"), StretchMode.createSmart(borderLeft, borderRight, borderTop, borderBottom));
		else if (background == 1)
			theme.setBackgroundSprite((RectSprite) res.findSprite("dlgbox4"), StretchMode.createSmart(borderLeft, borderRight, borderTop, borderBottom));
		theme.setFont(res.findFont("sett_8"));
		theme.setTitleFont(res.findFont("sett_8"));
		theme.setButtonSprite((RectSprite) res.findSprite("button1"));
		theme.setButtonSpriteHover((RectSprite) res.findSprite("button1_h"));
		theme.setButtonSpriteDown((RectSprite) res.findSprite("button1_d"));
		theme.setFlatButtonSpriteDown((RectSprite) res.findSprite("button2_d"));
		theme.setButtonSpriteSM(StretchMode.createSmart(1));
		setTheming(theme);
		v.getUI().getWindows().add(this);
	}

	@Override
	public void show() {
		Rect r = app.getUI().getUIBounds();
		screenResize((int) r.w, (int) r.h);
		super.show();
	}

	@Override
	public void hide() {
		super.hide();
		app.getUI().removeWindow(this);
	}

	public SettApp getApp() {
		return app;
	}

	public int getBorderLeft() {
		return borderLeft;
	}

	public int getBorderRight() {
		return borderRight;
	}

	public int getBorderTop() {
		return borderTop;
	}

	public int getBorderBottom() {
		return borderBottom;
	}

	public Rect getClientBounds() {
		Rect r = getBounds();
		return new Rect(borderLeft, borderTop, r.w - borderLeft - borderRight, r.h - borderTop - borderBottom);
	}

}
