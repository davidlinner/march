package org.march.data.command;

import org.march.data.Command;

public class Nil implements Command {

    private static final long serialVersionUID = -4543569228432928359L;
   
    public Nil() {
    }

    public Command clone(){
        return new Nil();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Nil;
    }
    
    private static Nil nil = new Nil(); 
    public static Nil instance(){
        return nil;
    }
}
