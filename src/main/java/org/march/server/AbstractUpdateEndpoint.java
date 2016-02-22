package org.march.server;

import org.march.sync.endpoint.*;
import org.march.sync.endpoint.UpdateEndpoint;
import org.march.sync.endpoint.UpdateListener;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by dli on 15.02.2016.
 */
public abstract class AbstractUpdateEndpoint implements UpdateEndpoint {

    private LinkedList<UpdateListener> updateListeners = new LinkedList<>();

    public void receive(UUID replicaName, ChangeSet changeSet) throws UpdateException {
        for(UpdateListener listener: updateListeners){
           listener.receive(replicaName, changeSet);
        }
    }

    @Override
    public void setUpdateListener(UpdateListener updateListener) {
        updateListeners.add(updateListener);
    }

    @Override
    public abstract void send(UUID member, ChangeSet changeSet) throws UpdateException;
}
