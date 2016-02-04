package org.march.sync.context;

import java.util.LinkedList;

import org.march.sync.Clock;
import org.march.sync.channel.ChangeSet;
import org.march.sync.transform.Transformer;


// TODO: add member uuid as field and check member of message against this field on reception 
public abstract class Backlog {

	private Transformer transformer;
	private int remoteTime;

	private LinkedList<ChangeSet> queue;

	public Backlog(Transformer transformer) {
		this.transformer = transformer;

		this.remoteTime = 0;

		this.queue = new LinkedList<>();
	}

	public ChangeSet update(ChangeSet bucket) throws BacklogException {

		// remove messages member has seen already
		try {
			while (!queue.isEmpty()
					&& !Clock.after(getLocalTime(queue.peek()),
							getLocalTime(bucket))) {
				queue.poll();
			}

			// harmonize remaining messages in buffer and new message at once
			for (ChangeSet enqueued : queue) {
				transformer
						.transform(bucket.getOperations(), enqueued
								.getOperations(), bucket.getReplicaName()
								.compareTo(enqueued.getReplicaName()) > 0);

				// adjust times
				setRemoteTime(enqueued, getRemoteTime(bucket));
				setLocalTime(bucket, getLocalTime(enqueued));
			}

			this.remoteTime = getRemoteTime(bucket); // make sure time is
														// preserved on empty
														// queue

			return bucket;

		} catch (Exception e) {
			throw new BacklogException(e);
		}
	}

	public void append(ChangeSet bucket) throws BacklogException {
		if (getRemoteTime(bucket) != this.remoteTime) {
			throw new BacklogException("Message is out of synchronization.");
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

	protected abstract int getLocalTime(ChangeSet changeSet);

	protected abstract void setLocalTime(ChangeSet changeSet, int time);

	protected abstract int getRemoteTime(ChangeSet changeSet);

	protected abstract void setRemoteTime(ChangeSet changeSet, int time);

}
