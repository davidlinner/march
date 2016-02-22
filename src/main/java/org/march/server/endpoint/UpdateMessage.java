package org.march.server.endpoint;

import org.march.sync.endpoint.ChangeSet;

import java.util.UUID;

public class UpdateMessage extends Message {

	private ChangeSet changeSet;

    private String scope;

	public UpdateMessage() {
	}

    public UpdateMessage(String scope, UUID replicaName, ChangeSet changeSet) {
        super(replicaName);
        this.changeSet = changeSet;
        this.scope = scope;
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
