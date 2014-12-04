package org.march.sync.transform;

import org.march.data.Command;
import org.march.data.command.Nil;
import org.march.data.command.Set;

public class SetSetInclusion implements Inclusion {
  
    public boolean canInclude(Command o1, Command o2) {
        return o1 instanceof Set && o2 instanceof Set;
    }

    public Command include(Command o1, Command o2, boolean inferior) {
        Set s1 = (Set)o1,
            s2 = (Set)o2;
        
        if(s1.getIdentifier().equals(s2.getIdentifier()) && inferior){
            return Nil.instance();
        } 
        
        return o1;
    }
}
