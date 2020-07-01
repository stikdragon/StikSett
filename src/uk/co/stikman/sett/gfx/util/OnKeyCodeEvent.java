package uk.co.stikman.sett.gfx.util;

public interface OnKeyCodeEvent {
	/**
	 * <code>code</code> is one of the GLFW named keys. <code>down</code> is
	 * <code>true</code> when the key is pressed, <code>false</code> when it's
	 * released. <code>mods</code> is a set of GLFW.GLFW_MOD_* values
	 * 
	 * @param code
	 * @param down
	 * @param mods
	 */
	void keyCode(int code, boolean down, int mods);
}
