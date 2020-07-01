package uk.co.stikman.sett.gfx.util;

public class GLException extends RuntimeException {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 3698011889726269964L;

	public GLException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public GLException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public GLException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public GLException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public GLException(int glCode, String string) {
		this("(" + glCode + ") " + string);
	}

}
