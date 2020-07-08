package uk.co.stikman.sett;

import java.util.ArrayList;
import java.util.List;

import uk.co.stikman.sett.gfx.VectorColours;
import uk.co.stikman.sett.gfx.ui.Button;
import uk.co.stikman.sett.gfx.ui.Component;
import uk.co.stikman.sett.gfx.ui.WindowPosition;
import uk.co.stikman.sett.gfx.util.Rect;

public class MainMenuWindow extends SettStandardWindow {

	private final List<Component>	items	= new ArrayList<>();
	private final Rect				tmpR	= new Rect();
	private final Rect				tmpR2	= new Rect();
	private Button					btnQuit;

	public MainMenuWindow(SettApp v) {
		super(v);
		setCaption("Main Menu");
		setGlass(true);
		setWindowPosition(WindowPosition.CENTRE);
		setBounds(new Rect(0, 0, 180, 150));
	}

	@Override
	public void init() {
		super.init();
		items.add(new Button(this, "new", "New Game"));
		items.add(new Button(this, "save", "Save Game"));
		items.add(new Button(this, "load", "Load Game"));
		items.add(new Button(this, "options", "Options"));
		items.add(btnQuit = new Button(this, "quit", "Quit"));
		btnQuit.setButtonColour(VectorColours.rgb(255, 226, 236));
	}

	@Override
	public void screenResize(int w, int h) {
		super.screenResize(w, h);
		int y = 26;
		int x = (int) getBounds().x;
		Rect r = tmpR;
		r.set(getBounds());
		for (Component l : items) {
			l.setBounds(tmpR2.set(r.x + 20, r.y + y, 80, 19));
			y += 21;
		}
	}

}
