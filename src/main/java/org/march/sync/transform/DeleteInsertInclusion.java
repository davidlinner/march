package org.march.sync.transform;

import org.march.data.Command;
import org.march.data.command.Delete;
import org.march.data.command.Insert;

public class DeleteInsertInclusion implements Inclusion {
  
    public boolean canInclude(Command o1, Command o2) {
        return o1 instanceof Delete && o2 instanceof Insert;
    }

    public Command include(Command o1, Command o2, boolean inferior) {
        Delete d1 = (Delete)o1;
        Insert i1 = (Insert)o2; 
        
        if(d1.getOffset() >= i1.getOffset()){
            return new Delete(d1.getOffset() + 1);
        } 
        
        return o1;
    }
}
