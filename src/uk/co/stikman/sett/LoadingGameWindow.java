package uk.co.stikman.sett;

import uk.co.stikman.sett.gfx.RectSprite;
import uk.co.stikman.sett.gfx.StretchMode;
import uk.co.stikman.sett.gfx.ui.ProgressBar;
import uk.co.stikman.sett.gfx.ui.WindowPosition;
import uk.co.stikman.sett.gfx.util.Rect;

public class LoadingGameWindow extends SettStandardWindow {

	private int			mapSize	= 3;
	private ProgressBar	pb;

	public LoadingGameWindow(SettApp v) {
		super(v, 0);
		getTheming().setBackgroundSprite((RectSprite) v.getUIResources().findSprite("dlgbox4"), StretchMode.createSmart(8, 8, 9, 7));
		setCaption("Loading...");
		setGlass(true);
		setWindowPosition(WindowPosition.CENTRE);
		setBounds(new Rect(0, 0, 140, 60));

		pb = new ProgressBar(this);
		pb.setBounds(new Rect(12, 32, 140 - 24, 8));
	}

	@Override
	public void screenResize(int w, int h) {
		super.screenResize(w, h);
	}

	public void setProgress(int p) {
		pb.setValue(p);
	}

	public int getProgress() {
		return pb.getValue();
	}

	public void closeIn1000ms() {
		getUi().newTimer(1000, () -> hide(), true);
	}
}
