package uk.co.stikman.sett;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import uk.co.stikman.sett.gfx.VectorColours;
import uk.co.stikman.sett.gfx.text.HAlign;
import uk.co.stikman.sett.gfx.text.OutlineMode;
import uk.co.stikman.sett.gfx.text.VAlign;
import uk.co.stikman.sett.gfx.ui.Button;
import uk.co.stikman.sett.gfx.ui.Component;
import uk.co.stikman.sett.gfx.ui.Label;
import uk.co.stikman.sett.gfx.ui.WindowPosition;
import uk.co.stikman.sett.gfx.util.Rect;

public class MainMenuWindow extends SettStandardWindow {

	private final List<Component>		items	= new ArrayList<>();
	private final Rect					tmpR	= new Rect();
	private final Rect					tmpR2	= new Rect();
	private Button						btnQuit;
	private Button						btnNew;
	private Consumer<MainMenuWindow>	onQuit;
	private Consumer<MainMenuWindow>	onNewGame;

	public MainMenuWindow(SettApp v) {
		super(v, 0);
		setGlass(true);
		setWindowPosition(WindowPosition.CENTRE);
		setBounds(new Rect(0, 0, 180, 150));
	}

	@Override
	public void init() {
		super.init();
		Label l = new Label(this, "Main Menu");
		l.getRenderOptions().setOutlineMode(OutlineMode.OUTLINE);
		l.getRenderOptions().setAlignH(HAlign.CENTRE);
		l.getRenderOptions().setAlignV(VAlign.CENTRE);
		l.getRenderOptions().setOutlineColour(VectorColours.rgba(0, 0, 0, 70));
		l.setColour(VectorColours.parseCSS("#880"));
		items.add(l);
		items.add(btnNew = new Button(this, "new", "New Game"));
		items.add(btnQuit = new Button(this, "quit", "Quit"));
		btnQuit.setButtonColour(VectorColours.rgb(255, 226, 236));
		btnQuit.setOnClick(b -> onQuit.accept(this));
		btnNew.setOnClick(b -> onNewGame.accept(this));
	}

	@Override
	public void screenResize(int w, int h) {
		super.screenResize(w, h);
		int y = 4;
		int x = 0;
		Rect r = getClientBounds();
		for (Component l : items) {
			l.setBounds(tmpR2.set(r.x + (r.w - 80) / 2, y + r.y, 80, 19));
			y += tmpR2.h + 2;
		}
	}

	public void setOnQuit(Consumer<MainMenuWindow> onquit) {
		this.onQuit = onquit;
	}

	public void setOnNewGame(Consumer<MainMenuWindow> onNewGame) {
		this.onNewGame = onNewGame;
	}

}
