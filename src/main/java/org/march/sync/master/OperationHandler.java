package org.march.sync.master;

import org.march.data.Operation;

public interface OperationHandler {
    void handleOperation(Operation operation);
}
