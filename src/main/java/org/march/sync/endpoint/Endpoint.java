package org.march.sync.endpoint;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import org.march.sync.Clock;
import org.march.sync.transform.Transformer;


// TODO: add member uuid as field and check member of message against this field on reception 
public abstract class Endpoint {

	private Transformer transformer;
	private int remoteTime;

	private LinkedList<UpdateBucket> queue;

	private BucketHandler inboundHandler = null;
	private BucketHandler outboundHandler = null;


	public Endpoint(Transformer transformer) {
		this.transformer = transformer;

		this.remoteTime = 0;

		this.queue = new LinkedList<UpdateBucket>();
	}

	public UpdateBucket receive(UpdateBucket bucket) throws EndpointException {

		// remove messages member has seen already
		try {
			while (!queue.isEmpty()
					&& !Clock.after(getLocalTime(queue.peek()),
							getLocalTime(bucket))) {
				queue.poll();
			}

			// harmonize remaining messages in buffer and new message at once
			for (Bucket enqueued : queue) {
				transformer
						.transform(bucket.getOperations(), enqueued
								.getOperations(), bucket.getMember()
								.compareTo(enqueued.getMember()) > 0);

				// adjust times
				setRemoteTime(enqueued, getRemoteTime(bucket));
				setLocalTime(bucket, getLocalTime(enqueued));
			}

			this.remoteTime = getRemoteTime(bucket); // make sure time is
														// preserved on empty
														// queue

			return bucket;

		} catch (Exception e) {
			throw new EndpointException(e);
		}
	}

	public UpdateBucket send(UpdateBucket bucket) throws EndpointException {
		
		if (getRemoteTime(bucket) != this.remoteTime) {
			throw new EndpointException("Message is out of synchronization.");
		}

		queue.offer(bucket.clone());

		return bucket;
	}

	public int getRemoteTime() {
		return this.remoteTime;
	}

	public void setRemoteTime(int remoteTime) {
		this.remoteTime = remoteTime;
	}

	protected abstract int getLocalTime(Bucket bucket);

	protected abstract void setLocalTime(Bucket bucket, int time);

	protected abstract int getRemoteTime(Bucket bucket);

	protected abstract void setRemoteTime(Bucket bucket, int time);

}
