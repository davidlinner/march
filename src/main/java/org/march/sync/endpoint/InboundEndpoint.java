package org.march.sync.endpoint;


public interface InboundEndpoint {
    void send(Message message) throws EndpointException;
    InboundEndpoint onInbound(MessageHandler handler);
    InboundEndpoint offInbound(MessageHandler handler);
    InboundEndpoint offInbound();
}
