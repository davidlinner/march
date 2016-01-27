package org.march.demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.UpdateBucket;
import org.march.sync.endpoint.BucketHandler;
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
        channel.connectOutbound(new BucketHandler() {            
            @Override
            public void handle(Bucket message) {
                try {
                    out.writeObject(message);
                } catch (IOException e) {
                   channel.disconnectOutbound();                   
                }
            }
        });
        
        while(true){
            try {
                UpdateBucket message = (UpdateBucket)in.readObject();
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
