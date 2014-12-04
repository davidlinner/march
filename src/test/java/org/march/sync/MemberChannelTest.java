package org.march.sync;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.march.data.Constant;
import org.march.data.Pointer;
import org.march.data.command.Insert;
import org.march.sync.Clock;
import org.march.sync.channel.ChannelException;
import org.march.sync.channel.MemberChannel;
import org.march.sync.channel.Message;
import org.march.sync.channel.MessageHandler;
import org.march.sync.channel.Operation;
import org.march.sync.transform.InsertInsertInclusion;
import org.march.sync.transform.Transformer;

public class MemberChannelTest {

    private UUID member0 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private UUID member1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    
    private Pointer p = new Pointer(UUID.randomUUID());
    
    private Constant a = new Constant("a");
    private Constant b = new Constant("b");
    private Constant c = new Constant("c");
    private Constant d = new Constant("d");
    
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
        
    private MemberChannel channel;
       
    final LinkedList<Message> inboundBuffer = new LinkedList<Message>();
    final LinkedList<Message> outboundBuffer = new LinkedList<Message>();
    
    
    @Before
    public void setupChannel(){
        inboundBuffer.clear();
        outboundBuffer.clear();

        channel = new MemberChannel(TRANSFORMER);
        channel.onOutbound(new MessageHandler() {            
            public void handle(Message message) {
                outboundBuffer.add(message);
            }
        });          
        
        channel.onInbound(new MessageHandler() {            
            public void handle(Message message) {
                inboundBuffer.add(message);
            }
        });     
    }
    
    @After
    public void clearBuffers(){
        inboundBuffer.clear();
        outboundBuffer.clear();
    }
    
    @Test
    public void testMemberChannelSend() throws ChannelException {     
        
        Clock clk = new Clock();
        
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0}; 
              
        Message m0 = new Message(member0, clk.tick(), 0, ol0);
        Message m1 = new Message(member0, clk.tick(), 0, ol1);
        
        channel.send(m0);
        channel.send(m1);
        
        assertEquals(outboundBuffer.size(), 2);
        assertEquals(channel.getRemoteTime(), 0);
    }     
    
    @Test
    public void testMemberChannelReceive() throws ChannelException {     
        
        Clock clk = new Clock();
              
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0}; 
              
        Message m0 = new Message(member0, 0, clk.tick(), ol0);
        Message m1 = new Message(member0, 0, clk.tick(), ol1);
        
        channel.receive(m0);
        channel.receive(m1);
        
        assertEquals(inboundBuffer.size(), 2);
        assertEquals(channel.getRemoteTime(), 2);
                    
    }
        
    @Test
    public void testMemberChannelSynchronizationOnContextInequivalence() throws ChannelException {     
        
        Clock cm = new Clock();
        Clock cl = new Clock();       
               
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0}; 
              
        Message mm = new Message(member0, cm.tick(), 0, ol0);
        Message ml = new Message(member1, 0, cl.tick(), ol1);
        
        channel.send(mm);
        channel.receive(ml);
        
        assertEquals(inboundBuffer.size(), 1);
        assertEquals(c2, inboundBuffer.getFirst().getOperations()[0]);
        assertEquals(d2, inboundBuffer.getFirst().getOperations()[1]);
    }
    
    @Test
    public void testMemberChannelSynchronizationOnContextEquivalence() throws ChannelException {             
        Clock cm = new Clock();
        Clock cl = new Clock();        
               
        Operation[] ol0 = new Operation[]{a0, b0}, 
                    ol1 = new Operation[]{c0, d0}; 
                      
        Message ml = new Message(member0, cm.tick(), 0, ol0);
        Message mm = new Message(member1, cm.getTime(), cl.tick(), ol1);              
        
        channel.send(ml);
        channel.receive(mm);
        
        assertEquals(inboundBuffer.size(), 1);
        assertEquals(c0, inboundBuffer.getFirst().getOperations()[0]);
        assertEquals(d0, inboundBuffer.getFirst().getOperations()[1]);
    } 
}
