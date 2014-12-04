package org.march.demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.march.sync.channel.ChannelException;
import org.march.sync.channel.Message;
import org.march.sync.channel.MessageHandler;
import org.march.sync.channel.OutboundChannel;

public class ChannelConnector implements Runnable{
        
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    private OutboundChannel channel;
    
    public ChannelConnector(OutboundChannel channel,  ObjectInputStream in, ObjectOutputStream out) throws IOException{       
        this.in     = in;
        this.out    = out;
        
        this.channel = channel;
    }

    @Override
    public void run() {
        channel.onOutbound(new MessageHandler() {            
            @Override
            public void handle(Message message) {
                try {
                    out.writeObject(message);
                } catch (IOException e) {
                   channel.offOutbound();                   
                }
            }
        });
        
        while(true){
            try {
                Message message = (Message)in.readObject();
                if(message != null){                    
                    channel.receive(message);
                }
            } catch (ClassNotFoundException | IOException | ChannelException e) {
                e.printStackTrace();
                break;
            } 
        }             
    }
}
