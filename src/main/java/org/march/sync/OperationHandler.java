package org.march.sync;

import org.march.data.Operation;

public interface OperationHandler {
    void handleOperation(Operation operation);
}
