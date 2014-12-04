package org.march.data.command;

import org.march.data.Command;
import org.march.data.Data;
import org.march.data.Modification;

public class Set implements Modification{    
      
    private static final long serialVersionUID = 5037939691001432553L;
  
    private String identifier;
    private Data data;
    
    public Set(String identifier, Data data) {
        super();
        this.identifier = identifier;
        this.data       = data;
    }
       
    public Command clone(){
        return new Set(identifier, data);
    }        

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result
                + ((identifier == null) ? 0 : identifier.hashCode());
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
        Set other = (Set) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Set [identifier=" + identifier + ", data=" + data + "]";
    }
}
