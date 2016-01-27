package org.march.server.message;

import org.march.sync.endpoint.Bucket;

public class DataMessage extends Message {

	private Bucket bucket;
	
	public DataMessage() {
	}

	public DataMessage(String scope, Bucket bucket) {
		super(scope);

		this.bucket = bucket;
	}

	public Bucket getBucket() {
		return bucket;
	}

	public void setBucket(Bucket bucket) {
		this.bucket = bucket;
	}
	
}
