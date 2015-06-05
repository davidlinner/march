package org.march.sync;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.march.data.CommandException;
import org.march.data.ObjectException;
import org.march.data.StringConstant;
import org.march.data.command.Set;
import org.march.sync.endpoint.EndpointException;
import org.march.sync.transform.Transformer;

public class SynchronizationTest {

    private UUID name1 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private UUID name2 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    
    private Leader leader;
    private Member member1, member2;
       
    private StringConstant a = new StringConstant("A");
    private StringConstant b = new StringConstant("B");

   
    static Transformer TRANSFORMER = new Transformer();        
               
    Pipe up1, down1, up2, down2;
        
    @Before
    public void setup() throws LeaderException, EndpointException{               
        leader = new Leader(TRANSFORMER);
        member1 = new Member(name1, TRANSFORMER);
        member2 = new Member(name2, TRANSFORMER);
        
        leader.subscribe(name1);
        leader.subscribe(name2);
        
        // setup pipes
        up1 = new Pipe(member1.getOutbound(), leader.getOutbound(name1));
        down1 = new Pipe(leader.getOutbound(name1), member1.getOutbound());
        
        up2 = new Pipe(member2.getOutbound(), leader.getOutbound(name2));
        down2 = new Pipe(leader.getOutbound(name2), member2.getOutbound());        
        
        // open pipes
        up1.open();
        down1.open();
        
        up2.open();
        down2.open();
    }
    
    @After
    public void clear() throws EndpointException{
        up1.close();
        down1.close();

        up2.close();
        down2.close();
    }
    
    @Test
    public void testOneWaySynchronization() throws EndpointException, MemberException, ObjectException, CommandException {
        member1.apply(null, new Set("a", a));
        
        up1.flush();
        down1.flush();
        
        up2.flush();
        down2.flush();        
        
        assertEquals(member2.find(null, "a"), a);
    }
    
    @Test
    public void testTwoWaySynchronizationNoConflict() throws EndpointException, MemberException, ObjectException, CommandException {
        member1.apply(null, new Set("a", a));
        member2.apply(null, new Set("b", b));
        
        up1.flush();
        up2.flush();

        down1.flush();
        down2.flush();        
        
        assertEquals(member2.find(null, "a"), a);
        assertEquals(member1.find(null, "b"), b);
    }

    @Test
    public void testTwoWaySynchronizationOnConflict() throws EndpointException, MemberException, ObjectException, CommandException {
        member1.apply(null, new Set("a", a));
        member2.apply(null, new Set("a", b));
        
        up1.flush();
        up2.flush();

        down2.flush();        
        down1.flush();
        
        assertEquals(a, member1.find(null, "a"));
        assertEquals(a, member2.find(null, "a"));
    }

}
