package org.march.server.endpoint;

import java.util.UUID;

public abstract class Message {

	private UUID replicaName;

	public Message() {		
	}

    protected Message(UUID replicaName) {
        this.replicaName = replicaName;
    }

    public UUID getReplicaName() {
        return replicaName;
    }

    public void setReplicaName(UUID replicaName) {
        this.replicaName = replicaName;
    }

}
