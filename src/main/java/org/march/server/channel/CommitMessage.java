package org.march.server.channel;

import org.march.sync.channel.ChangeSet;

import java.util.UUID;

public class CommitMessage extends Message {

	private ChangeSet changeSet;

    private String scope;

	public CommitMessage() {
	}

    public CommitMessage(String scope, UUID replicaName, ChangeSet changeSet) {
        super(replicaName);
        this.scope = scope;
        this.changeSet = changeSet;
    }

    public ChangeSet getChangeSet() {
		return changeSet;
	}

	public void setChangeSet(ChangeSet changeSet) {
		this.changeSet = changeSet;
	}

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
