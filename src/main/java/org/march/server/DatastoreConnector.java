package org.march.server;

import org.march.data.Operation;

public interface DatastoreConnector {
	Operation[] read(String scope);
	void write(String scope, Operation[] operations);
}
