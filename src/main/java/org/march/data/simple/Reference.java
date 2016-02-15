package org.march.data.simple;

import java.util.LinkedList;

import org.march.data.model.CommandException;
import org.march.data.model.Constant;
import org.march.data.model.Data;
import org.march.data.model.ObjectException;
import org.march.data.model.Pointer;
import org.march.data.model.Model;
import org.march.data.model.TypeException;


public class Reference {
            
    private LinkedList<Resolver> stack;
    
    public Reference(){
        stack = new LinkedList<Resolver>();
    }
    
    public Reference append(String identifier) throws ObjectException, CommandException{
        stack.add(new Identifier(identifier));        
        return this;
    }
    
    public Reference append(int index) throws ObjectException, CommandException{
        stack.add(new Index(index));        
        return this;
    }
    
    public Pointer resolveAsPointer(Model state) throws CommandException, ObjectException{
        return (Pointer)resolve(state);
    }
    
    public Constant resolveAsConstant(Model state) throws CommandException, ObjectException{
        return (Constant)resolve(state);
    }   
    
    public Data resolve(Model state) throws CommandException, ObjectException{
        Data data = null;
        for(Resolver resolver: stack){
             data = resolver.resolve(data, state);
        }
        return data;
    }
    
    private interface Resolver{
        Data resolve(Data data, Model state) throws CommandException, ObjectException;
    }
    
    private class Identifier implements Resolver{
        String identifier;

        Identifier(String identifier) {
            super();
            this.identifier = identifier;
        }
        
        public Data resolve(Data data, Model state) throws CommandException, ObjectException{
            if(data instanceof Pointer || data == null){
                return state.find((Pointer) data, this.identifier);
            } else {
                throw new TypeException("No pointer.");
            }                      
        }
    }
    
    private class Index  implements Resolver{
        int index;

        Index (int index) {
            super();
            this.index = index;
        }
        
        public Data resolve(Data data, Model state) throws CommandException, ObjectException{
            if(data != null && data instanceof Pointer){
                return state.find((Pointer) data, this.index);
            } else {
                throw new TypeException("No pointer.");
            }                      
        }
    }
}
