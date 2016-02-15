package org.march.data.command;

import org.march.data.model.Command;

public class Destruct implements Command {
    
    private static final long serialVersionUID = -3846145472821795961L;

    public Command clone(){
        return new Destruct();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Destruct;
    }
    
        
}
