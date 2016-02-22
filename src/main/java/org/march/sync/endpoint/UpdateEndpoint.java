package org.march.sync.endpoint;

import java.util.UUID;

/**
 * Created by dli on 01.02.2016.
 */
public interface UpdateEndpoint  {
    void setUpdateListener(UpdateListener handler);
    void send(UUID member, ChangeSet changeSet) throws UpdateException;
}
