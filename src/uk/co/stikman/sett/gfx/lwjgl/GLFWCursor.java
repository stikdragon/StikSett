package uk.co.stikman.sett.gfx.lwjgl;

public class GLFWCursor {

	private long	handle;
	private int		hotY;
	private int		hotX;

	public void setHotSpot(int hotx, int hoty) {
		this.hotX = hotx;
		this.hotY = hoty;
	}

	public long getHandle() {
		return handle;
	}

	public int getHotY() {
		return hotY;
	}

	public int getHotX() {
		return hotX;
	}

	public void setHandle(long h) {
		this.handle = h;
	}

}
