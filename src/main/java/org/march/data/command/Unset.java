package org.march.data.command;

import org.march.data.Command;
import org.march.data.Modification;

public class Unset implements Modification{    
   
    /**
     * 
     */
    private static final long serialVersionUID = -6543676421934184024L;
    
    private String identifier;
    
    public Unset(String identifier) {
        super();
        this.identifier = identifier;
    }
   
//    public Pointable apply(Pointable pointable) throws TypeException {
//        if(!(pointable instanceof Hash))
//            throw new TypeException("No hash.");
//        
//        ((Hash)pointable).remove(this.identifier);
//        
//        return pointable;
//    } 
    
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Command clone(){
        return new Unset(identifier);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        Unset other = (Unset) obj;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        return true;
    }


}
