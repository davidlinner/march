package org.march.sync;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.march.data.CommandException;
import org.march.data.Model;
import org.march.data.ObjectException;
import org.march.data.Operation;
import org.march.data.command.Nil;
import org.march.data.simple.SimpleModel;
import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.LeaderEndpoint;
import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.BucketHandler;
import org.march.sync.endpoint.SynchronizationBucket;
import org.march.sync.endpoint.UpdateBucket;
import org.march.sync.transform.Transformer;

public class Leader {
    
    private HashMap<UUID, LeaderEndpoint> endpoints;    
   
    private Transformer transformer;
    
    private Clock clock;
    
    private Model model;

    private OperationHandler operationHandler;

    private BucketHandler bucketHandler;

    private LeaderState state = LeaderState.INITIALIZED;

    public Leader(Transformer transformer){
        this.endpoints      = new HashMap<UUID, LeaderEndpoint>();
        this.clock          = new Clock();
        
        this.transformer    = transformer;
        this.model          = new SimpleModel();

    }
    
    public synchronized void share(Operation [] operations, OperationHandler operationHandler) throws LeaderException {

        if (state != LeaderState.INITIALIZED)
            throw new LeaderException("Cannot reset data once sharing was started.");


        this.operationHandler = operationHandler;

        try {
            this.model.apply(operations);
        } catch (ObjectException | CommandException e) {
            throw new LeaderException("Cannot load operations into a consistent state representation model.", e);
        }

        state = LeaderState.SHARING;

    }

    public synchronized void unshare(){
        for(UUID member: endpoints.keySet()){
            unregister(member);
        }

        state = LeaderState.READ_ONLY;

        this.operationHandler = null;
    }
    
    public Operation[] read(){
    	try {
			return this.model.serialize();
		} catch (ObjectException e) {
			//TODO: add debug logging here - must not happen / no reasonable treatment 
		}
    	
    	return null;
    }

    public synchronized void onBucket(BucketHandler bucketHandler) throws LeaderException {
        if(this.state != LeaderState.INITIALIZED) throw new LeaderException("Can only change bucket listener before start sharing.");
        this.bucketHandler = bucketHandler;
    }

    public synchronized void register(UUID member) throws LeaderException{

        if (state != LeaderState.SHARING) throw new LeaderException("Can only add members when actively sharing data.");

        if (!endpoints.containsKey(member)) {
            final LeaderEndpoint endpoint = new LeaderEndpoint(this.transformer);

            this.endpoints.put(member, endpoint);

//            try {
//                // push synchronization message for new member into buffer
//                Operation[] operations = this.model.serialize();
//
//                Bucket bucket = endpoint.send(new SynchronizationBucket(member, endpoint.getRemoteTime(), this.clock.getTime(), operations));
//
//                this.bucketHandler.handle(bucket);
//            } catch (ObjectException | EndpointException e) {
//                throw new LeaderException("Adding member failed. Inconsistent leader state.", e);
//            }
        } else {
            throw new LeaderException("Member is already subscribed.");
        }

    }
    
    public synchronized void unregister(UUID member){
        LeaderEndpoint endpoint = endpoints.remove(member);
    }

    public synchronized void update(Bucket bucket) throws LeaderException {

        if (state != LeaderState.SHARING) throw new LeaderException("Can only add members when actively sharing data.");

        LeaderEndpoint originEndpoint = endpoints.get(bucket.getMember());

        if (originEndpoint == null) {
            throw new LeaderException(String.format("Member '%s' is unknown.", bucket.getMember()));
        }

        try {
            bucket = originEndpoint.receive(bucket);

            model.test(bucket.getOperations());

            this.clock.tick();

            model.apply(bucket.getOperations());

            if (this.operationHandler != null) {
                for (Operation operation : bucket.getOperations()) {
                    operationHandler.handleOperation(operation);
                }
            }

        } catch (EndpointException e) {

            unregister(bucket.getMember());

            throw new LeaderException("Cannot contextualize bucket.", e);
        } catch (ObjectException | CommandException e) {

            unregister(bucket.getMember());

            throw new LeaderException("Changes cannot be applied.", e);
        }

        for (Map.Entry<UUID, LeaderEndpoint> entry: endpoints.entrySet()) {
            UUID member             = entry.getKey();
            LeaderEndpoint endpoint = entry.getValue();

            if (endpoint != originEndpoint) {
                // need a deep copy of operations since operations are mutable - prune Nil
                LinkedList<Operation> operations = new LinkedList<Operation>();
                for (Operation operation: bucket.getOperations()) {
                    if(!(operation.getCommand() instanceof Nil)) operations.add(operation.clone());
                }

                try {
                    Bucket update = endpoint.send(
                                    new UpdateBucket(
                                        bucket.getMember(),
                                        endpoint.getRemoteTime(),
                                        this.clock.getTime(),
                                        operations.toArray(new Operation[operations.size()])));

                    bucketHandler.handle(member, update);

                } catch (EndpointException e) {
                    unregister(member);
                }
            }
        }
    }

    /*
     * Leader(transformer)
     * setData(operations)
     * getData
     * onBucket((bucket)->{})
     * add(member)
     * update(bucket)
     * remove(member)
     *
     * TODO:
     * - find a solution for initialization / synch messages
     * - tidy up the interface chaos - separate concern on leader and member level (not endpoint)
     * - rename interface methods on endpoint and endpoint itself - think of name Leader and Member
     * - add proper state management at Member and thread safety
     */
        
}
