package org.march.sync.replica;

import java.util.Arrays;
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
import org.march.sync.Clock;
import org.march.sync.context.*;
import org.march.sync.channel.*;
import org.march.sync.transform.Transformer;


public class Replica {
    // add state
    private UUID name;

    private Clock clock;

    private MasterBacklog context;

    private Model model;

    private HashSet<Listener> listeners = new HashSet<Listener>();

    private ChannelListener channelListener;

    private ReplicaState state = ReplicaState.INACTIVE;

    private Channel channel;

    public Replica(Channel channel){
        this.clock  = new Clock();
        this.channel = channel;
    }

    public synchronized void open(Transformer transformer) throws ReplicaException {

        if(state != ReplicaState.INACTIVE) throw new ReplicaException("Already opened.");

        this.context = new MasterBacklog(transformer);
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

    public synchronized void close(){
        state = ReplicaState.DEACTIVATING;

        for(Listener listener : listeners){
            if(listener instanceof CloseListener) ((CloseListener) listener).closed(this);
        }

        this.channel.removeReceiveListener(this.channelListener);
        this.channelListener = null;
    }

    public synchronized void apply(Pointer pointer, Command command) throws ReplicaException {
        if(state != ReplicaState.ACTIVE) throw new ReplicaException("Can only change data when sharing.");

        try {
            model.apply(pointer, command);

            ChangeSet changeSet = new ChangeSet(this.name, clock.tick(), context.getRemoteTime(),
                    new Operation[]{new Operation(pointer, command)});

            context.append(changeSet);

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
            //if (!state == ReplicaState.ACTIVATING) throw new ReplicaException("Cannot set data. Replica is not ready yet or already closed.");

            try {
                name = changeSet.getReplicaName();
                model.apply(changeSet.getOperations());
                context.setRemoteTime(changeSet.getMasterTime());
                clock.setTime(changeSet.getReplicaTime());
            } catch (ObjectException | CommandException e) {
                throw new ReplicaException("Cannot initialize member.", e);
            }

            state = ReplicaState.ACTIVE;

            for(Listener listener : listeners){
                if(listener instanceof OpenListener) ((OpenListener) listener).opened(this);
            }

        } else {
            if (!ReplicaState.isSynchronizing(state)) throw new ReplicaException("Can only accept updates when sharing data or terminating.");

            try {
                changeSet = this.context.update(changeSet);

                model.apply(changeSet.getOperations());

                for(Listener listener : listeners){
                    if(listener instanceof ChangeListener) ((ChangeListener) listener).changed(this, changeSet.getOperations());
                }


                if(state == ReplicaState.DEACTIVATING && context.isEmpty()){
                    //out buffer is empty, all change sets have been confirmed by server
                    state = ReplicaState.DEACTIVATED;

                    for(Listener listener : listeners){
                        if(listener instanceof CloseListener) ((CloseListener) listener).closed(this);
                    }
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