package org.march.sync.master;

public class MasterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8250120917847968738L;

	public MasterException() {
	}

	public MasterException(String message) {
		super(message);
	}

	public MasterException(Throwable cause) {
		super(cause);
	}

	public MasterException(String message, Throwable cause) {
		super(message, cause);
	}

	public MasterException(String message, Throwable cause,
						   boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
