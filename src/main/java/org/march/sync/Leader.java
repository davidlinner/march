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
import org.march.sync.context.*;
import org.march.sync.endpoint.*;
import org.march.sync.leader.LeaderException;
import org.march.sync.leader.OperationHandler;
import org.march.sync.transform.Transformer;

public class Leader {
    
    private HashMap<UUID, MemberContext> contexts;
   
    private Transformer transformer;
    
    private Clock clock;
    
    private Model model;

    private OperationHandler operationHandler;

    private BucketListener bucketListener;

    private BucketEndpoint bucketEndpoint;

    private State state = State.INITIALIZED;

    public Leader(BucketEndpoint bucketEndpoint){
        this.contexts       = new HashMap<UUID, MemberContext>();
        this.clock          = new Clock();
        this.model          = new SimpleModel();

        this.bucketEndpoint = bucketEndpoint;

    }
    
    public synchronized void share(Operation [] operations, Transformer transformer, OperationHandler operationHandler) throws LeaderException {

        if (!State.INITIALIZED.equals(state))
            throw new LeaderException("Cannot reset data once sharing was started.");

        this.transformer    = transformer;

        this.operationHandler = operationHandler;

        try {
            this.model.apply(operations);
        } catch (ObjectException | CommandException e) {
            throw new LeaderException("Cannot load operations into a consistent state representation model.", e);
        }

        this.bucketEndpoint.addReceiveListener(this.bucketListener = new BucketListener() {
            @Override
            public void deliver(UUID member, Bucket bucket) throws BucketDeliveryException {
                try {
                    Leader.this.update(bucket);
                } catch (LeaderException e) {
                    throw new BucketDeliveryException("Cannot deliver received bucket.", e);
                }
            }
        });

        state = State.SHARING;
    }

    public synchronized void unshare(){
        //todo: think of a graceful shutdown - this will kill locally comitted changes without saving them
        for(UUID member: contexts.keySet()){
            unregister(member);
        }

        this.bucketEndpoint.removeReceiveListener(this.bucketListener);

        this.bucketListener = null;
        this.operationHandler = null;

        state = State.TERMINATED;
    }
    
    public synchronized Operation[] read() {
        return this.model.serialize();
    }

    public synchronized void register(UUID member) throws LeaderException{

        if (state != State.SHARING) throw new LeaderException("Can only add members when actively sharing data.");

        if (!contexts.containsKey(member)) {
            final MemberContext endpoint = new MemberContext(this.transformer);
            this.contexts.put(member, endpoint);

            BaseBucket base = new BaseBucket(member, endpoint.getRemoteTime(), this.clock.getTime(), this.model.serialize());
            try {
                this.bucketEndpoint.deliver(member, base);
            } catch (BucketDeliveryException e) {
                throw new LeaderException("Cannot initialize new member.", e);
            }
        } else {
            throw new LeaderException("Member is already subscribed.");
        }
    }
    
    public synchronized void unregister(UUID member){
        MemberContext endpoint = contexts.remove(member);
    }

    private synchronized void update(Bucket bucket) throws LeaderException {

        if(!(bucket instanceof UpdateBucket))
            throw new LeaderException("Resetting full state from remote is not allowed.");

        if (!State.SHARING.equals(state))
            throw new LeaderException("Can only accept updates when actively sharing data.");

        MemberContext originContext = contexts.get(bucket.getMember());

        if (originContext == null)
            throw new LeaderException(String.format("Member '%s' is unknown.", bucket.getMember()));


        try {
            bucket = originContext.adapt((UpdateBucket) bucket);

            model.test(bucket.getOperations());

            this.clock.tick();

            model.apply(bucket.getOperations());

            if (this.operationHandler != null) {
                for (Operation operation : bucket.getOperations()) {
                    operationHandler.handleOperation(operation);
                }
            }

        } catch (ContextException e) {

            unregister(bucket.getMember());

            throw new LeaderException("Cannot contextualize bucket.", e);
        } catch (ObjectException | CommandException e) {

            unregister(bucket.getMember());

            throw new LeaderException("Changes cannot be applied.", e);
        }

        for (Map.Entry<UUID, MemberContext> entry: contexts.entrySet()) {
            UUID member             = entry.getKey();
            MemberContext context = entry.getValue();

            if (context != originContext) {
                // need a deep copy of operations since operations are mutable - prune Nil
                LinkedList<Operation> operations = new LinkedList<Operation>();
                for (Operation operation: bucket.getOperations()) {
                    if(!(operation.getCommand() instanceof Nil)) operations.add(operation.clone());
                }

                try {
                    UpdateBucket update = new UpdateBucket(
                            bucket.getMember(),
                            context.getRemoteTime(),
                            this.clock.getTime(),
                            operations.toArray(new Operation[operations.size()]));

                    context.include(update);

                    this.bucketEndpoint.deliver(member, update);

                } catch (ContextException | BucketDeliveryException e) {
                    unregister(member);
                    //todo: collect errors or log them
                }
            }
        }
    }

    /*
     * # Leader(transformer)
     * ## control
     * load(operations, transformer, listener)
     * read
     * unload
     * register
     * unregister
     *
     * ## link
     * onBucket((bucket)->{})
     * adapt(bucket)
     *
     * TODO:
     * - rename interface methods on bucketEndpoint and bucketEndpoint itself - think of name Leader and Member
     * - separate state enums for member and leader
     * - code server controller [mo night]



     * - refactor client code ... deserialization??? [tue]
     * - write client controller [wed]
     * - make client shippable through bower / git [thu]
     * - create sparc binding an simple demo [fri]
     *
     * else:
     * - make a multi module maven project
     * - rename stuff and clear git history
     */
        
}
