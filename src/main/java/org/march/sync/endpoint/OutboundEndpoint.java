package org.march.sync.endpoint;

public interface OutboundEndpoint {
    void receive(Bucket message) throws EndpointException;
    OutboundEndpoint connectOutbound(BucketHandler handler);
    OutboundEndpoint disconnectOutbound();
    void open() throws EndpointException;
}  
