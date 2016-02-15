package org.march.sync.transform;

import org.march.data.model.Command;
import org.march.data.command.Delete;
import org.march.data.command.Nil;

public class DeleteDeleteInclusion implements Inclusion {
  
    public boolean canInclude(Command o1, Command o2) {
        return o1 instanceof Delete && o2 instanceof Delete;
    }

    public Command include(Command o1, Command o2, boolean inferior) {
        Delete d1 = (Delete)o1,
               d2 = (Delete)o2; 
        
        if(d1.getOffset() == d2.getOffset()){
            return Nil.instance();
        } else if(d1.getOffset() > d2.getOffset()){
            return new Delete(d1.getOffset() - 1);
        }
        
        return o1;
    }
}
