package org.march.sync;

import org.march.data.Operation;

/**
 * Created by dli on 31.01.2016.
 */
public interface ChangeListener extends Listener {
    void changed(Member member, Operation... operations);
}
