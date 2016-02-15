package org.march.server;

import org.march.sync.channel.ChangeSet;
import org.march.sync.channel.CommitChannel;
import org.march.sync.channel.CommitException;
import org.march.sync.channel.CommitListener;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by dli on 15.02.2016.
 */
public abstract class AbstractCommitChannel implements CommitChannel {

    private LinkedList<CommitListener> commitListeners = new LinkedList<>();

    public void delegate(UUID replicaName, ChangeSet changeSet) throws CommitException {
        for(CommitListener listener: commitListeners){
           listener.commit(replicaName, changeSet);
        }
    }

    @Override
    public void addReceiveListener(CommitListener commitListener) {
        commitListeners.add(commitListener);
    }

    @Override
    public void removeReceiveListener(CommitListener commitListener) {
        commitListeners.remove(commitListener);
    }

    @Override
    public abstract void commit(UUID member, ChangeSet changeSet) throws CommitException;
}
