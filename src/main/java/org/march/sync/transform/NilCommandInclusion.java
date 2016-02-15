package org.march.sync.transform;

import org.march.data.model.Command;
import org.march.data.command.Nil;

public class NilCommandInclusion implements Inclusion {
  
    public boolean canInclude(Command o1, Command o2) {
        return o1 instanceof Nil;
    }

    public Command include(Command o1, Command o2, boolean inferior) {       
        return o1;
    }
}
