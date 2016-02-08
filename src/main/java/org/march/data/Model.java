package org.march.data;

import java.util.List;

public interface Model {
    public void apply(Pointer pointer, Command... commands) throws ObjectException, CommandException;

    public void apply(Operation... operation) throws ObjectException, CommandException;

    public void test(Operation... operations) throws ObjectException, CommandException;

    public Data find(Pointer pointer, String identifier) throws ObjectException, CommandException;
    
    public Data find(Pointer pointer, int index) throws ObjectException, CommandException;
    
    public List<Operation> serialize(Pointer pointer) throws ObjectException;
    
    public List<Operation> serialize();
    
}
