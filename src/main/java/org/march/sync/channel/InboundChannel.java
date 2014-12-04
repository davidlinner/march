package org.march.sync.channel;


public interface InboundChannel {
    void send(Message message) throws ChannelException;
    InboundChannel onInbound(MessageHandler handler);
    InboundChannel offInbound(MessageHandler handler);
    InboundChannel offInbound();
}
