package org.march.sync;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.march.data.CommandException;
import org.march.data.Model;
import org.march.data.ObjectException;
import org.march.data.Operation;
import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.LeaderEndpoint;
import org.march.sync.endpoint.Message;
import org.march.sync.endpoint.MessageHandler;
import org.march.sync.endpoint.OutboundEndpoint;
import org.march.sync.endpoint.SynchronizationMessage;
import org.march.sync.endpoint.UpdateMessage;
import org.march.sync.transform.Transformer;

public class Leader {
    
    private HashMap<UUID, LeaderEndpoint> endpoints;    
   
    private Transformer transformer;
    
    private Clock clock;
    
    private Model model;
    
    private ReentrantLock lock;
    
    //TODO: clone model here on construction as it is mutable
    public Leader(Model model, Transformer transformer){
        this.endpoints    = new HashMap<UUID, LeaderEndpoint>();               
        this.clock       = new Clock();
        
        this.transformer    = transformer;
        this.model          = model;
        
        this.lock   = new ReentrantLock();
    }
    
    public void subscribe(UUID member) throws LeaderException{
    	//FIXME: shouldn't the list of endpoints be synchronized
        if(!this.endpoints.containsKey(member)){
            final LeaderEndpoint endpoint = new LeaderEndpoint(this.transformer, this.lock);
            
            this.endpoints.put(member, endpoint);        
            
            endpoint.connectInbound(new MessageHandler() {                
                public void handle(Message message) {
                    Leader.this.inbound(endpoint, message);
                }
            }); 
            
            boolean isExclusive = lock.isHeldByCurrentThread();
			if (!isExclusive) {
				lock.lock();
			}

			try {
				// send synchronization message to new member
				Operation [] operations = this.model.serialize();
				
				endpoint.send(new SynchronizationMessage(member,endpoint.getRemoteTime(), this.clock.getTime(), operations));
				
			} catch (ObjectException | EndpointException e) {
				throw new LeaderException("Subscription failed. Inconsistent leader state.", e);
			} finally {
				if (!isExclusive) {
					lock.unlock();
				}
			}
        }        
    }
    
    public void unsubscribe(UUID member){        
        LeaderEndpoint endpoint = this.endpoints.remove(member);
        if(endpoint != null){
            endpoint.disconnectInbound();
        }        
    }
    
    public OutboundEndpoint getOutbound(UUID member){
        return this.endpoints.get(member);     
    }
    
    private void inbound(LeaderEndpoint originEndpoint, Message message){        
        this.clock.tick();
        
        try {
            // TODO: set recovery point on model
            for(Operation operation: message.getOperations()){
                this.model.apply(operation.getPointer(), operation.getCommand());
            }
        } catch (ObjectException|CommandException  e) {
            // TODO: roll already performed changes back to recovery point
            // TODO: send error to member
            
            unsubscribe(message.getMember());   
            
            return;
        } 
        
        for(LeaderEndpoint endpoint: this.endpoints.values()){
            if(endpoint != originEndpoint){
                
                //TODO: filter Nil type commands - no need to forward
                // need a deep copy of operations since operations are mutable
                Operation[] operations = new Operation[message.getOperations().length];
                for(int i = 0; i < operations.length; i++){
                    operations[i] = message.getOperations()[i].clone();
                }
                              
                try {
                    endpoint.send(new UpdateMessage(message.getMember(), endpoint.getRemoteTime(), this.clock.getTime(), operations));
                } catch (EndpointException e) {
                    // TODO: unsubscribe - client uuid of endpoint should be obtainable from endpoint itself
                }
            }
        }
    }
        
}
