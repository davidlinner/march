package org.march.server.endpoint;

/**
 * Created by dli on 14.02.2016.
 */
public interface MessageListener {
    void receive(Message message);
}
