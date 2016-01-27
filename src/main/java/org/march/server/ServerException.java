package org.march.server;

public class ServerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6303459453305021921L;

	public ServerException() {
	}

	public ServerException(String arg0) {
		super(arg0);
	}

	public ServerException(Throwable arg0) {
		super(arg0);
	}

	public ServerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ServerException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
