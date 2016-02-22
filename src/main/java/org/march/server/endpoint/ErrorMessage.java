package org.march.server.endpoint;

import org.march.sync.endpoint.ChangeSet;

import java.util.UUID;

public class ErrorMessage extends Message {

    private String reason;

    private ErrorCode code;

	public ErrorMessage() {
	}

    public ErrorMessage(UUID replicaName, String reason, ErrorCode code) {
        super(replicaName);
        this.reason = reason;
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }


}
