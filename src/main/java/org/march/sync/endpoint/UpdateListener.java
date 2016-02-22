package org.march.sync.endpoint;


import java.util.UUID;

public interface UpdateListener {
    void receive(UUID member, ChangeSet changeSet) throws UpdateException;
}
