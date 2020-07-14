package uk.co.stikman.sett;

import uk.co.stikman.sett.game.WorldParameters;
import uk.co.stikman.sett.gfx.RectSprite;
import uk.co.stikman.sett.gfx.StretchMode;
import uk.co.stikman.sett.gfx.ui.Button;
import uk.co.stikman.sett.gfx.ui.Label;
import uk.co.stikman.sett.gfx.ui.WindowPosition;
import uk.co.stikman.sett.gfx.util.Rect;
import uk.co.stikman.utils.math.Vector4;

public class NewGameWindow extends SettStandardWindow {

	private int mapSize = 3;

	public NewGameWindow(SettApp v) {
		super(v, 1);
		setCaption("New Game");
		setGlass(true);
		setWindowPosition(WindowPosition.CENTRE);
		setBounds(new Rect(0, 0, 200, 250));

		Label lblSize = new Label(this, "World Size:");
		lblSize.setBounds(new Rect(12, 32, 70, 20));

		Label txtSize = new Label(this, "3");
		txtSize.setBounds(new Rect(104, 32, 40, 20));
		txtSize.setColour(new Vector4(1, 1, 0.5f, 1.0f));

		Button btnSizeUp = new Button(this, "SizeUp", v.getUIResources().getSprite("icon-plus"));
		Button btnSizeDn = new Button(this, "SizeDn", v.getUIResources().getSprite("icon-minus"));
		btnSizeUp.setBounds(new Rect(156, 28, 16, 16));
		btnSizeDn.setBounds(new Rect(140, 28, 16, 16));
		btnSizeUp.setOnClick(e -> {
			++mapSize;
			if (mapSize > 16)
				mapSize = 16;
			txtSize.setCaption(Integer.toString(mapSize));
		});
		btnSizeDn.setOnClick(e -> {
			--mapSize;
			if (mapSize < 2)
				mapSize = 2;
			txtSize.setCaption(Integer.toString(mapSize));
		});

		Button btnCancel = new Button(this, "btnCancel", "Cancel");
		btnCancel.setBounds(new Rect(12, 230 - 12, 60, 20));
		btnCancel.setOnClick(e -> hide());

		Button btnStart = new Button(this, "btnStart", "Start");
		btnStart.setBounds(new Rect(200 - 60 - 12, 230 - 12, 60, 20));
		btnStart.setButtonColour(new Vector4(0f, 1.0f, 0f, 1.0f));
		btnStart.setOnClick(e -> {
			hide();
			v.startNewGame(getParams());
		});

	}

	public WorldParameters getParams() {
		WorldParameters wp = new WorldParameters();
		wp.setSize(mapSize);
		return wp;
	}

	@Override
	public void screenResize(int w, int h) {
		super.screenResize(w, h);
	}

}
