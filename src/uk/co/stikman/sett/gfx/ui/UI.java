package uk.co.stikman.sett.gfx.ui;

import uk.co.stikman.sett.gfx.lwjgl.Window3DNative;
import uk.co.stikman.sett.gfx.util.Rect;

public class UI {
	private WindowList	windows		= new WindowList(this);
	private Window3DNative	owner;
	private Rect		uiBounds	= new Rect();

	public UI(Window3DNative owner) {
		this.owner = owner;
	}

	public void windowListChanged(WindowList list) {
	}

	public Window3DNative getWindow3D() {
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

	public void handleMouseMove(int x, int y) {
		getWindows().forEach(w -> w.mouseMove(x, y));
	}

	public void handleMouseDown(int x, int y, int button) {
		getWindows().forEach(w -> w.mouseDown(x, y, button));
	}

	public void handleMouseUp(int x, int y, int button) {
		getWindows().forEach(w -> w.mouseUp(x, y, button));
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
