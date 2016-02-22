package org.march.server.endpoint;

import java.util.UUID;

public class ReplicateMessage extends Message {

    private String scope;

    public ReplicateMessage() {
	}

    public ReplicateMessage(String scope, UUID replicaName) {
        super(replicaName);
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
