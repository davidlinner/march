package org.march.server.message;

import java.util.UUID;

public abstract class ControlMessage extends Message {

	private UUID member;
		
	public ControlMessage() {
	}

	public ControlMessage(String scope, UUID member) {
		super(scope);
		
		this.member = member;
	}

	public UUID getMember() {
		return member;
	}

	public void setMember(UUID member) {
		this.member = member;
	}
}
