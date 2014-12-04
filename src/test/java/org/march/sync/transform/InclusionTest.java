package org.march.sync.transform;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.march.data.Command;
import org.march.data.Constant;
import org.march.data.command.Delete;
import org.march.data.command.Insert;
import org.march.data.command.Nil;
import org.march.data.command.Set;
import org.march.data.command.Unset;

@RunWith(Parameterized.class)
public class InclusionTest {
    
    final static Transformer TRANSFORMER = new Transformer();
    static {
        TRANSFORMER.addInclusion(new InsertInsertInclusion());
        TRANSFORMER.addInclusion(new InsertDeleteInclusion());
        TRANSFORMER.addInclusion(new DeleteInsertInclusion());
        TRANSFORMER.addInclusion(new DeleteDeleteInclusion());
        
        TRANSFORMER.addInclusion(new SetSetInclusion());
        TRANSFORMER.addInclusion(new SetUnsetInclusion());
        TRANSFORMER.addInclusion(new UnsetSetInclusion());
        TRANSFORMER.addInclusion(new UnsetUnsetInclusion());
        
        TRANSFORMER.addInclusion(new CommandNilInclusion());
        TRANSFORMER.addInclusion(new NilCommandInclusion());
    }
    
    final static Constant  
        A = new Constant("a"),
        B = new Constant("b"),
        C = new Constant("c"),
        D = new Constant("d");
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 {new Insert(0, A), new Insert(0, B), true,     new Insert(1, A)},
                 {new Insert(0, A), new Insert(0, B), false,    new Insert(0, A)},
                 {new Insert(1, A), new Insert(0, B), true,     new Insert(2, A)},
                 {new Insert(1, A), new Insert(0, B), false,    new Insert(2, A)},
                 {new Insert(0, A), new Insert(1, B), true,     new Insert(0, A)},
                 {new Insert(0, A), new Insert(1, B), false,    new Insert(0, A)},

                 {new Insert(0, A), new Delete(0), true,    new Insert(0, A)},
                 {new Insert(0, A), new Delete(0), false,   new Insert(0, A)},
                 {new Insert(1, A), new Delete(0), true,    new Insert(0, A)},
                 {new Insert(1, A), new Delete(0), false,   new Insert(0, A)},
                 {new Insert(0, A), new Delete(1), true,    new Insert(0, A)},
                 {new Insert(0, A), new Delete(1), false,   new Insert(0, A)},
                 
                 {new Delete(0), new Insert(0, B), true,    new Delete(1)},
                 {new Delete(0), new Insert(0, B), false,   new Delete(1)},
                 {new Delete(0), new Insert(1, B), true,    new Delete(0)},
                 {new Delete(0), new Insert(1, B), false,   new Delete(0)},
                 {new Delete(1), new Insert(0, B), true,    new Delete(2)},
                 {new Delete(1), new Insert(0, B), false,   new Delete(2)},
                 
                 {new Delete(0), new Delete(0), true,   Nil.instance()},
                 {new Delete(0), new Delete(0), false,  Nil.instance()},
                 {new Delete(1), new Delete(0), true,   new Delete(0)},
                 {new Delete(1), new Delete(0), false,  new Delete(0)},
                 {new Delete(0), new Delete(1), true,   new Delete(0)},
                 {new Delete(0), new Delete(1), false,  new Delete(0)},
                   
                 {new Set("a", A), new Set("a", B), true,   Nil.instance()},
                 {new Set("a", A), new Set("a", B), false,  new Set("a", A)},
                 {new Set("a", A), new Set("b", B), true,   new Set("a", A)},
                 {new Set("a", A), new Set("b", B), false,  new Set("a", A)},
                 
                 {new Set("a", A), new Unset("a"), true,    new Set("a", A)},
                 {new Set("a", A), new Unset("a"), false,   new Set("a", A)},
                 {new Set("a", A), new Unset("b"), true,    new Set("a", A)},
                 {new Set("a", A), new Unset("b"), false,   new Set("a", A)},

                 {new Unset("a"), new Set("a", B), true,    Nil.instance()},
                 {new Unset("a"), new Set("a", B), false,   Nil.instance()},
                 {new Unset("a"), new Set("b", B), true,    new Unset("a")},
                 {new Unset("a"), new Set("b", B), false,   new Unset("a")},

                 {new Unset("a"), new Unset("a"), true,     Nil.instance()},
                 {new Unset("a"), new Unset("a"), false,    Nil.instance()},
                 {new Unset("a"), new Unset("b"), true,     new Unset("a")},
                 {new Unset("a"), new Unset("b"), false,    new Unset("a")}
            });
    }

    private Command c1, c2, assertResult;
    
    private boolean inferior;
    
    public InclusionTest(Command c1, Command c2, boolean inferior, Command assertResult) {
        super();
        this.c1 = c1;
        this.c2 = c2;
        this.inferior = inferior;
        this.assertResult = assertResult;
    }



    @Test
    public void testInclusion() throws TransformationUndefinedException {
        
        Command result = TRANSFORMER.include(c1, c2, inferior);
                
        assertEquals(assertResult, result);
    }     
}
