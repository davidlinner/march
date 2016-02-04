package org.march.sync.replica;

/**
 * Created by dli on 31.01.2016.
 */
public interface ClosingListener extends Listener {
    void closing(Replica replica);
}
