package org.march.sync;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.march.data.Operation;
import org.march.data.Pointer;
import org.march.data.StringConstant;
import org.march.data.command.Insert;
import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.MemberEndpoint;
import org.march.sync.endpoint.Message;
import org.march.sync.endpoint.MessageHandler;
import org.march.sync.endpoint.UpdateMessage;
import org.march.sync.transform.InsertInsertInclusion;
import org.march.sync.transform.Transformer;

public class MemberChannelTest {

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
        
    private MemberEndpoint channel;
       
    final LinkedList<Message> inboundBuffer = new LinkedList<Message>();
    final LinkedList<Message> outboundBuffer = new LinkedList<Message>();
    
    
    @Before
    public void setupChannel() throws EndpointException{
        inboundBuffer.clear();
        outboundBuffer.clear();

        channel = new MemberEndpoint(TRANSFORMER, new ReentrantLock());
        channel.connectOutbound(new MessageHandler() {            
            public void handle(Message message) {
                outboundBuffer.add(message);
            }
        });          
        
        channel.connectInbound(new MessageHandler() {            
            public void handle(Message message) {
                inboundBuffer.add(message);
            }
        });
        
        channel.open();
    }
    
    @After
    public void clearBuffers(){
        inboundBuffer.clear();
        outboundBuffer.clear();
    }
    
    @Test
    public void testMemberChannelSend() throws EndpointException {     
        
        Clock clk = new Clock();
        
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0}; 
              
        UpdateMessage m0 = new UpdateMessage(member0, clk.tick(), 0, ol0);
        UpdateMessage m1 = new UpdateMessage(member0, clk.tick(), 0, ol1);
        
        channel.send(m0);
        channel.send(m1);
        
        assertEquals(outboundBuffer.size(), 2);
        assertEquals(channel.getRemoteTime(), 0);
    }     
    
    @Test
    public void testMemberChannelReceive() throws EndpointException {     
        
        Clock clk = new Clock();
              
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0}; 
              
        UpdateMessage m0 = new UpdateMessage(member0, 0, clk.tick(), ol0);
        UpdateMessage m1 = new UpdateMessage(member0, 0, clk.tick(), ol1);
        
        channel.receive(m0);
        channel.receive(m1);
        
        assertEquals(inboundBuffer.size(), 2);
        assertEquals(channel.getRemoteTime(), 2);
                    
    }
        
    @Test
    public void testMemberChannelSynchronizationOnContextInequivalence() throws EndpointException {     
        
        Clock cm = new Clock();
        Clock cl = new Clock();       
               
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0}; 
              
        UpdateMessage mm = new UpdateMessage(member0, cm.tick(), 0, ol0);
        UpdateMessage ml = new UpdateMessage(member1, 0, cl.tick(), ol1);
        
        channel.send(mm);
        channel.receive(ml);
        
        assertEquals(inboundBuffer.size(), 1);
        assertEquals(c2, inboundBuffer.getFirst().getOperations()[0]);
        assertEquals(d2, inboundBuffer.getFirst().getOperations()[1]);
    }
    
    @Test
    public void testMemberChannelSynchronizationOnContextEquivalence() throws EndpointException {             
        Clock cm = new Clock();
        Clock cl = new Clock();        
               
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0}; 
                      
        UpdateMessage ml = new UpdateMessage(member0, cm.tick(), 0, ol0);
        UpdateMessage mm = new UpdateMessage(member1, cm.getTime(), cl.tick(), ol1);              
        
        channel.send(ml);
        channel.receive(mm);
        
        assertEquals(inboundBuffer.size(), 1);
        assertEquals(c0, inboundBuffer.getFirst().getOperations()[0]);
        assertEquals(d0, inboundBuffer.getFirst().getOperations()[1]);
    } 
}
