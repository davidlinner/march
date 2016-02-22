package org.march.sync.replica;

import org.march.data.model.Operation;

/**
 * Created by dli on 31.01.2016.
 */
public interface ChangeListener extends Listener {
    void changed(Replica replica, Operation... operations);
}
