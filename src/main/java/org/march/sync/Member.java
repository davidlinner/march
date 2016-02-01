package org.march.sync;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.UUID;

import org.march.data.Command;
import org.march.data.CommandException;
import org.march.data.Data;
import org.march.data.Model;
import org.march.data.ObjectException;
import org.march.data.Operation;
import org.march.data.Pointer;
import org.march.data.simple.SimpleModel;
import org.march.sync.context.*;
import org.march.sync.endpoint.*;
import org.march.sync.member.*;
import org.march.sync.transform.Transformer;


public class Member {
    // add state
    private UUID name;

    private Clock clock;

    private LeaderContext context;

    private Model model;

    private HashSet<Listener> listeners = new HashSet<Listener>();

    private BucketListener bucketListener;

    private State state = State.INITIALIZED;

    private BucketEndpoint bucketEndpoint;

    private final static EnumSet<State> ACCEPT_UPDATES = EnumSet.of(State.SHARING,State.TERMINATING);

    public Member(BucketEndpoint bucketEndpoint){
        this.clock  = new Clock();
        this.bucketEndpoint = bucketEndpoint;
    }

    public synchronized void open(Transformer transformer) throws MemberException {

        if(!State.INITIALIZED.equals(state)) throw new MemberException("Already opened.");

        this.context = new LeaderContext(transformer);
        this.model   = new SimpleModel();

        bucketEndpoint.addReceiveListener(this.bucketListener = new BucketListener() {
            @Override
            public void deliver(UUID member, Bucket bucket) throws BucketDeliveryException {

                if(!(member.equals(name) || bucket instanceof BaseBucket)) throw new BucketDeliveryException("Not the right receiver.");
                try {
                    Member.this.update(bucket);
                } catch (MemberException e) {
                    throw new BucketDeliveryException("Cannot deliver received bucket.", e);
                }
            }
        });

        this.state = State.READY;
    }

    public synchronized void close(){
        state = State.TERMINATING;

        for(Listener listener : listeners){
            if(listener instanceof CloseListener) ((CloseListener) listener).closed(this);
        }

        this.bucketEndpoint.removeReceiveListener(this.bucketListener);
        this.bucketListener = null;
    }

//    public void onBucket(BucketListener bucketListener) throws MemberException {
//        if(!State.INITIALIZED.equals(state)) throw new MemberException("Can only set bucket handler when sharing was not yet started.");
//        this.bucketListener = bucketListener;
//    }

    public synchronized void apply(Pointer pointer, Command command) throws MemberException{
        if(!State.SHARING.equals(state)) throw new MemberException("Can only change data when sharing.");

        try {
            model.apply(pointer, command);

            UpdateBucket bucket = new UpdateBucket(this.name, clock.tick(), context.getRemoteTime(),
                    new Operation[]{new Operation(pointer, command)});

            context.include(bucket);

            //this.bucketListener.deliver(null, bucket);
            this.bucketEndpoint.deliver(this.name, bucket);
        } catch (ContextException e) {
            throw new MemberException(e);
        } catch (ObjectException|CommandException e){
            throw new MemberException(e);
        } catch (BucketDeliveryException e) {
            throw new MemberException("Cannot commit changes.", e);
        }
    }

    private synchronized void update(Bucket bucket) throws MemberException {

        if(bucket instanceof BaseBucket){
            if (!State.READY.equals(state)) throw new MemberException("Cannot set data. Member is not ready yet or already closed.");

            try {
                name = bucket.getMember();
                model.apply(bucket.getOperations());
                context.setRemoteTime(bucket.getLeaderTime());
                clock.setTime(bucket.getMemberTime());
            } catch (ObjectException | CommandException e) {
                throw new MemberException("Cannot initialize member.", e);
            }

            state = State.SHARING;

            for(Listener listener : listeners){
                if(listener instanceof OpenListener) ((OpenListener) listener).opened(this);
            }

        } else if(bucket instanceof UpdateBucket) {
            if (!ACCEPT_UPDATES.contains(state)) throw new MemberException("Can only accept updates when sharing data or terminating.");

            try {
                bucket = this.context.adapt((UpdateBucket) bucket);

                model.apply(bucket.getOperations());

                for(Listener listener : listeners){
                    if(listener instanceof ChangeListener) ((ChangeListener) listener).changed(this, bucket.getOperations());
                }


                if(State.TERMINATING.equals(state) && context.isEmpty()){
                    //out buffer is empty, a buckets have been confirmed by server
                    state = State.TERMINATED;

                    for(Listener listener : listeners){
                        if(listener instanceof CloseListener) ((CloseListener) listener).closed(this);
                    }
                }

            } catch (ObjectException | CommandException e) {
                throw new MemberException("Cannot apply changes to state consistently.", e);
            } catch (ContextException e) {
                throw new MemberException("Failed to contextualize bucket.", e);
            }
        }
    }

    public synchronized Data find(Pointer pointer, String identifier)
            throws ObjectException, CommandException {

        // todo: log warning wenn reading is performed while member is not yet sharing data
        return model.find(pointer, identifier);
    }

    public synchronized Data find(Pointer pointer, int index) throws ObjectException, CommandException {
        // todo: log warning wenn reading is performed while member is not yet sharing data
        return model.find(pointer, index);
    }

    public synchronized void addListener(Listener... listeners){
        this.listeners.addAll(Arrays.asList(listeners));
    }

    public synchronized void removeListener(Listener... listeners){
        if(listeners.length == 0){
            this.listeners.clear();
        } else {
            this.listeners.removeAll(Arrays.asList(listeners));
        }
    }
    
}