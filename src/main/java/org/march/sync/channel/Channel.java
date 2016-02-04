package org.march.sync.channel;

/**
 * Created by dli on 01.02.2016.
 */
public interface Channel extends ChannelListener {
    void addReceiveListener(ChannelListener handler);
    void removeReceiveListener(ChannelListener handler);
}
