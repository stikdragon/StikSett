package uk.co.stikman.sett.gfx.ui;

import java.awt.Window;

import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.util.Rect;

public class UI {
	private WindowList	windows		= new WindowList(this);
	private Window3D	owner;
	private Rect		uiBounds	= new Rect();

	public UI(Window3D owner) {
		this.owner = owner;
	}

	public void windowListChanged(WindowList list) {
	}

	public Window3D getWindow3D() {
		return owner;
	}

	public void relayoutWindow(SimpleWindow wnd) {
		wnd.screenResize((int) uiBounds.w, (int) uiBounds.h);
	}

	public WindowList getWindows() {
		return windows;
	}

	public void render() {
		getWindows().forEach(SimpleWindow::render);
	}

	public void update(float dt) {
		getWindows().forEach(sw -> sw.update(dt));
	}

	public boolean handleKeyPress(char ch) {
		for (SimpleWindow sw : getWindows())
			if (sw.keyPress(ch))
				return true;
		return false;
	}

	public boolean handleKeyCode(int keycode, boolean down, int mods) {
		for (SimpleWindow sw : getWindows())
			if (sw.keyCode(keycode, down, mods))
				return true; // something handled it
		return false;
	}

	public boolean handleMouseMove(int x, int y) {
		boolean b = false;
		for (SimpleWindow w : getWindows())
			b |= w.mouseMove(x - (int) w.getBounds().x, y - (int) w.getBounds().y);
		return b;
	}

	public boolean handleMouseDown(int x, int y, int button) {
		boolean b = false;
		for (SimpleWindow w : getWindows()) {
			b |= w.mouseDown(x - (int) w.getBounds().x, y - (int) w.getBounds().y, button);
		}
		return b;
	}

	public boolean handleMouseUp(int x, int y, int button) {
		boolean b = false;
		for (SimpleWindow w : getWindows())
			b |= w.mouseUp(x - (int) w.getBounds().x, y - (int) w.getBounds().y, button);
		return b;
	}

	public void handleResize(int w, int h) {
		uiBounds.set(0, 0, w, h);
		for (SimpleWindow a : getWindows())
			a.screenResize(w, h);
	}

	public Rect getUIBounds() {
		return uiBounds;
	}

	public void removeWindow(SimpleWindow wnd) {
		getWindows().remove(wnd);
	}

}
