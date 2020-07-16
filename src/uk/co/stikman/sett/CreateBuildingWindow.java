package uk.co.stikman.sett;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import uk.co.stikman.sett.client.renderer.GameView;
import uk.co.stikman.sett.gfx.VectorColours;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.ui.Button;
import uk.co.stikman.sett.gfx.ui.WindowPosition;
import uk.co.stikman.sett.gfx.util.Rect;
import uk.co.stikman.utils.math.Vector4;

public class CreateBuildingWindow extends SettStandardWindow {

	private static final Vector4		COLOUR_SELECTED	= new Vector4(1.3f, 1.3f, 1.3f, 1.0f);
	private Button						btnCancel;
	private Button						btnFlipPage;

	private List<CreateBuildingPage>	pages			= new ArrayList<>();
	private PageBuilding				selected;
	private GameView					gameview;
	private Consumer<PageBuilding>		onSelect;

	public CreateBuildingWindow(SettApp v, GameView gameview, Consumer<PageBuilding> onselect) {
		super(v, 1);
		this.gameview = gameview;
		this.onSelect = onselect;
		setGlass(true);
		setWindowPosition(WindowPosition.CENTRE);
		setBounds(new Rect(0, 0, 144, 160));

		CreateBuildingPage p = new CreateBuildingPage(this);
		p.addBuilding("woodcutter", "nb-woodcutter", 10, 25);
		p.addBuilding("forester", "nb-forester", 40, 25);
		p.addBuilding("weaponsmith", "nb-weaponsmith", 13, 55);
		pages.add(p);
	}

	@Override
	public void init() {
		super.init();
		btnCancel = new Button(this, "cancel", getApp().getUIResources().getSprite("icon-exit"));
		btnFlipPage = new Button(this, "flip", getApp().getUIResources().getSprite("icon-flip"));

		Rect r = getClientBounds();
		btnCancel.setBounds(new Rect(r.x + r.w - 18, r.y + r.h - 18, 18, 18));
		btnFlipPage.setBounds(new Rect(r.x, r.y + r.h - 18, 18, 18));

		btnCancel.setOnClick(b -> hide());
		btnFlipPage.setOnClick(b -> flipPage());
	}

	private void flipPage() {

	}

	@Override
	public void screenResize(int w, int h) {
		super.screenResize(w, h);

	}

	@Override
	public void render() {
		super.render();
		Rect r = getBounds();

		CreateBuildingPage pg = getCurrentPage();

		for (PageBuilding b : pg) {
			Vector4 c = VectorColours.WHITE;
			if (selected == b)
				c = COLOUR_SELECTED;
			getWindow().drawSprite(b.getSprite(), (int) (b.getX() + r.x), (int) (b.getY() + r.y), c, 0, 0);
		}
	}

	private CreateBuildingPage getCurrentPage() {
		return pages.get(0);
	}

	@Override
	public boolean mouseMove(int x, int y) {
		boolean res = super.mouseMove(x, y);
		Rect bounds = getBounds();
		Rect r = new Rect();
		CreateBuildingPage pg = getCurrentPage();
		selected = null;
		for (PageBuilding b : pg) {
			r.set(b.getX(), b.getY(), b.getSprite().getBounds().x, b.getSprite().getBounds().y);
			if (r.contains(x, y))
				selected = b;
		}
		return res;
	}

	@Override
	public boolean mouseDown(int x, int y, int button) {
		boolean res = super.mouseDown(x, y, button);
		if (selected != null)
			onSelect.accept(selected);
		return res;
	}

}
