package org.march.sync.replica;

/**
 * Created by dli on 31.01.2016.
 */
public interface DeactivationListener extends Listener {
    void deactivated(Replica replica);
}
