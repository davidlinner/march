package org.march.sync.channel;

public interface OutboundChannel {
    void receive(Message message) throws ChannelException;
    OutboundChannel onOutbound(MessageHandler handler);
    OutboundChannel offOutbound(MessageHandler handler);
    OutboundChannel offOutbound();
}  
