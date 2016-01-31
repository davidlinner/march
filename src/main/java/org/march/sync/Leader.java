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
import org.march.sync.endpoint.*;
import org.march.sync.transform.Transformer;

public class Leader {
    
    private HashMap<UUID, LeaderEndpoint> endpoints;    
   
    private Transformer transformer;
    
    private Clock clock;
    
    private Model model;

    private OperationHandler operationHandler;

    private BucketHandler bucketHandler;

    private State state = State.INITIALIZED;

    public Leader(Transformer transformer){
        this.endpoints      = new HashMap<UUID, LeaderEndpoint>();
        this.clock          = new Clock();
        
        this.transformer    = transformer;
        this.model          = new SimpleModel();

    }
    
    public synchronized void share(Operation [] operations, OperationHandler operationHandler) throws LeaderException {

        if (state != State.INITIALIZED)
            throw new LeaderException("Cannot reset data once sharing was started.");


        this.operationHandler = operationHandler;

        try {
            this.model.apply(operations);
        } catch (ObjectException | CommandException e) {
            throw new LeaderException("Cannot load operations into a consistent state representation model.", e);
        }

        state = State.SHARING;

    }

    public synchronized void unshare(){
        //todo: think of a graceful shutdown - this will kill locally comitted changes without saving them
        for(UUID member: endpoints.keySet()){
            unregister(member);
        }

        state = State.TERMINATED;

        this.operationHandler = null;
    }
    
    public synchronized Operation[] read() {
        return this.model.serialize();
    }

    public synchronized void onBucket(BucketHandler bucketHandler) throws LeaderException {
        if(this.state != State.INITIALIZED) throw new LeaderException("Can only change bucket listener before start sharing.");
        this.bucketHandler = bucketHandler;
    }

    public synchronized void register(UUID member) throws LeaderException{

        if (state != State.SHARING) throw new LeaderException("Can only add members when actively sharing data.");

        if (!endpoints.containsKey(member)) {
            final LeaderEndpoint endpoint = new LeaderEndpoint(this.transformer);
            this.endpoints.put(member, endpoint);

            BaseBucket base = new BaseBucket(member, endpoint.getRemoteTime(), this.clock.getTime(), this.model.serialize());
            bucketHandler.handle(member, base);
        } else {
            throw new LeaderException("Member is already subscribed.");
        }
    }
    
    public synchronized void unregister(UUID member){
        LeaderEndpoint endpoint = endpoints.remove(member);
    }

    public synchronized void update(Bucket bucket) throws LeaderException {

        if(!(bucket instanceof UpdateBucket))
            throw new LeaderException("Resetting full state from remote is not allowed.");

        if (!State.SHARING.equals(state))
            throw new LeaderException("Can only accept updates when actively sharing data.");

        LeaderEndpoint originEndpoint = endpoints.get(bucket.getMember());

        if (originEndpoint == null) {
            throw new LeaderException(String.format("Member '%s' is unknown.", bucket.getMember()));
        }

        try {
            bucket = originEndpoint.receive((UpdateBucket)bucket);

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
                    UpdateBucket update = endpoint.send(
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
     * # Leader(transformer)
     * ## control
     * load(operations, transformer, listener)
     * read
     * close
     * register
     * unregister
     *
     * ## link
     * onBucket((bucket)->{})
     * update(bucket)
     *
     * TODO:
     * - tidy up the interface chaos - separate concern on leader and member level (not endpoint) [mo noon]
     * - rename interface methods on endpoint and endpoint itself - think of name Leader and Member
     * - code server controller [mo night]
     * - refactor client code ... [tue]
     * - write client controller [wed]
     * - make client shippable through bower / git [thu]
     * - create sparc binding an simple demo [fri]
     */
        
}
