package org.march.sync.endpoint;

import java.util.UUID;

import org.march.data.Operation;

public class SynchronizationBucket extends Bucket {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8660716465237997248L;
	public SynchronizationBucket() {
		super();
	}

	public SynchronizationBucket(UUID member, int memberTime, int leaderTime,
			Operation[] operations) {
		super(member, memberTime, leaderTime, operations);
	}

}
