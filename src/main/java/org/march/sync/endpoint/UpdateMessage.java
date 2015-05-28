package org.march.sync.endpoint;

import java.util.UUID;

import org.march.data.Operation;

public class UpdateMessage extends Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2860862372297426352L;

	public UpdateMessage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UpdateMessage(UUID member, int memberTime, int leaderTime,
			Operation[] operations) {
		super(member, memberTime, leaderTime, operations);
	}
	
}
