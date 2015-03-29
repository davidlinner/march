package org.march.sync;

import java.util.LinkedList;

import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.Message;
import org.march.sync.endpoint.MessageHandler;
import org.march.sync.endpoint.OutboundEndpoint;

public class Pipe {

    private final LinkedList<Message> buffer = new LinkedList<Message>();
    
    private OutboundEndpoint source, destination;
    
    private MessageHandler handler;
    
    public Pipe(OutboundEndpoint source, OutboundEndpoint destination) {
        this.source         = source;
        this.destination    = destination;
    }
    
    public void open(){
        buffer.clear();
        
        this.handler = new MessageHandler() {            
            @Override
            public void handle(Message message) {
                buffer.add(message);
            }
        };
        
        source.onOutbound(this.handler);
    }
    
    public void close() throws EndpointException{
        this.flush();
        
        source.offOutbound(this.handler);        
        
        this.handler = null;
    }

    public void flush() throws EndpointException{
        for(Message message: buffer){
            destination.receive(message);
        }
    }
}
