package uk.co.stikman.sett.gfx.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WindowList implements Iterable<SimpleWindow> {

	private List<SimpleWindow>	list	= new ArrayList<>();
	private UI					ui;

	public WindowList(UI ui) {
		this.ui = ui;
	}

	@Override
	public Iterator<SimpleWindow> iterator() {
		return new ArrayList<>(list).iterator();
	}

	public void add(SimpleWindow w) {
		list.add(w);
		changed();
	}

	private void changed() {
		if (ui != null)
			ui.windowListChanged(this);
	}

	public void remove(SimpleWindow wnd) {
		list.remove(wnd);
	}
}
