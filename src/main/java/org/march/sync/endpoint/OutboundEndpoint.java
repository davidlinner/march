package org.march.sync.endpoint;

public interface OutboundEndpoint {
    void receive(Message message) throws EndpointException;
    OutboundEndpoint onOutbound(MessageHandler handler);
    OutboundEndpoint offOutbound(MessageHandler handler);
    OutboundEndpoint offOutbound();
    void connect() throws EndpointException;
    boolean isConnected();
}  
