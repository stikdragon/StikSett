package uk.co.stikman.sett.gfx.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class WindowList implements Iterable<SimpleWindow> {

	private List<SimpleWindow>	list	= new ArrayList<>();
	private UI					ui;
	private List<SimpleWindow>	rev		= null;

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
		rev = null;
		if (ui != null)
			ui.windowListChanged(this);
	}

	public void remove(SimpleWindow wnd) {
		list.remove(wnd);
	}

	public Iterable<SimpleWindow> reverse() {
		if (rev == null) {
			rev = new ArrayList<>(list);
			Collections.reverse(rev);
		}
		return rev;
	}
}
