package org.march.sync.master;

import org.march.data.Operation;

public interface OperationListener {
    void update(Operation... operation);
}
