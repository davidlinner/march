package org.march.data.command;

import org.march.data.Command;
import org.march.data.Lifecycle;

public class Construct implements Lifecycle {
    
    private static final long serialVersionUID = -3497711464981411229L;
    
    private Type type;
        
    public Construct(Type type) {
        super();
        this.type = type;
    }

    public Construct() {
        this.type = Type.HASH;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Command clone(){
        return new Construct(type);               
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Construct other = (Construct) obj;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Construct [type=" + type + "]";
    }
    
    
}
