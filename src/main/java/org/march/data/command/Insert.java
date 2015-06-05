package org.march.data.command;

import org.march.data.Command;
import org.march.data.Data;
import org.march.data.Modification;

public class Insert implements Modification{    
   
    private static final long serialVersionUID = -801997573780532462L;
   
    private int offset;
    private Data data;        
    
    public Insert() {
		super();
	}

	public Insert(int offset, Data data) {
        super();
        this.offset = offset;
        this.data  = data;
    }
   
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    } 
    
    public Command clone(){
        return new Insert(this.offset, this.data);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
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
        Insert other = (Insert) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        if (offset != other.offset)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Insert [offset=" + offset + ", data=" + data + "]";
    }
    
}
