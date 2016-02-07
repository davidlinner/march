package org.march.server;

import org.march.sync.master.OperationListener;

public interface LiveDataConnector extends DatastoreConnector {
	OperationListener getUpdateHandler();
}
