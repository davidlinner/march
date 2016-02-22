package org.march.sync.transform;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.march.data.model.Operation;
import org.march.data.model.Pointer;
import org.march.data.model.StringConstant;
import org.march.data.command.Insert;

@RunWith(Parameterized.class)
public class TransformationTest {
    
    final StringConstant  a = new StringConstant("a"),
                    b = new StringConstant("b"),
                    c = new StringConstant("c"),
                    d = new StringConstant("d");
    
    Operation a0, b0, c0, c2, d0, d2;
    
    private static Transformer TRANSFORMER = new Transformer();
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 {new Pointer(UUID.randomUUID())}, {null}  
           });
    }
    
    public TransformationTest(Pointer p){       
        a0 = new Operation(p, new Insert(0, a));
        
        b0 = new Operation(p, new Insert(0, b));
        
        c0 = new Operation(p, new Insert(0, c));
        c2 = new Operation(p, new Insert(2, c));
        
        d0 = new Operation(p, new Insert(0, d));
        d2 = new Operation(p, new Insert(2, d));
    }
   
    
    @Test
    public void testListInclusionWithInsert() throws TransformationUndefinedException {        
      
        Operation[] ol1 = new Operation[]{a0, b0}, 
                    ol2 = new Operation[]{c0, d0};
        
        TRANSFORMER.transform(ol1, ol2, false);
        
//        assertEquals(a0, ol1[0]);        
//        assertEquals(b0, ol1[1]);
//        
//        assertEquals(c2, ol2[0]);
//        assertEquals(d2, ol2[1]);
    }   
}
