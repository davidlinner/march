package org.march.server.channel;

/**
 * Created by dli on 14.02.2016.
 */
public interface MessageChannel extends MessageListener{
    void addMessageListener(MessageListener messageListener);
    void removeMessageListener(MessageListener messageListener);
}
