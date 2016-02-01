package org.march.sync.endpoint;

import java.util.UUID;

import org.march.data.Operation;

import org.march.data.Tools;

public class UpdateBucket extends Bucket{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2860862372297426352L;

	public UpdateBucket() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UpdateBucket(UUID member, int memberTime, int leaderTime,
			Operation[] operations) {
		super(member, memberTime, leaderTime, operations);
	}

	@Override
	public UpdateBucket clone() {
		return new UpdateBucket(this.getMember(), this.getMemberTime(), this.getLeaderTime(), Tools.clone(this.getOperations()));
	}
}
