package org.march.server;

import org.march.sync.OperationHandler;

public interface LiveDataConnector extends DatastoreConnector {
	OperationHandler getUpdateHandler();
}
