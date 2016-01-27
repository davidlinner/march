package org.march.sync.endpoint;


public interface InboundEndpoint {
    void send(Bucket message) throws EndpointException;
    InboundEndpoint connectInbound(BucketHandler handler);
    InboundEndpoint disconnectInbound();
}
