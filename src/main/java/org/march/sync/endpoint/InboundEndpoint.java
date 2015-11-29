package org.march.sync.endpoint;


public interface InboundEndpoint {
    void send(Message message) throws EndpointException;
    InboundEndpoint connectInbound(MessageHandler handler);
    InboundEndpoint disconnectInbound();
}
