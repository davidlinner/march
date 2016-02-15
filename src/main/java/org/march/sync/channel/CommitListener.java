package org.march.sync.channel;


import java.util.UUID;

public interface CommitListener {
    void commit(UUID member, ChangeSet changeSet) throws CommitException;
}
