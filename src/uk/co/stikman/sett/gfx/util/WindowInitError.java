package uk.co.stikman.sett.gfx.util;

public class WindowInitError extends Exception {

	private static final long serialVersionUID = -2972614080344231384L;

	public WindowInitError(String message, Throwable cause) {
		super(message, cause);
	}

	public WindowInitError(String message) {
		super(message);
	}

	public WindowInitError(Throwable cause) {
		super(cause);
	}
}
