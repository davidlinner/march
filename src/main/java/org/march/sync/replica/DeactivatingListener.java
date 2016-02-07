package org.march.sync.replica;

/**
 * Created by dli on 31.01.2016.
 */
public interface DeactivatingListener extends Listener {
    void deactivating(Replica replica);
}
