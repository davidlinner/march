package org.march.sync.replica;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import org.march.data.*;
import org.march.data.simple.SimpleModel;
import org.march.sync.Clock;
import org.march.sync.UncertainTemporalRelationException;
import org.march.sync.backlog.*;
import org.march.sync.channel.*;
import org.march.sync.transform.Transformer;


public class Replica {
    // add state
    private UUID name;

    private Clock clock;

    private MasterBacklog masterBacklog;

    private Model model;

    private HashSet<Listener> listeners = new HashSet<Listener>();

    private ChannelListener channelListener;

    private ReplicaState state = ReplicaState.INACTIVE;

    private Channel channel;

    public Replica(Channel channel){
        this.clock  = new Clock();
        this.channel = channel;
    }

    public synchronized void activate(Transformer transformer) throws ReplicaException {

        if(state != ReplicaState.INACTIVE) throw new ReplicaException("Already opened.");

        this.masterBacklog = new MasterBacklog(transformer);
        this.model   = new SimpleModel();

        channel.addReceiveListener(this.channelListener = new ChannelListener() {
            @Override
            public void send(UUID replicaName, ChangeSet changeSet) throws ChannelException {

                if(state == ReplicaState.ACTIVE && !replicaName.equals(name)) throw new ChannelException("Not the right receiver.");
                try {
                    Replica.this.update(changeSet);
                } catch (ReplicaException e) {
                    throw new ChannelException("Cannot send received changeSet.", e);
                }
            }
        });

        this.state = ReplicaState.ACTIVATING;
    }

    public synchronized void deactivate() throws ReplicaException {
        state = ReplicaState.DEACTIVATING;

        for(Listener listener : listeners){
            if(listener instanceof DeactivatingListener) ((DeactivatingListener) listener).deactivating(this);
        }

        try {
            this.channel.send(this.name, new ChangeSet(
                                this.name,
                                this.clock.getTime(),
                                masterBacklog.getRemoteTime(),
                                Collections.emptyList()));
        } catch (ChannelException e) {
             throw new ReplicaException("Cannot send empty change set.", e);
        }
    }

    public synchronized void apply(Pointer pointer, Command command) throws ReplicaException {
        if(state != ReplicaState.ACTIVE) throw new ReplicaException("Can only change data when sharing.");

        try {
            model.apply(pointer, command);

            ChangeSet changeSet = new ChangeSet(this.name, clock.tick(), masterBacklog.getRemoteTime(),
                    Tools.asList(new Operation(pointer, command)));

            masterBacklog.append(changeSet);

            //this.channelListener.send(null, changeSet);
            this.channel.send(this.name, changeSet);
        } catch (BacklogException e) {
            throw new ReplicaException(e);
        } catch (ObjectException|CommandException e){
            throw new ReplicaException(e);
        } catch (ChannelException e) {
            throw new ReplicaException("Cannot commit changes.", e);
        }
    }

    private synchronized void update(ChangeSet changeSet) throws ReplicaException {

        if(state == ReplicaState.ACTIVATING){
            //if (!state == ReplicaState.ACTIVATING) throw new ReplicaException("Cannot set data. Replica is not ready yet or already deactivated.");

            try {
                name = changeSet.getReplicaName();
                model.apply(Tools.asArray(changeSet.getOperations()));

                masterBacklog.setRemoteTime(changeSet.getMasterTime());

                clock.setTime(changeSet.getReplicaTime());
            } catch (ObjectException | CommandException e) {
                throw new ReplicaException("Cannot initialize member.", e);
            }

            state = ReplicaState.ACTIVE;

            for(Listener listener : listeners){
                if(listener instanceof OpenListener) ((OpenListener) listener).opened(this);
            }

        } else {
            if (!ReplicaState.isAcceptingRemoteChanges(state)) throw new ReplicaException("Can only accept updates when sharing data or terminating.");

            try {
                changeSet = this.masterBacklog.update(changeSet);

                if(!changeSet.isEmpty()){
                    model.apply(Tools.asArray(changeSet.getOperations()));
                    for(Listener listener : listeners){
                        if(listener instanceof ChangeListener) ((ChangeListener) listener).changed(this, Tools.asArray(changeSet.getOperations()));
                    }
                }

                if(state == ReplicaState.DEACTIVATING && masterBacklog.isEmpty()){
                    //out buffer is empty, all change sets have been confirmed by server
                    state = ReplicaState.DEACTIVATED;

                    for(Listener listener : listeners){
                        if(listener instanceof DeactivatedListener) ((DeactivatedListener) listener).deactivated(this);
                    }

                    this.channel.removeReceiveListener(this.channelListener);
                }

            } catch (ObjectException | CommandException e) {
                throw new ReplicaException("Cannot apply changes to state consistently.", e);
            } catch (BacklogException e) {
                throw new ReplicaException("Failed to contextualize changeSet.", e);
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