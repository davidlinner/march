package org.march.sync.endpoint;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import org.march.sync.Clock;
import org.march.sync.transform.Transformer;


// TODO: add member uuid as field and check member of message against this field on reception 
public abstract class Endpoint implements InboundEndpoint, OutboundEndpoint {

	private Transformer transformer;
	private int remoteTime;

	private LinkedList<Bucket> queue;

	private BucketHandler inboundHandler = null;
	private BucketHandler outboundHandler = null;

	private ReentrantLock lock;
		
	private EndpointState state = EndpointState.INITIALIZED;

	public Endpoint(Transformer transformer, ReentrantLock lock) {
		this.transformer = transformer;
		this.lock = lock;

		this.remoteTime = 0;

		this.queue = new LinkedList<Bucket>();
	}

	public void receive(Bucket bucket) throws EndpointException {
		if (state != EndpointState.OPEN){
			throw new EndpointException(
					String.format("Endpoint has to be open to receive messages. Current state is %s.", state));
		}
		
		lock.lock();

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

			inboundHandler.handle(bucket);

		} catch (Exception e) {
			throw new EndpointException(e);
		} finally {
			lock.unlock();
		}
	}

	public OutboundEndpoint connectOutbound(BucketHandler handler) {

		if (outboundHandler == null){
			this.outboundHandler = handler;						
		}

		return this;
	}

	public OutboundEndpoint disconnectOutbound() {
		this.outboundHandler = null;
		this.state = EndpointState.CLOSED;
		
		return this;
	}

	public void send(Bucket bucket) throws EndpointException {
		
		if(state == EndpointState.CLOSED){
			throw new EndpointException("Endpoint was closed.");
		}
		
		if (getRemoteTime(bucket) != this.remoteTime) {
			throw new EndpointException("Message is out of synchronization.");
		}

		boolean isExclsuive = lock.isHeldByCurrentThread();
		if (!isExclsuive) {
			lock.lock();
		}

		try {
			queue.offer(bucket);
			if (state == EndpointState.OPEN) {
				outboundHandler.handle(bucket);
			}
		} finally {
			if (!isExclsuive) {
				lock.unlock();
			}
		}
	}

	public InboundEndpoint connectInbound(BucketHandler handler) {
		if (handler != null && inboundHandler == null) {
			inboundHandler = handler;
		}

		return this;
	}

	public InboundEndpoint disconnectInbound() {
		this.inboundHandler = null;
		this.state = EndpointState.CLOSED;
		return this;
	}

	public int getRemoteTime() {
		return this.remoteTime;
	}

	public void open() throws EndpointException {
		
		if(state == EndpointState.OPEN) {
			throw new EndpointException("Endpoint already opened.");
		};
		
		if(!isConnected()){
			throw new EndpointException("Cannot open endpoint. Message handler missing on in- or outbound channel.");
		}
		
		this.state = EndpointState.OPEN;
		
		boolean isExclusive = lock.isHeldByCurrentThread();
		if (!isExclusive) {
			lock.lock();
		}

		try {
			for (Bucket bucket : queue) {
				outboundHandler.handle(bucket);
			}
		} finally {
			if (!isExclusive) {
				lock.unlock();
			}
		}		
	}	
		
	public boolean isConnected() {
		return this.outboundHandler != null &&
				this.inboundHandler != null;
	}

	protected abstract int getLocalTime(Bucket bucket);

	protected abstract void setLocalTime(Bucket bucket, int time);

	protected abstract int getRemoteTime(Bucket bucket);

	protected abstract void setRemoteTime(Bucket bucket, int time);

}
