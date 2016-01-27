package org.march.server.message;

public abstract class Message {

	private String scope;
	
	public Message() {		
	}

	public Message(String scope) {
		super();
		this.scope = scope;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	
}
