package org.march.sync.endpoint;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import org.march.sync.Clock;
import org.march.sync.transform.Transformer;

public abstract class Endpoint implements InboundEndpoint, OutboundEndpoint {
  
    private Transformer transformer;
    private int remoteTime;
    
    private LinkedList<Message> queue; 
    
    private LinkedList<MessageHandler> inboundHandlers;
    private LinkedList<MessageHandler> outboundHandlers;
    
    private ReentrantLock lock;
    
    public Endpoint(Transformer transformer, ReentrantLock lock) {
        this.transformer    = transformer;
        this.lock = lock;
        
        this.remoteTime    = 0;
        
        this.queue = new LinkedList<Message>();
        
        this.inboundHandlers = new LinkedList<MessageHandler>();
        this.outboundHandlers = new LinkedList<MessageHandler>();
    }

    
    public Endpoint(Transformer transformer) {
        this(transformer, new ReentrantLock());
    }

    public void receive(Message message) throws EndpointException {
       lock.lock();
        
        // remove messages member has seen already
        try {
            while(!queue.isEmpty() && !Clock.after(getLocalTime(queue.peek()), getLocalTime(message))){
                queue.poll();
            }
            
            // harmonize remaining messages in buffer and new message at once
            for(Message enqueued: queue){            
                transformer.transform(message.getOperations(), enqueued.getOperations(), message.getMember().compareTo(enqueued.getMember()) > 0);
                
                // adjust times
                setRemoteTime(enqueued, getRemoteTime(message));
                setLocalTime(message, getLocalTime(enqueued));
            }
            
            this.remoteTime = getRemoteTime(message); // make sure time is preserved on empty queue            
           
            for(MessageHandler handler: inboundHandlers){
                handler.handle(message);
            }
            
        } catch (Exception e) {
            throw new EndpointException(e);
        } finally {
            lock.unlock();            
        }                
    }

    public OutboundEndpoint onOutbound(MessageHandler handler) {
        if(handler != null && !outboundHandlers.contains(handler)){
            outboundHandlers.add(handler);
        }
        
        return this;
    }
    
    public OutboundEndpoint offOutbound(MessageHandler handler) {
        outboundHandlers.remove(handler);
        return this;
    }
    
    public OutboundEndpoint offOutbound() {
        outboundHandlers.clear();
        return this;
    }

    public void send(Message message) throws EndpointException {
        if(getRemoteTime(message) != this.remoteTime) {
            throw new EndpointException("Message is out of synchronization.");
        }
        
        boolean isExclsuive = lock.isHeldByCurrentThread();        
        if(!isExclsuive){ 
            lock.lock();
        }
        
        try {
            queue.offer(message);            
            for(MessageHandler handler: outboundHandlers){
                handler.handle(message);
            }    
        } finally {
            if(!isExclsuive){
                lock.unlock();
            }
        }
    }

    public InboundEndpoint onInbound(MessageHandler handler) {
        if(handler != null && !inboundHandlers.contains(handler)){
            inboundHandlers.add(handler);
        }
        
        return this;
    }
    
    public InboundEndpoint offInbound(MessageHandler handler) {
        inboundHandlers.remove(handler);
        return this;
    }

    public InboundEndpoint offInbound() {
        inboundHandlers.clear();
        return this;
    }

    public int getRemoteTime(){
        return this.remoteTime;
    }    

    protected abstract int getLocalTime(Message message);
    protected abstract void setLocalTime(Message message, int time);
    
    protected abstract int getRemoteTime(Message message);
    protected abstract void setRemoteTime(Message message, int time);   

}
