package org.march.server.message;

import org.march.sync.channel.ChangeSet;

public class DataMessage extends Message {

	private ChangeSet changeSet;
	
	public DataMessage() {
	}

	public DataMessage(String scope, ChangeSet changeSet) {
		super(scope);

		this.changeSet = changeSet;
	}

	public ChangeSet getChangeSet() {
		return changeSet;
	}

	public void setChangeSet(ChangeSet changeSet) {
		this.changeSet = changeSet;
	}
	
}
