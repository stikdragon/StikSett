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
		menu.setOnQuit(x -> getApp().quit()); 
		menu.setOnNewGame(x -> newGameScreen()); 
		menu.show();
	}

	private void newGameScreen() {
		NewGameWindow wnd = new NewGameWindow(getApp());
		wnd.show();
		
	}

	@Override
	public void hidden() {
		super.hidden();
	}

	@Override
	public void setViewport(int w, int h) {

	}

	@Override
	public void render() {
		super.render();
	}

}
