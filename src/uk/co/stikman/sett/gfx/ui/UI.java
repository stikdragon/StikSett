package uk.co.stikman.sett.gfx.ui;

import java.util.LinkedList;
import java.util.List;

import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.util.Rect;

public class UI {
	private WindowList		windows		= new WindowList(this);
	private Window3D		owner;
	private Rect			uiBounds	= new Rect();
	private SimpleWindow	activeWindow;
	private List<UITimer>	timers		= new LinkedList<>();

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
		for (UITimer t : timers) {
			if (t.update(dt))
				t.trigger();
		}

		getWindows().forEach(sw -> sw.update(dt));
	}

	public boolean handleKeyPress(char ch) {
		SimpleWindow wnd = getActiveWindow();
		if (wnd != null)
			if (wnd.keyPress(ch))
				return true;
		return false;
	}

	public boolean handleKeyCode(int keycode, boolean down, int mods) {
		SimpleWindow wnd = getActiveWindow();
		if (wnd != null)
			if (wnd.keyCode(keycode, down, mods))
				return true;
		return false;
	}

	private SimpleWindow getActiveWindow() {
		return activeWindow;
	}

	public boolean handleMouseMove(int x, int y) {
		for (SimpleWindow w : getWindows().reverse())
			if (w.mouseMove(x - (int) w.getBounds().x, y - (int) w.getBounds().y))
				return true;
		return false;
	}

	public boolean handleMouseDown(int x, int y, int button) {
		for (SimpleWindow w : getWindows().reverse())
			if (w.mouseDown(x - (int) w.getBounds().x, y - (int) w.getBounds().y, button))
				return true;
		return false;
	}

	public boolean handleMouseUp(int x, int y, int button) {
		for (SimpleWindow w : getWindows().reverse())
			if (w.mouseUp(x - (int) w.getBounds().x, y - (int) w.getBounds().y, button))
				return true;
		return false;
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

	public void setActiveWindow(SimpleWindow activeWindow) {
		this.activeWindow = activeWindow;
	}

	public void bringWindowToFront(SimpleWindow window) {
		getWindows().remove(window);
		getWindows().add(window);
	}

	/**
	 * interval in ms
	 * 
	 * @param interval
	 * @return
	 */
	public UITimer newTimer(int interval, Runnable event) {
		UITimer t = new UITimer(this, interval, event);
		timers.add(t);
		return t;
	}

	void cancelTimer(UITimer t) {
		timers.remove(t);
	}

}
