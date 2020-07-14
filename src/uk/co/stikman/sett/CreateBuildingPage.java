package uk.co.stikman.sett;

import java.util.ArrayList;
import java.util.List;

import uk.co.stikman.sett.gfx.Sprite;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.util.Rect;

public class CreateBuildingPage {

	private static class Building {
		int		x;
		int		y;
		Sprite	sprite;
	}

	private List<Building>			list	= new ArrayList<>();
	private CreateBuildingWindow	wnd;

	public CreateBuildingPage(CreateBuildingWindow wnd) {
		this.wnd = wnd;
	}

	public void addBuilding(String sprite, int x, int y) {
		Building b = new Building();
		b.x = x;
		b.y = y;
		b.sprite = wnd.getApp().getUIResources().findSprite(sprite);
		list.add(b);
	}

	public void renderIn(Window3D window, Rect bounds) {
		for (Building b : list) 
			window.drawSprite(b.sprite, (int) (b.x + bounds.x), (int) (b.y + bounds.y));
	}

}
