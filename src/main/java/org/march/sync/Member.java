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
import org.march.sync.endpoint.*;
import org.march.sync.transform.Transformer;


public class Member {
    // add state
    private UUID name;

    private Clock clock;

    private MemberEndpoint channel;

    private Model model;

    private HashSet<Listener> listeners = new HashSet<Listener>();

    private BucketHandler bucketHandler;

    private State state = State.INITIALIZED;

    private final static EnumSet<State> ACCEPT_UPDATES = EnumSet.of(State.SHARING,State.TERMINATING);

    public Member(Transformer transformer){
        clock   = new Clock();
        channel = new MemberEndpoint(transformer);

        model   = new SimpleModel();
    }

    public synchronized void close(){
        state = State.TERMINATING;

        for(Listener listener : listeners){
            if(listener instanceof CloseListener) ((CloseListener) listener).closed(this);
        }

    }

    public void onBucket(BucketHandler bucketHandler) throws MemberException {
        if(!State.INITIALIZED.equals(state)) throw new MemberException("Can only set bucket handler when sharing was not yet started.");
        this.bucketHandler = bucketHandler;
    }

    public synchronized void apply(Pointer pointer, Command command) throws MemberException{
        if(!State.SHARING.equals(state)) throw new MemberException("Can only change data when sharing.");

        try {
            model.apply(pointer, command);

            UpdateBucket bucket = new UpdateBucket(this.name, clock.tick(), channel.getRemoteTime(),
                    new Operation[]{new Operation(pointer, command)});

            bucket = channel.send(bucket);

            this.bucketHandler.handle(null, bucket);
        } catch (EndpointException e) {
            throw new MemberException(e);
        } catch (ObjectException|CommandException e){
            throw new MemberException(e);
        }
    }

    public synchronized void update(Bucket bucket) throws MemberException {

        if(bucket instanceof BaseBucket){
            if (!State.INITIALIZED.equals(state)) throw new MemberException("Already initialized. Cannot reset data.");

            try {
                name = bucket.getMember();
                model.apply(bucket.getOperations());
                channel.setRemoteTime(bucket.getLeaderTime());
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
                bucket = channel.receive((UpdateBucket)bucket);

                model.apply(bucket.getOperations());

                for(Listener listener : listeners){
                    if(listener instanceof ChangeListener) ((ChangeListener) listener).changed(this, bucket.getOperations());
                }


                if(State.TERMINATING.equals(state) && channel.isEmpty()){
                    //out buffer is empty, a buckets have been confirmed by server
                    state = State.TERMINATED;

                    for(Listener listener : listeners){
                        if(listener instanceof CloseListener) ((CloseListener) listener).closed(this);
                    }
                }

            } catch (ObjectException | CommandException e) {
                throw new MemberException("Cannot apply changes to state consistently.", e);
            } catch (EndpointException e) {
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