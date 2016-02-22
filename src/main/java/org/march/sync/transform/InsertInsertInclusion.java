package org.march.sync.transform;

import org.march.data.model.Command;
import org.march.data.command.Insert;

public class InsertInsertInclusion implements Inclusion {
  
    public boolean canInclude(Command o1, Command o2) {
        return o1 instanceof Insert && o2 instanceof Insert;
    }

    public Command include(Command o1, Command o2, boolean inferior) {
        Insert i1 = (Insert)o1,
               i2 = (Insert)o2; 
        
        if(i1.getOffset() > i2.getOffset() || (i1.getOffset() == i2.getOffset() && inferior)){
            return new Insert(i1.getOffset() + 1, i1.getData());
        } 
        
        return i1;
    }
}
