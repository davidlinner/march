package org.march.sync.replica;

/**
 * Created by dli on 31.01.2016.
 */
public interface ActivationListener extends Listener {
    void activated(Replica replica);
}
