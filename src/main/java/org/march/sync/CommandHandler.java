package org.march.sync;

import org.march.data.Command;
import org.march.data.Pointer;

public interface CommandHandler {
    void handleCommand(Pointer pointer, Command command);
}
