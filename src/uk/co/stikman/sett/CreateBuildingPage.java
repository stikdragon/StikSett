package uk.co.stikman.sett;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CreateBuildingPage implements Iterable<PageBuilding> {

	private List<PageBuilding>		list	= new ArrayList<>();
	private CreateBuildingWindow	wnd;

	public CreateBuildingPage(CreateBuildingWindow wnd) {
		this.wnd = wnd;
	}

	public void addBuilding(String name, String sprite, int x, int y) {
		PageBuilding b = new PageBuilding(name, x, y, wnd.getApp().getUIResources().findSprite(sprite));
		list.add(b);
	}

	@Override
	public Iterator<PageBuilding> iterator() {
		return list.iterator();

	}

}
