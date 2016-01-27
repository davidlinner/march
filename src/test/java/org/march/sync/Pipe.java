package org.march.sync;

import java.util.LinkedList;

import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.BucketHandler;
import org.march.sync.endpoint.OutboundEndpoint;

public class Pipe {

    private final LinkedList<Bucket> buffer = new LinkedList<Bucket>();
    
    private OutboundEndpoint source, destination;
    
    private BucketHandler handler;
    
    public Pipe(OutboundEndpoint source, OutboundEndpoint destination) {
        this.source         = source;
        this.destination    = destination;
    }
    
    public void open() throws EndpointException{
        buffer.clear();
        
        this.handler = new BucketHandler() {            
            @Override
            public void handle(Bucket message) {
                buffer.add(message);
            }
        };
        
        source.connectOutbound(this.handler);
        
        source.open();
    }
    
    public void close() throws EndpointException{
        this.flush();
        
        source.disconnectOutbound();        
        
        this.handler = null;
    }

    public void flush() throws EndpointException{
        for(Bucket message: buffer){
            destination.receive(message);
        }
       buffer.clear();
    }
}
