package org.march.sync.backlog;

import java.util.LinkedList;

import org.march.sync.Clock;
import org.march.sync.channel.ChangeSet;
import org.march.sync.transform.Transformer;


// TODO: add member uuid as field and check member of channel against this field on reception
public abstract class Backlog {

	private Transformer transformer;

    private int remoteTime = Clock.getClockStart();

	private LinkedList<ChangeSet> queue;

	public Backlog(Transformer transformer) {
		this.transformer = transformer;
		this.queue = new LinkedList<>();
	}

	public ChangeSet update(ChangeSet changeSet) throws BacklogException {

		// remove messages member has seen already
		try {
			while (!queue.isEmpty()
					&& !Clock.after(getLocalTime(queue.peek()),
							getLocalTime(changeSet))) {
				queue.poll();
			}

			// harmonize remaining messages in buffer and new channel at once
			for (ChangeSet enqueued : queue) {
				transformer
						.transform(changeSet.getOperations(), enqueued
								.getOperations(), changeSet.getOriginReplicaName()
								.compareTo(enqueued.getOriginReplicaName()) > 0);

				// adjust times
				setRemoteTime(enqueued, getRemoteTime(changeSet));
			}

			this.remoteTime = getRemoteTime(changeSet); // make sure time is
														// preserved on empty
														// queue

			return changeSet;

		} catch (Exception e) {
			throw new BacklogException(e);
		}
	}

	public void append(ChangeSet changeSet) throws BacklogException {
		if (getRemoteTime(changeSet) != this.remoteTime) {
			throw new BacklogException("Message is out of synchronization.");
		}

		queue.offer(changeSet.clone());
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
