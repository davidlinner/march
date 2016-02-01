package org.march.sync.endpoint;

import java.util.UUID;

import org.march.data.Operation;

public class BaseBucket extends Bucket {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8660716465237997248L;
	public BaseBucket() {
		super();
	}

	public BaseBucket(UUID member, int memberTime, int leaderTime,
					  Operation[] operations) {
		super(member, memberTime, leaderTime, operations);
	}
}
