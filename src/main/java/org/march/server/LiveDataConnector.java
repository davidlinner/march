package org.march.server;

import org.march.sync.master.OperationHandler;

public interface LiveDataConnector extends DatastoreConnector {
	OperationHandler getUpdateHandler();
}
