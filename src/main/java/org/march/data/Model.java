package org.march.data;

public interface Model {
    public void apply(Pointer pointer, Command... commands) throws ObjectException, CommandException;
    
    public Data find(Pointer pointer, String identifier) throws ObjectException, CommandException;
    
    public Data find(Pointer pointer, int index) throws ObjectException, CommandException;
    
}
