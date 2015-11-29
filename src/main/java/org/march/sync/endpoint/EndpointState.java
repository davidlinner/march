package org.march.sync.endpoint;

public enum EndpointState {
	INITIALIZED,
	OPEN,
	CLOSED;
	
	public static boolean isReady(EndpointState state){
		return state == OPEN || state == INITIALIZED;
	}
}
