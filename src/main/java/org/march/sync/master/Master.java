package org.march.sync.master;

import java.util.*;

import org.march.data.*;
import org.march.data.command.Nil;
import org.march.data.simple.SimpleModel;
import org.march.sync.Clock;
import org.march.sync.UncertainTemporalRelationException;
import org.march.sync.backlog.*;
import org.march.sync.channel.*;
import org.march.sync.transform.Transformer;

public class Master {
    
    private HashMap<UUID, ReplicaBacklog> backlogs;
   
    private Transformer transformer;
    
    private Clock clock;
    
    private Model model;

    private OperationListener operationListener;

    private ChannelListener channelListener;

    private Channel channel;

    private MasterState state = MasterState.INACTIVE;

    public Master(Channel channel){
        this.backlogs       = new HashMap<UUID, ReplicaBacklog>();
        this.clock          = new Clock();
        this.model          = new SimpleModel();

        this.channel = channel;
    }
    
    public synchronized void activate(List<Operation> operations, Transformer transformer, OperationListener operationListener) throws MasterException {

        if (!MasterState.INACTIVE.equals(state))
            throw new MasterException("Cannot reset data once sharing was started.");

        this.transformer    = transformer;

        this.operationListener = operationListener;

        try {
            this.model.apply(operations.toArray(new Operation[operations.size()]));
        } catch (ObjectException | CommandException e) {
            throw new MasterException("Cannot load operations into a consistent state representation model.", e);
        }

        this.channel.addReceiveListener(this.channelListener = new ChannelListener() {
            @Override
            public void send(UUID member, ChangeSet changeSet) throws ChannelException {
                try {
                    Master.this.update(changeSet);
                } catch (MasterException e) {
                    throw new ChannelException("Cannot send received changeSet.", e);
                }
            }
        });

        state = MasterState.ACTIVE;
    }

    public synchronized void deactivate() throws MasterException {
        this.channel.removeReceiveListener(this.channelListener);

        this.channelListener = null;
        this.operationListener = null;

        state = MasterState.DEACTIVATED;
    }

    public synchronized Set<UUID> registrations(){
        return new HashSet<UUID>(backlogs.keySet());
    }
    
    public synchronized List<Operation> read() {
        return this.model.serialize();
    }

    public synchronized void register(UUID replicaName) throws MasterException {

        if (state != MasterState.ACTIVE) throw new MasterException("Can only add members when actively sharing data.");

        if (!backlogs.containsKey(replicaName)) {
            ReplicaBacklog backlog = new ReplicaBacklog(this.transformer);
            backlog.setRemoteTime(Clock.getClockStart());

            this.backlogs.put(replicaName, backlog);

            ChangeSet base = new ChangeSet(replicaName, backlog.getRemoteTime(), this.clock.getTime(), this.model.serialize());
            try {
                this.channel.send(replicaName, base);
            } catch (ChannelException e) {
                throw new MasterException("Cannot initialize new replicaName.", e);
            }
        } else {
            throw new MasterException("Replica is already subscribed.");
        }
    }
    
    public synchronized void unregister(UUID replicaName){
        ReplicaBacklog replicaBacklog = backlogs.remove(replicaName);
    }

    private synchronized void update(ChangeSet changeSet) throws MasterException {

        if (!MasterState.ACTIVE.equals(state))
            throw new MasterException("Can only accept updates when actively sharing data.");

        ReplicaBacklog originBacklog = backlogs.get(changeSet.getReplicaName());

        if (originBacklog == null)
            throw new MasterException(String.format("Replica '%s' is unknown.", changeSet.getReplicaName()));

        if(changeSet.isEmpty()){
            // replica is challenging an update of the time
            try {
                this.channel.send(changeSet.getReplicaName(), new ChangeSet(
                        changeSet.getReplicaName(),
                        originBacklog.getRemoteTime(),
                        this.clock.getTime(),
                        Collections.emptyList()));

                return;
            } catch (ChannelException e) {
                unregister(changeSet.getReplicaName());
                throw new MasterException("Cannot send a clearing set.", e);
            }
        }

        // put change set into context and apply to master state
        try {
            changeSet = originBacklog.update(changeSet);

            model.test(Tools.asArray(changeSet.getOperations()));

            this.clock.tick();

            model.apply(Tools.asArray(changeSet.getOperations()));

            if (this.operationListener != null) {
                operationListener.update(Tools.asArray(changeSet.getOperations()));
            }

        } catch (BacklogException e) {

            unregister(changeSet.getReplicaName());

            throw new MasterException("Cannot contextualize changeSet.", e);
        } catch (ObjectException | CommandException e) {

            unregister(changeSet.getReplicaName());

            throw new MasterException("Changes cannot be applied.", e);
        }

        // put change set into context of other replicas and send
        for (Map.Entry<UUID, ReplicaBacklog> entry: backlogs.entrySet()) {
            UUID replicaName       = entry.getKey();
            ReplicaBacklog backlog = entry.getValue();

            if (backlog != originBacklog) {
                // need a deep copy of operations since operations are mutable - prune Nil
                LinkedList<Operation> operations = new LinkedList<Operation>();
                for (Operation operation: changeSet.getOperations()) {
                    if(!(operation.getCommand() instanceof Nil)) operations.add(operation.clone());
                }

                try {
                    ChangeSet update = new ChangeSet(
                            changeSet.getReplicaName(),
                            backlog.getRemoteTime(),
                            this.clock.getTime(),
                            operations);

                    backlog.append(update);

                    this.channel.send(replicaName, update);

                } catch (BacklogException | ChannelException e) {
                    unregister(replicaName);
                    //todo: collect errors or log them
                }
            }
        }

        // in case of a strong deviation of clocks send an empty message to origin replica to update clocks (effectively clear queues)
//        try {
//            if(Clock.lag(this.clock.getTime(), originBacklog.getLocalTime()) > 50) {
//                this.channel.send(changeSet.getReplicaName(), new ChangeSet(
//                        changeSet.getReplicaName(),
//                        originBacklog.getRemoteTime(),
//                        this.clock.getTime(),
//                        new Operation[0]));
//            }
//        } catch (UncertainTemporalRelationException e) {
//            unregister(changeSet.getReplicaName());
//
//            throw new MasterException("Cannot determine lag.", e);
//        } catch (ChannelException e) {
//            unregister(changeSet.getReplicaName());
//
//            throw new MasterException("Cannot send a clearing set.", e);
//        }
    }
        
}
