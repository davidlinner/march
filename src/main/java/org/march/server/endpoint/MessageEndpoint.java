package org.march.server.endpoint;

/**
 * Created by dli on 14.02.2016.
 */
public interface MessageEndpoint {
    void setMessageListener(MessageListener messageListener);
    void send(Message message);

}
