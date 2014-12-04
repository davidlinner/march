package org.march.sync.transform;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.march.data.Command;
import org.march.sync.channel.Operation;

public class Transformer {
    
    private List<Inclusion> transformers;
    
    public Transformer() {
        transformers    = new LinkedList<Inclusion>(Arrays.asList(DEFAULT_INCLUSIONS));        
    }
    
    public void addInclusion(Inclusion transformer){
        transformers.add(transformer);        
    }   
    
    public void setInclusions(Inclusion[] inclusions){
        this.transformers.clear();
        this.transformers.addAll(Arrays.asList(inclusions));
    }
    
    public void transform(Operation[] ol1, Operation[] ol2, boolean inferior) throws TransformationUndefinedException{       
        Operation[] ol  = new Operation[ol1.length];
        
        for(int i = 0; i < ol1.length; i++){
            ol[i] = ol1[i].clone(); // preserve a copy of the original
            
            for(int j = 0; j < ol2.length; j++){
                // on context equivalence
                if((ol1[i].getPointer() == null && ol2[j].getPointer() == null) || 
                        (ol1[i].getPointer() != null && ol1[i].getPointer().equals(ol2[j].getPointer()) )){

                    ol1[i].setCommand(include(ol1[i].getCommand(), ol2[j].getCommand(), inferior));
                    ol2[j].setCommand(include(ol2[j].getCommand(), ol[i].getCommand(), !inferior));
                }
            }            
        }              
    }
    
    protected Command include(Command o1, Command o2, boolean inferior) throws TransformationUndefinedException{
        for(Inclusion transformer: transformers){
            if(transformer.canInclude(o1, o2)){
                return transformer.include(o1, o2, inferior);
            }
        }
        
        throw new TransformationUndefinedException("No proper transformation found.");
    }
    
    public static Inclusion [] DEFAULT_INCLUSIONS = new Inclusion[]{
        new InsertInsertInclusion(),
        new InsertDeleteInclusion(),
        new DeleteInsertInclusion(),
        new DeleteDeleteInclusion(),
        
        new SetSetInclusion(),
        new SetUnsetInclusion(),
        new UnsetSetInclusion(),
        new UnsetUnsetInclusion(),
        
        new CommandNilInclusion(),
        new NilCommandInclusion()
    };
    
    public static Inclusion [] getDefaultInclusions(){
        return DEFAULT_INCLUSIONS;
    }
}
