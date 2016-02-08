package org.march.data;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.march.data.command.Construct;
import org.march.data.command.Delete;
import org.march.data.command.Destruct;
import org.march.data.command.Insert;
import org.march.data.command.Set;
import org.march.data.command.Type;
import org.march.data.command.Unset;
import org.march.data.simple.SimpleModel;

public class ModelTest {
    
    private Model model;
    
    @Before
    public void setupModel(){
        model = new SimpleModel();
    }

    @Test(expected=DuplicateObjectException.class)
    public void testConstructHashNoPointer() throws ObjectException, CommandException {        
        model.apply(null, new Construct(Type.HASH));        
    }
    
    @Test
    public void testConstructHash() throws ObjectException, CommandException {        
        Pointer p = Pointer.uniquePointer();
        model.apply(p, new Construct(Type.HASH));   
        
        assertNull(model.find(p, "test"));
          
    }
        
    @Test
    public void testConstructSequence() throws ObjectException, CommandException {        
        Pointer p = Pointer.uniquePointer();
        model.apply(p, new Construct(Type.SEQUENCE));     
        
        assertNull(model.find(p, 0));
    }
    
    @Test(expected=NoSuchObjectException.class)
    public void testDestructHash() throws ObjectException, CommandException {        
        Pointer p = Pointer.uniquePointer();
        model.apply(p, new Construct(Type.HASH));
        assertNull(model.find(p, "test"));
        
        model.apply(p, new Destruct()); 
        model.find(p, "test");
    }
    
    @Test(expected=NoSuchObjectException.class)
    public void testDestructSequence() throws ObjectException, CommandException {        
        Pointer p = Pointer.uniquePointer();
        model.apply(p, new Construct(Type.SEQUENCE));
        assertNull(model.find(p, 0));
        
        model.apply(p, new Destruct());
        model.find(p, 0);        
    }
    
    @Test
    public void testSetAndUnsetOnHash() throws ObjectException, CommandException {        
        Pointer p = Pointer.uniquePointer();
        
        model.apply(p, new Construct(Type.HASH));
        
        NumberConstant one = new NumberConstant(1);

        model.apply(p, new Set("test", one));        
        assertEquals(model.find(p, "test"), one);
        
        model.apply(p, new Unset("test"));
        assertNull(model.find(p, "test"));
    }
    
  
    @Test
    public void testInsertAndDeleteOnSequence() throws ObjectException, CommandException {
        Pointer p = Pointer.uniquePointer();
        
        model.apply(p, new Construct(Type.SEQUENCE));
        
        NumberConstant one = new NumberConstant(1);

        model.apply(p, new Insert(0, one));        
        assertEquals(model.find(p, 0), one);
        
        model.apply(p, new Delete(0));
        assertNull(model.find(p, 0));
    }
    
    @Test
    public void testSerializeAll() throws ObjectException, CommandException {
        Pointer p1 = Pointer.uniquePointer(),
        		p2 = Pointer.uniquePointer();
        
        model.apply(p1, new Construct(Type.SEQUENCE));
        model.apply(p2, new Construct(Type.HASH));
        
        NumberConstant one = new NumberConstant(1);
        NumberConstant two = new NumberConstant(2);

        model.apply(p1, new Insert(0, one));        
        model.apply(p1, new Insert(0, two));
                
        model.apply(p2, new Set("one", one));
        model.apply(p2, new Set("two", two));
        
        
        List<Operation> operations = model.serialize();
        
        int c1 = operations.indexOf(new Operation(p1,  new Construct(Type.SEQUENCE))),
        	c2 = operations.indexOf(new Operation(p2,  new Construct(Type.HASH))),
        	s1 = operations.indexOf(new Operation(p2,  new Set("one", one))),
        	i1 = operations.indexOf(new Operation(p1,  new Insert(1, one)));
        
        assertTrue(c1 >= 0);
        assertTrue(c2 >= 0);        
        
        assertTrue(i1 > c1);
        assertTrue(s1 > c2);
        
        for (Operation o : operations){
        	System.out.println(o);
        }
    }
   
}
