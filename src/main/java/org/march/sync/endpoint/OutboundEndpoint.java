package org.march.sync.endpoint;

public interface OutboundEndpoint {
    void receive(Message message) throws EndpointException;
    OutboundEndpoint connectOutbound(MessageHandler handler);
    OutboundEndpoint disconnectOutbound();
    void open() throws EndpointException;
}  
