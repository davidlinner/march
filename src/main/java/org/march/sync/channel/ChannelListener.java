package org.march.sync.channel;


import java.util.UUID;

public interface ChannelListener {
    void send(UUID member, ChangeSet changeSet) throws ChannelException;
}
