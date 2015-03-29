package org.march.data;

import java.io.Serializable;

public class Operation implements Cloneable, Serializable {
    
    private static final long serialVersionUID = -6878006473366232911L;
 
    private Command command;
    private Pointer pointer;
    
    public Operation(Pointer pointer, Command command) {
        this.command = command;
        this.pointer = pointer;
    }
        
    public Command getCommand() {
        return command;
    }
    
    public void setCommand(Command command) {
        this.command = command;
    }      
    
    public Pointer getPointer() {
        return pointer;
    }

    public void setPointer(Pointer pointer) {
        this.pointer = pointer;
    }

    public Operation clone(){
        return new Operation(this.pointer, this.command);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((pointer == null) ? 0 : pointer.hashCode());
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
        Operation other = (Operation) obj;
        if (command == null) {
            if (other.command != null)
                return false;
        } else if (!command.equals(other.command))
            return false;
        if (pointer == null) {
            if (other.pointer != null)
                return false;
        } else if (!pointer.equals(other.pointer))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Operation [command=" + command + ", pointer=" + pointer + "]";
    }
    
    
}
