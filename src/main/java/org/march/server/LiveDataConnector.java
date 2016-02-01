package org.march.server;

import org.march.sync.leader.OperationHandler;

public interface LiveDataConnector extends DatastoreConnector {
	OperationHandler getUpdateHandler();
}
