package org.march.server.message;

import java.util.UUID;

public class OpenMessage extends ControlMessage {

	public OpenMessage() {
	}

	public OpenMessage(String scope, UUID member) {
		super(scope, member);
	}

}
