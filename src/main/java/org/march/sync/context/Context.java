package org.march.sync.context;

import java.util.LinkedList;

import org.march.sync.Clock;
import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.UpdateBucket;
import org.march.sync.transform.Transformer;


// TODO: add member uuid as field and check member of message against this field on reception 
public abstract class Context {

	private Transformer transformer;
	private int remoteTime;

	private LinkedList<UpdateBucket> queue;

	public Context(Transformer transformer) {
		this.transformer = transformer;

		this.remoteTime = 0;

		this.queue = new LinkedList<UpdateBucket>();
	}

	public UpdateBucket adapt(UpdateBucket bucket) throws ContextException {

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
			throw new ContextException(e);
		}
	}

	public void include(UpdateBucket bucket) throws ContextException {
		if (getRemoteTime(bucket) != this.remoteTime) {
			throw new ContextException("Message is out of synchronization.");
		}

		queue.offer(bucket.clone());
	}

	public boolean isEmpty(){
		return queue.isEmpty();
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
