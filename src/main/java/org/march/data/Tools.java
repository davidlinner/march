package org.march.data;

/**
 * Created by dli on 28.01.2016.
 */
public interface Tools {
    public static Operation [] clone(Operation[] operations){
        Operation [] result = new Operation[operations.length];
        for(int i = 0; i < operations.length; i++){
            result[i] = operations[i].clone();
        }
        return result;
    }
}
