package org.march.data;

import java.util.UUID;

public class Pointer implements Data{

    private static final long serialVersionUID = -6900353447447281308L;

    private UUID address;
    
    public Pointer(UUID address) {
        super();
        this.address = address;
    }

    public UUID getAddress() {
        return address;
    }

    public void setAddress(UUID address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
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
        Pointer other = (Pointer) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Pointer [address=" + address + "]";
    }           
    
    public Pointer clone(){
        return new Pointer(address);
    } 
    
    public static Pointer uniquePointer(){
        return new Pointer(UUID.randomUUID());
    }
}
