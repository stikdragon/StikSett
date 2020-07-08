package uk.co.stikman.sett;

import uk.co.stikman.sett.gfx.GameResources;
import uk.co.stikman.sett.gfx.RectSprite;
import uk.co.stikman.sett.gfx.StretchMode;
import uk.co.stikman.sett.gfx.ui.SimpleWindow;
import uk.co.stikman.sett.gfx.ui.WindowTheming;
import uk.co.stikman.sett.gfx.util.Rect;

public class SettStandardWindow extends SimpleWindow {

	private final SettApp app;

	public SettStandardWindow(SettApp v) {
		super(v.getUI(), v.getUIResources());
		this.app = v;
		GameResources res = v.getUIResources();
		WindowTheming theme = new WindowTheming();
		theme.setBackgroundSprite((RectSprite) res.findSprite("dlgbox2"), StretchMode.createSmart(4, 4, 16, 4));
		theme.setFont(res.findFont("pixelmix_8"));
		theme.setTitleFont(res.findFont("pixelmix_8"));
		theme.setButtonSprite((RectSprite) res.findSprite("button1"));
		theme.setButtonSpriteDown((RectSprite) res.findSprite("button1_d"));
		theme.setFlatButtonSpriteDown((RectSprite) res.findSprite("button2_d"));
		theme.setButtonSpriteSM(StretchMode.createSmart(2));
		setTheming(theme);
		v.getUI().getWindows().add(this);
	}

	@Override
	public void show() {
		Rect r = app.getUI().getUIBounds();
		screenResize((int) r.w, (int) r.h);
		super.show();
	}

	public void close() {
		app.getUI().removeWindow(this);
	}

	
}