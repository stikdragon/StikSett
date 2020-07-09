package uk.co.stikman.sett;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import uk.co.stikman.sett.gfx.RectSprite;
import uk.co.stikman.sett.gfx.StretchMode;
import uk.co.stikman.sett.gfx.ui.Button;
import uk.co.stikman.sett.gfx.ui.Component;
import uk.co.stikman.sett.gfx.ui.Label;
import uk.co.stikman.sett.gfx.ui.WindowPosition;
import uk.co.stikman.sett.gfx.util.Rect;

public class NewGameWindow extends SettStandardWindow {

	private final List<Component>		items	= new ArrayList<>();
	private final Rect					tmpR	= new Rect();
	private final Rect					tmpR2	= new Rect();
	private Button						btnQuit;
	private Button						btnNew;
	private Consumer<MainMenuWindow>	onQuit;
	private Consumer<MainMenuWindow>	onNewGame;

	public NewGameWindow(SettApp v) {
		super(v);
		getTheming().setBackgroundSprite((RectSprite) v.getUIResources().findSprite("dlgbox4"), StretchMode.createSmart(8, 8, 9, 7));
		setCaption("New Game");
		setGlass(true);
		setWindowPosition(WindowPosition.CENTRE);
		setBounds(new Rect(0, 0, 200, 250));
		
		
		Label lblSize = new Label(this, "World Size:");
		lblSize.setBounds(new Rect(8, 0, 70, 20));
		
		Label txtSize = new Label(this, "5");
		txtSize.setBounds(new Rect(78, 0, 40, 20));
		
		Button btnSizeUp = new Button(this, "SizeUp", v.getUIResources().getSprite("icon-plus"));
		Button btnSizeDn = new Button(this, "SizeDn", v.getUIResources().getSprite("icon-minus"));
		btnSizeUp.setBounds(new Rect(140, 0, 16, 16));
		btnSizeDn.setBounds(new Rect(156, 0, 16, 16));
		
	}
	
	@Override
	public void screenResize(int w, int h) {
		super.screenResize(w, h);
	}

	
}
