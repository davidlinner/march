package org.march.demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.Message;
import org.march.sync.endpoint.UpdateMessage;
import org.march.sync.endpoint.MessageHandler;
import org.march.sync.endpoint.OutboundEndpoint;

public class ChannelConnector implements Runnable{
        
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    private OutboundEndpoint channel;
    
    public ChannelConnector(OutboundEndpoint channel,  ObjectInputStream in, ObjectOutputStream out) throws IOException{       
        this.in     = in;
        this.out    = out;
        
        this.channel = channel;
    }

    @Override
    public void run() {
        channel.connectOutbound(new MessageHandler() {            
            @Override
            public void handle(Message message) {
                try {
                    out.writeObject(message);
                } catch (IOException e) {
                   channel.disconnectOutbound();                   
                }
            }
        });
        
        while(true){
            try {
                UpdateMessage message = (UpdateMessage)in.readObject();
                if(message != null){                    
                    channel.receive(message);
                }
            } catch (ClassNotFoundException | IOException | EndpointException e) {
                e.printStackTrace();
                break;
            } 
        }             
    }
}
