package org.march.sync.replica;

/**
 * Created by dli on 14.02.2016.
 */
public interface InvalidationListener extends Listener {
    void invalidated(Replica replica);
}
