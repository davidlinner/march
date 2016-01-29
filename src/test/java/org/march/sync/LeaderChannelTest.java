package org.march.sync;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.march.data.Operation;
import org.march.data.Pointer;
import org.march.data.StringConstant;
import org.march.data.command.Insert;
import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.LeaderEndpoint;
import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.BucketHandler;
import org.march.sync.endpoint.UpdateBucket;
import org.march.sync.transform.InsertInsertInclusion;
import org.march.sync.transform.Transformer;

public class LeaderChannelTest {

    private UUID member0 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private UUID member1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    
    private Pointer p = new Pointer(UUID.randomUUID());
    
    private StringConstant a = new StringConstant("a");
    private StringConstant b = new StringConstant("b");
    private StringConstant c = new StringConstant("c");
    private StringConstant d = new StringConstant("d");
    
    Operation a0 = new Operation(p, new Insert(0, a));    
    
    Operation b0 = new Operation(p, new Insert(0, b));
    
    Operation c0 = new Operation(p, new Insert(0, c));
    Operation c2 = new Operation(p, new Insert(2, c));
    
    Operation d0 = new Operation(p, new Insert(0, d));
    Operation d2 = new Operation(p, new Insert(2, d));
   
    static Transformer TRANSFORMER = new Transformer();        
    static {
        TRANSFORMER.addInclusion(new InsertInsertInclusion());
    }
        
    private LeaderEndpoint endpoint;

    
    @Before
    public void setupChannel() throws EndpointException{
        endpoint = new LeaderEndpoint(TRANSFORMER);
    }
    
    @Test
    public void testLeaderChannelSend() throws EndpointException {     
        
        Clock clk = new Clock();
        
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0}; 
              
        UpdateBucket m0 = new UpdateBucket(member0, 0, clk.tick(), ol0);
        UpdateBucket m1 = new UpdateBucket(member0, 0, clk.tick(), ol1);
        
        endpoint.send(m0);
        endpoint.send(m1);
        
        //assertEquals(outboundBuffer.size(), 2);
        assertEquals(endpoint.getRemoteTime(), 0);
    } 
    
    
    @Test
    public void testLeaderChannelReceive() throws EndpointException {     
        
        Clock clk = new Clock();
              
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0}; 
              
        UpdateBucket m0 = new UpdateBucket(member0, clk.tick(), 0, ol0);
        UpdateBucket m1 = new UpdateBucket(member0, clk.tick(), 0, ol1);
        
        endpoint.receive(m0);
        endpoint.receive(m1);
        
        assertEquals(endpoint.getRemoteTime(), 2);
                    
    }
        
    @Test
    public void testLeaderChannelSynchronizationOnContextInequivalence() throws EndpointException {     
        
        Clock cl = new Clock();
        Clock cm = new Clock();       
               
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0};

        UpdateBucket ml = new UpdateBucket(member0, 0, cl.tick(), ol0);
        UpdateBucket mm = new UpdateBucket(member1, cm.tick(), 0, ol1);
        
        ml = endpoint.send(ml);
        mm = endpoint.receive(mm);

        assertEquals(c2, mm.getOperations()[0]);
        assertEquals(d2, mm.getOperations()[1]);
    }
    
    @Test
    public void testLeaderChannelSynchronizationOnContextEquivalence() throws EndpointException {             
        Clock cl = new Clock();
        Clock cm = new Clock();        
               
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0};

        UpdateBucket ml = new UpdateBucket(member0, 0, cl.tick(), ol0);
        UpdateBucket mm = new UpdateBucket(member1, cm.tick(), cl.getTime(), ol1);
        
        ml = endpoint.send(ml);
        mm = endpoint.receive(mm);
        
        assertEquals(c0,mm.getOperations()[0]);
        assertEquals(d0,mm.getOperations()[1]);
    } 
}
