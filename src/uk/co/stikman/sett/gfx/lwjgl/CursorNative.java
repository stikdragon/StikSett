package uk.co.stikman.sett.gfx.lwjgl;

import uk.co.stikman.sett.gfx.Cursor;

public class CursorNative extends Cursor {
	private GLFWCursor	cur;
	private GLFWWindow	window;

	public CursorNative(Window3DNative window) {
		super(window);
		this.window = window.getGLFWWindow();
	}

	public void setGLFWObject(GLFWCursor x) {
		this.cur = x;
	}

	public GLFWCursor getGLFWObject() {
		return cur;
	}

	@Override
	public void apply() {
		if (getGLFWObject() == null) {
			GLFWCursor x = window.createCursor(getImage(), getHotspotX(), getHotspotY());
			setGLFWObject(x);
		}
		window.setCursor(getGLFWObject());
	}

}
