package org.march.data.command;

import org.march.data.model.Command;
import org.march.data.Modification;

public class Delete implements Command{
   
    private static final long serialVersionUID = -6816099990978169646L;
   
    private int offset;        
    
    public Delete() {
		super();
	}

	public Delete(int offset) {
        super();
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public Command clone(){
        return new Delete(this.offset);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + offset;
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
        Delete other = (Delete) obj;
        if (offset != other.offset)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Delete [offset=" + offset + "]";
    } 
    
    
    
}
