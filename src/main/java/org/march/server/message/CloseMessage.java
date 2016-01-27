package org.march.server.message;

import java.util.UUID;

public class CloseMessage extends ControlMessage {

	public CloseMessage() {
	}

	public CloseMessage(String scope, UUID member) {
		super(scope, member);
	}

}
