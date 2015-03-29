package org.march.data.simple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.march.data.Command;
import org.march.data.CommandException;
import org.march.data.Data;
import org.march.data.DuplicateObjectException;
import org.march.data.Model;
import org.march.data.Modification;
import org.march.data.NoSuchObjectException;
import org.march.data.ObjectException;
import org.march.data.Operation;
import org.march.data.Pointable;
import org.march.data.Pointer;
import org.march.data.TypeException;
import org.march.data.UnsupportedCommandException;
import org.march.data.command.Construct;
import org.march.data.command.Delete;
import org.march.data.command.Destruct;
import org.march.data.command.Insert;
import org.march.data.command.Nil;
import org.march.data.command.Set;
import org.march.data.command.Type;
import org.march.data.command.Unset;


public class SimpleModel implements Model{
    private HashMap<Pointer, Pointable> memory;
    private Pointer context;
    
    public SimpleModel(){
        this.memory = new HashMap<Pointer, Pointable>();
        
        this.context = hash();
    }
    
    private Pointer hash(){
        Pointer pointer = new Pointer(UUID.randomUUID());
        this.memory.put(pointer, new Hash());
        
        return pointer;
    }     
    
    public void apply(Operation operation) throws ObjectException, CommandException{ 
    	apply(operation.getPointer(), operation.getCommand());
    }

    public void apply(Pointer pointer, Command... commands) throws ObjectException, CommandException{        
        Pointable pointable;
        
        if(pointer != null)
            pointable = this.memory.get(pointer);
        else 
            pointable = this.memory.get(this.context);

        for(Command command: commands){
            if(command instanceof Modification){
                if(pointable == null){
                    throw new NoSuchObjectException("No such object.");
                }
                
                pointable = apply(pointable, command);
            } else if(command instanceof Destruct){                
                if(pointable == null){
                    throw new NoSuchObjectException("No such object.");
                }
                
                this.memory.remove(pointer);
            } else if(command instanceof Construct){                
                if(pointer == null || pointable != null){
                    throw new DuplicateObjectException("Object already exists.");
                }
                
                pointable = this.memory.put(pointer, (((Construct) command).getType() == Type.HASH ? new Hash() : new Sequence()));
            } else if (!(command instanceof Nil)){
                throw new UnsupportedCommandException("Command cannot be applied."); 
            }
        }
    }        
    
    
    private Pointable apply(Pointable pointable, Command command) throws TypeException{
        if(command instanceof Insert){
            if(!(pointable instanceof Sequence))
                throw new TypeException("No sequence.");
            
            ((Sequence)pointable).add(((Insert) command).getOffset(), ((Insert) command).getData());
        } else if(command instanceof Delete){
            if(!(pointable instanceof Sequence))
                throw new TypeException("No sequence.");
            
            ((Sequence)pointable).remove(((Delete) command).getOffset());
        } else if(command instanceof Set){
            if(!(pointable instanceof Hash))
                throw new TypeException("No hash.");
            
            ((Hash)pointable).put(((Set) command).getIdentifier(), ((Set) command).getData());
        } else if(command instanceof Unset){
            if(!(pointable instanceof Hash))
                throw new TypeException("No hash.");
            
            ((Hash)pointable).remove(((Unset) command).getIdentifier());
        }
        
        return pointable;
    }

    public Data find(Pointer pointer, String identifier) throws CommandException, ObjectException {
        Pointable pointable; 
        
        if(pointer != null)
            pointable = this.memory.get(pointer);
        else 
            pointable = this.memory.get(this.context);
        
        if(pointable == null ){
            throw new NoSuchObjectException("Object unknown.");
        } if(!(pointable instanceof Hash)) {
            throw new TypeException("No hash.");
        }
        
        return ((Hash)pointable).get(identifier);
    }
    
    public Data find(Pointer pointer, int index) throws CommandException, ObjectException{        
        Pointable pointable = this.memory.get(pointer);
       
        if(pointable == null ){
            throw new NoSuchObjectException("Object unknown.");
        } else if(!(pointable instanceof Sequence)){
            throw new TypeException("No sequence.");
        }
        
        try {
            return ((Sequence)pointable).get(index);
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

	@Override
	public Operation[] serialize(Pointer pointer) throws ObjectException {
		List<Operation> operations = serialize(pointer, memory.get(pointer));
		return operations.toArray(new Operation[operations.size()]);
	}
	
	@Override
	public Operation[] serialize() throws ObjectException {
		Pointer pointer;
		List<Operation> operations = new LinkedList<Operation>();
			
		for(Entry<Pointer, Pointable> entry: memory.entrySet()){
			pointer = entry.getKey() == context ? null: entry.getKey();			
			
			//if not root object  
			if(pointer != null){
				operations.add(new Operation(pointer, new Construct(getType(entry.getValue()))));
			}
			
			operations.addAll(serialize(pointer, entry.getValue())); 									
		}				
		
		return operations.toArray(new Operation[operations.size()]);
	}
	
	private List<Operation> serialize(Pointer pointer, Pointable pointable) throws ObjectException{
		if(pointable == null ){
            throw new NoSuchObjectException("Object unknown.");
        } else if(pointable instanceof Sequence){
        	return serializeSequence(pointer, (Sequence)pointable);
        } else if(pointable instanceof Hash){
        	return serializeHash(pointer, (Hash)pointable);
        } else {
        	throw new ObjectException("Unknown type of data structure.");
        }		
	}
	
	private Type getType(Pointable pointable) throws NoSuchObjectException{
		if(pointable == null ){
            throw new NoSuchObjectException("Object unknown.");
        } else if(pointable instanceof Sequence){
        	return Type.SEQUENCE;
        } else if(pointable instanceof Hash){
        	return Type.HASH;
        } 
		
		return null;
	}
    
    private List<Operation> serializeHash(Pointer pointer, Hash hash){
    	LinkedList<Operation> operations = new LinkedList<Operation>();
    	
    	for(Entry<String, Data> entry : hash.entrySet()){
    		operations.add(new Operation(pointer, new Set(entry.getKey(), entry.getValue())));
    	}
    	
    	return operations;
    }
    
    private List<Operation> serializeSequence(Pointer pointer, Sequence seq){
    	LinkedList<Operation> operations = new LinkedList<Operation>();
    	
    	for(int i = 0; i < seq.size(); i++){
    		operations.add(new Operation(pointer, new Insert(i, seq.get(i))));
    	}
    	
    	return operations;
    }
   
}
