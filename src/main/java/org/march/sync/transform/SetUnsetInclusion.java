package org.march.sync.transform;

import org.march.data.Command;
import org.march.data.command.Set;
import org.march.data.command.Unset;

public class SetUnsetInclusion implements Inclusion {
  
    public boolean canInclude(Command o1, Command o2) {
        return o1 instanceof Set && o2 instanceof Unset;
    }

    public Command include(Command o1, Command o2, boolean inferior) {
        return o1;
    }
}
