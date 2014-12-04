package org.march.sync.channel;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import org.march.sync.Clock;
import org.march.sync.transform.Transformer;

public abstract class TransformingChannel implements InboundChannel, OutboundChannel {
  
    private Transformer transformer;
    private int remoteTime;
    
    private LinkedList<Message> queue; 
    
    private LinkedList<MessageHandler> inboundHandlers;
    private LinkedList<MessageHandler> outboundHandlers;
    
    private ReentrantLock lock;
    
    public TransformingChannel(Transformer transformer, ReentrantLock lock) {
        this.transformer    = transformer;
        this.lock = lock;
        
        this.remoteTime    = 0;
        
        this.queue = new LinkedList<Message>();
        
        this.inboundHandlers = new LinkedList<MessageHandler>();
        this.outboundHandlers = new LinkedList<MessageHandler>();
    }

    
    public TransformingChannel(Transformer transformer) {
        this(transformer, new ReentrantLock());
    }

    public void receive(Message message) throws ChannelException {
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
            throw new ChannelException(e);
        } finally {
            lock.unlock();            
        }                
    }

    public OutboundChannel onOutbound(MessageHandler handler) {
        if(handler != null && !outboundHandlers.contains(handler)){
            outboundHandlers.add(handler);
        }
        
        return this;
    }
    
    public OutboundChannel offOutbound(MessageHandler handler) {
        outboundHandlers.remove(handler);
        return this;
    }
    
    public OutboundChannel offOutbound() {
        outboundHandlers.clear();
        return this;
    }

    public void send(Message message) throws ChannelException {
        if(getRemoteTime(message) != this.remoteTime) {
            throw new ChannelException("Message is out of synchronization.");
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

    public InboundChannel onInbound(MessageHandler handler) {
        if(handler != null && !inboundHandlers.contains(handler)){
            inboundHandlers.add(handler);
        }
        
        return this;
    }
    
    public InboundChannel offInbound(MessageHandler handler) {
        inboundHandlers.remove(handler);
        return this;
    }

    public InboundChannel offInbound() {
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
