package org.march.sync.replica;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import org.march.data.model.*;
import org.march.data.simple.SimpleModel;
import org.march.sync.Clock;
import org.march.sync.backlog.*;
import org.march.sync.endpoint.*;
import org.march.sync.transform.Transformer;


public class Replica {
    // add state
    private UUID name;

    private Clock clock;

    private MasterBacklog masterBacklog;

    private Model model;

    private HashSet<Listener> listeners = new HashSet<Listener>();

    private ReplicaState state = ReplicaState.INACTIVE;

    private UpdateEndpoint endpoint;

    public Replica(UpdateEndpoint endpoint){
        this.clock  = new Clock();
        this.endpoint = endpoint;
    }

    public ReplicaState getState() {
        return state;
    }

    public synchronized void activate(Transformer transformer) throws ReplicaException {

        if(state != ReplicaState.INACTIVE) throw new ReplicaException("Already activated.");

        this.masterBacklog = new MasterBacklog(transformer);
        this.model   = new SimpleModel();

        endpoint.setUpdateListener(new UpdateListener() {
            @Override
            public void receive(UUID replicaName, ChangeSet changeSet) throws UpdateException {

                if (state == ReplicaState.ACTIVE && !replicaName.equals(name))
                    throw new UpdateException("Not the right receiver.");
                try {
                    Replica.this.update(changeSet);
                } catch (ReplicaException e) {
                    throw new UpdateException("Cannot process received changeSet.", e);
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
            this.endpoint.send(this.name, new ChangeSet(
                    this.name,
                    this.clock.getTime(),
                    masterBacklog.getRemoteTime(),
                    Collections.emptyList()));
        } catch (UpdateException e) {
             throw new ReplicaException("Cannot send empty change set.", e);
        }
    }

    public synchronized void invalidate(){
        state = ReplicaState.INVALID;

        for(Listener listener : listeners){
            if(listener instanceof InvalidationListener) ((InvalidationListener) listener).invalidated(this);
        }

        this.endpoint.setUpdateListener(null);
    }

    private synchronized void update(ChangeSet changeSet) throws ReplicaException {

        if(state == ReplicaState.ACTIVATING){
            //if (!state == ReplicaState.ACTIVATING) throw new ReplicaException("Cannot set data. Replica is not ready yet or already deactivated.");

            try {
                name = changeSet.getOriginReplicaName();
                model.apply(Tools.asArray(changeSet.getOperations()));

                masterBacklog.setRemoteTime(changeSet.getMasterTime());

                clock.setTime(changeSet.getReplicaTime());
            } catch (ObjectException | CommandException e) {
                throw new ReplicaException("Cannot initialize replica.", e);
            }

            state = ReplicaState.ACTIVE;

            for(Listener listener : listeners){
                if(listener instanceof ActivationListener) ((ActivationListener) listener).activated(this);
            }

        } else {
            if (!ReplicaState.isAcceptingRemoteChanges(state)) throw new ReplicaException("Can only receive updates when sharing data or terminating.");

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
                    deactivated();
                }

            } catch (ObjectException | CommandException e) {
                throw new ReplicaException("Cannot apply changes to state consistently.", e);
            } catch (BacklogException e) {
                throw new ReplicaException("Failed to contextualize changeSet.", e);
            }
        }
    }

    private void deactivated(){
        state = ReplicaState.DEACTIVATED;

        for(Listener listener : listeners){
            if(listener instanceof DeactivationListener) ((DeactivationListener) listener).deactivated(this);
        }

        this.endpoint.setUpdateListener(null);
    }

    public synchronized void apply(Pointer pointer, Command command) throws ReplicaException {
        if(state != ReplicaState.ACTIVE) throw new ReplicaException("Can only change data when sharing.");

        try {
            model.apply(pointer, command);

            ChangeSet changeSet = new ChangeSet(this.name, clock.tick(), masterBacklog.getRemoteTime(),
                    Tools.asList(new Operation(pointer, command)));

            masterBacklog.append(changeSet);

            //this.channelListener.receive(null, changeSet);
            this.endpoint.send(this.name, changeSet);
        } catch (BacklogException e) {
            throw new ReplicaException(e);
        } catch (ObjectException|CommandException e){
            throw new ReplicaException(e);
        } catch (UpdateException e) {
            throw new ReplicaException("Cannot receive changes.", e);
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