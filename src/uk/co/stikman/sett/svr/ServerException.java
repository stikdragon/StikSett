package uk.co.stikman.sett.svr;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ServerException extends Exception {
	private final int			code;
	/**
	 * 
	 */
	private static final long	serialVersionUID			= 1L;

	public static final int		E_UNSPECIFIED				= -1;
	public static final int		E_INVALID_VERSION			= 1;
	public static final int		E_LOGIN_FAILED				= 2;
	public static final int		E_UNAUTHORISED				= 3;
	public static final int		E_INVALID_BUILD_LOCATION	= 4;

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
		return describeCode(code) + ": " + getMessage();
	}

	public static String describeCode(int code) {
		for (Field f : ServerException.class.getDeclaredFields()) {
			if (Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) {
				if (f.getType().equals(int.class)) {
					if (f.getName().startsWith("E_")) {
						try {
							int i = ((Integer) f.get(null)).intValue();
							if (i == code)
								return code + " (" + f.getName().substring(2) + ")";
						} catch (IllegalArgumentException | IllegalAccessException e) {
						}
					}
				}
			}
		}
		return code + "(Unknown)";
	}

}
