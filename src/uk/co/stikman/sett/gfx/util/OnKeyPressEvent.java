package uk.co.stikman.sett.gfx.util;

public interface OnKeyPressEvent {
	/**
	 * <p>
	 * this is a codepoint. You can usually cast this to a <code>char</code>,
	 * but be aware of unicode issues if you do (eg. Emojis, which are 4byte
	 * UTF-16)
	 * <p>
	 * It does not receive things like the Shift key. Use the OnKeyCodeEvent for
	 * those
	 * 
	 * @param code
	 */
	void keyPress(int code);
}
