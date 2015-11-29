package org.march.sync.endpoint;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import org.march.sync.Clock;
import org.march.sync.transform.Transformer;


// TODO: add member uuid as field and check member of message against this field on reception 
public abstract class Endpoint implements InboundEndpoint, OutboundEndpoint {

	private Transformer transformer;
	private int remoteTime;

	private LinkedList<Message> queue;

	private MessageHandler inboundHandler = null;
	private MessageHandler outboundHandler = null;

	private ReentrantLock lock;
		
	private EndpointState state = EndpointState.INITIALIZED;

	public Endpoint(Transformer transformer, ReentrantLock lock) {
		this.transformer = transformer;
		this.lock = lock;

		this.remoteTime = 0;

		this.queue = new LinkedList<Message>();
	}

	public void receive(Message message) throws EndpointException {
		if (state != EndpointState.OPEN){
			throw new EndpointException(
					String.format("Endpoint has to be open to receive messages. Current state is %s.", state));
		}
		
		lock.lock();

		// remove messages member has seen already
		try {
			while (!queue.isEmpty()
					&& !Clock.after(getLocalTime(queue.peek()),
							getLocalTime(message))) {
				queue.poll();
			}

			// harmonize remaining messages in buffer and new message at once
			for (Message enqueued : queue) {
				transformer
						.transform(message.getOperations(), enqueued
								.getOperations(), message.getMember()
								.compareTo(enqueued.getMember()) > 0);

				// adjust times
				setRemoteTime(enqueued, getRemoteTime(message));
				setLocalTime(message, getLocalTime(enqueued));
			}

			this.remoteTime = getRemoteTime(message); // make sure time is
														// preserved on empty
														// queue

			inboundHandler.handle(message);

		} catch (Exception e) {
			throw new EndpointException(e);
		} finally {
			lock.unlock();
		}
	}

	public OutboundEndpoint connectOutbound(MessageHandler handler) {

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

	public void send(Message message) throws EndpointException {
		
		if(state == EndpointState.CLOSED){
			throw new EndpointException("Endpoint was closed.");
		}
		
		if (getRemoteTime(message) != this.remoteTime) {
			throw new EndpointException("Message is out of synchronization.");
		}

		boolean isExclsuive = lock.isHeldByCurrentThread();
		if (!isExclsuive) {
			lock.lock();
		}

		try {
			queue.offer(message);
			if (state == EndpointState.OPEN) {
				outboundHandler.handle(message);
			}
		} finally {
			if (!isExclsuive) {
				lock.unlock();
			}
		}
	}

	public InboundEndpoint connectInbound(MessageHandler handler) {
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
			for (Message message : queue) {
				outboundHandler.handle(message);
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

	protected abstract int getLocalTime(Message message);

	protected abstract void setLocalTime(Message message, int time);

	protected abstract int getRemoteTime(Message message);

	protected abstract void setRemoteTime(Message message, int time);

}
