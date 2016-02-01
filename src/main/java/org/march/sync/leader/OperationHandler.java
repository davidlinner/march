package org.march.sync.leader;

import org.march.data.Operation;

public interface OperationHandler {
    void handleOperation(Operation operation);
}
