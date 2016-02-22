package org.march.data.model;

import org.march.data.model.Operation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dli on 28.01.2016.
 */
public interface Tools {
    public static Operation[] clone(Operation[] operations){
        Operation [] result = new Operation[operations.length];
        for(int i = 0; i < operations.length; i++){
            result[i] = operations[i].clone();
        }
        return result;
    }

    public static List<Operation> clone(List<Operation> operations){
        LinkedList<Operation> result = new LinkedList<Operation>();
        for(Operation operation: operations){
            result.add(operation.clone());
        }
        return result;
    }

    public static Operation[] asArray(List<Operation> operations){
        return operations.toArray(new Operation[operations.size()]);
    }

    public static List<Operation> asList(Operation... operations){
        return Arrays.asList(operations);
    }

}
