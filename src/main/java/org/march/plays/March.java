package org.march.plays;

import org.march.data.model.Pointer;
import org.march.data.model.StringConstant;
import org.march.data.command.Construct;
import org.march.data.command.Insert;
import org.march.data.command.Set;
import org.march.data.command.Type;
import org.march.data.simple.Reference;
import org.march.data.simple.SimpleModel;

public class March {

    /**
     * @param args
     */
    public static void main(String[] args) {
        SimpleModel ctx = new SimpleModel();
        
        try {
            ctx.apply(root().resolveAsPointer(ctx), new Set("a", new StringConstant("hello world!")));
            
            Pointer p = Pointer.uniquePointer();
            ctx.apply(p, new Construct(Type.SEQUENCE));
            
            ctx.apply(root().resolveAsPointer(ctx), new Set("b", p));
            
            ctx.apply(root().append("b").resolveAsPointer(ctx), new Insert(0, new StringConstant("nice!")), new Insert(0, new StringConstant("very")));
            
            
            System.out.println(root().append("a").resolveAsConstant(ctx));
            System.out.println(root().append("b").append(1).resolveAsConstant(ctx));
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    public static Reference root(){
        return new Reference();
    }

}
