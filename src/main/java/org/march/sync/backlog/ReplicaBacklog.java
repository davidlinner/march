package org.march.sync.backlog;

import org.march.sync.endpoint.ChangeSet;
import org.march.sync.transform.Transformer;

public class ReplicaBacklog extends Backlog {

    public ReplicaBacklog(Transformer transformer) {
        super(transformer);
    }

    @Override
    protected int getLocalTime(ChangeSet message) {
        return message.getMasterTime();
    }

    @Override
    protected void setLocalTime(ChangeSet changeSet, int time) {
        changeSet.setMasterTime(time);
    }

    @Override
    protected int getRemoteTime(ChangeSet message) {
        return message.getReplicaTime();
    }

    @Override
    protected void setRemoteTime(ChangeSet message, int time) {
        message.setReplicaTime(time);
    }
}
