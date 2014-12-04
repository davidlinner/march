package org.march.sync.transform;

import org.march.data.Command;
import org.march.data.command.Nil;
import org.march.data.command.Unset;

public class UnsetUnsetInclusion implements Inclusion {
  
    public boolean canInclude(Command o1, Command o2) {
        return o1 instanceof Unset && o2 instanceof Unset;
    }

    public Command include(Command o1, Command o2, boolean inferior) {
        Unset u1 = (Unset)o1;
        Unset u2 = (Unset)o2;
        
        if (u1.getIdentifier().equals(u2.getIdentifier())){
            return Nil.instance();
        }
        
        return o1;
    }
}
