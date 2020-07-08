package uk.co.stikman.sett;

import uk.co.stikman.sett.client.renderer.BaseView;
import uk.co.stikman.sett.gfx.util.Rect;

public class MainMenuView extends BaseView {

	private MainMenuWindow menu;

	public MainMenuView(SettApp app) {
		super(app);
	}

	@Override
	public void shown() {
		super.shown();

		menu = new MainMenuWindow(getApp());
		menu.setBounds(new Rect(10, 10, 130, 150));
		menu.show();
	}

	@Override
	public void hidden() {
		super.hidden();
	}

	@Override
	public void setViewport(int w, int h) {

	}

}
