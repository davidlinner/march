package org.march.sync;

import java.util.LinkedList;

import org.march.sync.channel.ChannelException;
import org.march.sync.channel.Message;
import org.march.sync.channel.MessageHandler;
import org.march.sync.channel.OutboundChannel;

public class Pipe {

    private final LinkedList<Message> buffer = new LinkedList<Message>();
    
    private OutboundChannel source, destination;
    
    private MessageHandler handler;
    
    public Pipe(OutboundChannel source, OutboundChannel destination) {
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
    
    public void close() throws ChannelException{
        this.flush();
        
        source.offOutbound(this.handler);        
        
        this.handler = null;
    }

    public void flush() throws ChannelException{
        for(Message message: buffer){
            destination.receive(message);
        }
    }
}
