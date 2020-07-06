package uk.co.stikman.sett.svr;

public class ServerException extends Exception {
	private final int			code;
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public static final int		UNSPECIFIED			= -1;
	public static final int		INVALID_VERSION		= 1;
	public static final int		LOGIN_FAILED		= 2;
	public static final int		UNAUTHORISED		= 3;

	public ServerException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public ServerException(int code, String message) {
		super(message);
		this.code = code;
	}

	public ServerException(int code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	@Override
	public String toString() {
		return code + ": " + getMessage();
	}

}
