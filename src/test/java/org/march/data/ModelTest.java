package org.march.data;

import static org.junit.Assert.*;

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
        
        Constant one = new Constant(1);

        model.apply(p, new Set("test", one));        
        assertEquals(model.find(p, "test"), one);
        
        model.apply(p, new Unset("test"));
        assertNull(model.find(p, "test"));
    }
    
  
    @Test
    public void testInsertAndDeleteOnSequence() throws ObjectException, CommandException {
        Pointer p = Pointer.uniquePointer();
        
        model.apply(p, new Construct(Type.SEQUENCE));
        
        Constant one = new Constant(1);

        model.apply(p, new Insert(0, one));        
        assertEquals(model.find(p, 0), one);
        
        model.apply(p, new Delete(0));
        assertNull(model.find(p, 0));
    }
   
}
