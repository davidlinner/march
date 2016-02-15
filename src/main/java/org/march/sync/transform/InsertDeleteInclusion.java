package org.march.sync.transform;

import org.march.data.model.Command;
import org.march.data.command.Delete;
import org.march.data.command.Insert;

public class InsertDeleteInclusion implements Inclusion {
  
    public boolean canInclude(Command o1, Command o2) {
        return o1 instanceof Insert && o2 instanceof Delete;
    }

    public Command include(Command o1, Command o2, boolean inferior) {
        Insert i1 = (Insert)o1;
        Delete d1 = (Delete)o2; 
        
        if(i1.getOffset() > d1.getOffset()){
            return new Insert(i1.getOffset() - 1, i1.getData());
        } 
        
        return i1;
    }
}
