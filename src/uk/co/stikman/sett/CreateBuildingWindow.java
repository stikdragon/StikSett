package uk.co.stikman.sett;

import java.util.ArrayList;
import java.util.List;

import uk.co.stikman.sett.client.renderer.GameView;
import uk.co.stikman.sett.gfx.ui.Button;
import uk.co.stikman.sett.gfx.ui.WindowPosition;
import uk.co.stikman.sett.gfx.util.Rect;

public class CreateBuildingWindow extends SettStandardWindow {

	private Button						btnCancel;
	private Button						btnFlipPage;

	private List<CreateBuildingPage>	pages	= new ArrayList<>();


	public CreateBuildingWindow(SettApp v, GameView gameview) {
		super(v);
		setGlass(true);
		setWindowPosition(WindowPosition.CENTRE);
		setBounds(new Rect(0, 0, 144, 160));
		
		CreateBuildingPage p = new CreateBuildingPage(this);
		p.addBuilding("nb-woodcutter", 10, 40);
		p.addBuilding("nb-forester", 50, 40);
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
		pg.renderIn(getWindow(), r);
	}

	private CreateBuildingPage getCurrentPage() {
		return pages.get(0);
	}

}
