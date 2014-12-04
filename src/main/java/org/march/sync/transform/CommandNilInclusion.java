package org.march.sync.transform;

import org.march.data.Command;
import org.march.data.command.Nil;

public class CommandNilInclusion implements Inclusion {
  
    public boolean canInclude(Command o1, Command o2) {
        return o2 instanceof Nil;
    }

    public Command include(Command o1, Command o2, boolean inferior) {       
        return o1;
    }
}
