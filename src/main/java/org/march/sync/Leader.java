package org.march.sync;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.march.data.CommandException;
import org.march.data.Model;
import org.march.data.ObjectException;
import org.march.data.Operation;
import org.march.data.simple.SimpleModel;
import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.LeaderEndpoint;
import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.BucketHandler;
import org.march.sync.endpoint.OutboundEndpoint;
import org.march.sync.endpoint.SynchronizationBucket;
import org.march.sync.endpoint.UpdateBucket;
import org.march.sync.transform.Transformer;

public class Leader {
    
    private HashMap<UUID, LeaderEndpoint> endpoints;    
   
    private Transformer transformer;
    
    private Clock clock;
    
    private Model model;
    
    private ReentrantLock lock;
    
    private OperationHandler handler;
    
    //TODO: Model could be muted outside. how cope with this issue?
    //TODO: access to endpoints has to be synchronized
    public Leader(Transformer transformer){
        this.endpoints    = new HashMap<UUID, LeaderEndpoint>();               
        this.clock       = new Clock();
        
        this.transformer    = transformer;
        this.model          = new SimpleModel();
        
        this.lock   = new ReentrantLock();
    }
    
    public void setData(Operation [] operations) throws ObjectException, CommandException, LeaderException {
    	if(!endpoints.isEmpty()){
    		throw new LeaderException("State is immutable when endpoints are connected.");
    	}
    	
    	this.model.apply(operations);
    }
    
    public Operation[] getData(){
    	try {
			return this.model.serialize();
		} catch (ObjectException e) {
			//TODO: add debug logging here - must not happen / no reasonable treatment 
		}
    	
    	return null;
    }
    
    // use command handle to keep original data source in sync
    public void onCommand(OperationHandler handler){
    	this.handler = handler;
    }
    
    public void offCommand(){
    	this.handler = null;
    }
    
    public void subscribe(UUID member) throws LeaderException{

    	//FIXME: shouldn't the list of endpoints be synchronized?
        if(!this.endpoints.containsKey(member)){
            final LeaderEndpoint endpoint = new LeaderEndpoint(this.transformer, this.lock);
            
            this.endpoints.put(member, endpoint);        
            
            endpoint.connectInbound(new BucketHandler() {                
                public void handle(Bucket message) {
                    Leader.this.deliver(endpoint, message);
                }
            }); 
            
            boolean isExclusive = lock.isHeldByCurrentThread();
			if (!isExclusive) {
				lock.lock();
			}

			try {
				// send synchronization message to new member
				Operation [] operations = this.model.serialize();
				
				endpoint.send(new SynchronizationBucket(member,endpoint.getRemoteTime(), this.clock.getTime(), operations));
				
			} catch (ObjectException | EndpointException e) {
				throw new LeaderException("Subscription failed. Inconsistent leader state.", e);
			} finally {
				if (!isExclusive) {
					lock.unlock();
				}
			}
        } else {
        	throw new LeaderException("Member is already subscribed.");
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
    
    private void deliver(LeaderEndpoint originEndpoint, Bucket bucket){        
        this.clock.tick();
        
        try {
            // TODO: set recovery point on model            
        	this.model.apply(bucket.getOperations());    
        	
        	if(this.handler != null){
	        	for (Operation operation: bucket.getOperations()){
	        		handler.handleOperation(operation);
	        	}
        	}
            
        } catch (ObjectException|CommandException  e) {
            // TODO: roll already performed changes back to recovery point
            // TODO: send error to member
            
            unsubscribe(bucket.getMember());   
            
            return;
        } 
        
        for(LeaderEndpoint endpoint: this.endpoints.values()){
            if(endpoint != originEndpoint){
                
                //TODO: filter Nil type commands - no need to forward
                // need a deep copy of operations since operations are mutable
                Operation[] operations = new Operation[bucket.getOperations().length];
                for(int i = 0; i < operations.length; i++){
                    operations[i] = bucket.getOperations()[i].clone();
                }
                              
                try {
                    endpoint.send(new UpdateBucket(bucket.getMember(), endpoint.getRemoteTime(), this.clock.getTime(), operations));
                } catch (EndpointException e) {
                    // TODO: unsubscribe - client uuid of endpoint should be obtainable from endpoint itself
                }
            }
        }
    }
        
}
