package org.march.sync;

public class LeaderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8250120917847968738L;

	public LeaderException() {
	}

	public LeaderException(String message) {
		super(message);
	}

	public LeaderException(Throwable cause) {
		super(cause);
	}

	public LeaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public LeaderException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
