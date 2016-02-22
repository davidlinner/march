package org.march.sync.transform;

import org.march.data.model.Command;

public interface Inclusion {
    boolean canInclude(Command o1, Command o2);    
 
    Command include(Command o1, Command o2, boolean inferior);
}
