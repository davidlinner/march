package org.march.sync.channel;

/**
 * Created by dli on 01.02.2016.
 */
public interface CommitChannel extends CommitListener {
    void addReceiveListener(CommitListener handler);
    void removeReceiveListener(CommitListener handler);
}
