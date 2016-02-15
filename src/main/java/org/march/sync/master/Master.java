package org.march.sync.master;

import java.util.*;

import org.march.data.Resource;
import org.march.data.command.Nil;
import org.march.data.model.*;
import org.march.data.simple.SimpleModel;
import org.march.sync.Clock;
import org.march.sync.backlog.*;
import org.march.sync.channel.*;
import org.march.sync.transform.Transformer;

public class Master {
    
    private HashMap<UUID, ReplicaBacklog> backlogs;
   
    private Transformer transformer;
    
    private Clock clock;
    
    private Model model;

    private CommitListener commitListener;

    private CommitChannel commitChannel;

    private Resource resource;

    private MasterState state = MasterState.INACTIVE;

    public Master(CommitChannel commitChannel){
        this.backlogs       = new HashMap<UUID, ReplicaBacklog>();
        this.clock          = new Clock();
        this.model          = new SimpleModel();

        this.commitChannel = commitChannel;
    }
    
    public synchronized void activate(Resource resource, Transformer transformer) throws MasterException {

        if (!MasterState.INACTIVE.equals(state))
            throw new MasterException("Cannot reset data once sharing was started.");

        this.transformer    = transformer;

        List<Operation> operations = resource.getData();

        try {
            this.model.apply(operations.toArray(new Operation[operations.size()]));
        } catch (ObjectException | CommandException e) {
            throw new MasterException("Cannot load operations into a consistent state representation model.", e);
        }

        this.commitChannel.addReceiveListener(this.commitListener = new CommitListener() {
            @Override
            public void commit(UUID member, ChangeSet changeSet) throws CommitException {
                try {
                    Master.this.update(changeSet);
                } catch (MasterException e) {
                    throw new CommitException("Cannot commit received changeSet.", e);
                }
            }
        });

        state = MasterState.ACTIVE;
    }

    public synchronized void deactivate() throws MasterException {
        this.commitChannel.removeReceiveListener(this.commitListener);

        this.commitListener = null;

        this.resource = null;

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
                this.commitChannel.commit(replicaName, base);
            } catch (CommitException e) {
                throw new MasterException("Cannot initialize new replicaName.", e);
            }
        } else {
            throw new MasterException("Replica is already subscribed.");
        }
    }
    
    public synchronized void unregister(UUID replicaName){
        ReplicaBacklog replicaBacklog = backlogs.remove(replicaName);
    }

    public synchronized MasterState getState(){
        return this.state;
    }

    public CommitChannel getCommitChannel(){
        return this.commitChannel;
    }

    private synchronized void update(ChangeSet changeSet) throws MasterException {

        if (!MasterState.ACTIVE.equals(state))
            throw new MasterException("Can only delegate updates when actively sharing data.");

        ReplicaBacklog originBacklog = backlogs.get(changeSet.getOriginReplicaName());

        if (originBacklog == null)
            throw new MasterException(String.format("Replica '%s' is unknown.", changeSet.getOriginReplicaName()));

        if(changeSet.isEmpty()){
            // replica is challenging an update of the time
            try {
                this.commitChannel.commit(changeSet.getOriginReplicaName(), new ChangeSet(
                        changeSet.getOriginReplicaName(),
                        originBacklog.getRemoteTime(),
                        this.clock.getTime(),
                        Collections.emptyList()));

                return;
            } catch (CommitException e) {
                unregister(changeSet.getOriginReplicaName());
                throw new MasterException("Cannot commit a clearing set.", e);
            }
        }

        // put change set into context and apply to master state
        try {
            changeSet = originBacklog.update(changeSet);

            model.test(Tools.asArray(changeSet.getOperations()));

            this.clock.tick();

            model.apply(Tools.asArray(changeSet.getOperations()));

            this.resource.update(Tools.asArray(changeSet.getOperations()));

        } catch (BacklogException e) {

            unregister(changeSet.getOriginReplicaName());

            throw new MasterException("Cannot contextualize changeSet.", e);
        } catch (ObjectException | CommandException e) {

            unregister(changeSet.getOriginReplicaName());

            throw new MasterException("Changes cannot be applied.", e);
        }

        // put change set into context of other replicas and commit
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
                            changeSet.getOriginReplicaName(),
                            backlog.getRemoteTime(),
                            this.clock.getTime(),
                            operations);

                    backlog.append(update);

                    this.commitChannel.commit(replicaName, update);

                } catch (BacklogException | CommitException e) {
                    unregister(replicaName);
                    //todo: collect errors or log them
                }
            }
        }

        // in case of a strong deviation of clocks commit an empty channel to origin replica to update clocks (effectively clear queues)
//        try {
//            if(Clock.lag(this.clock.getTime(), originBacklog.getLocalTime()) > 50) {
//                this.channel.commit(changeSet.getOriginReplicaName(), new ChangeSet(
//                        changeSet.getOriginReplicaName(),
//                        originBacklog.getRemoteTime(),
//                        this.clock.getTime(),
//                        new Operation[0]));
//            }
//        } catch (UncertainTemporalRelationException e) {
//            unregister(changeSet.getOriginReplicaName());
//
//            throw new MasterException("Cannot determine lag.", e);
//        } catch (ChannelException e) {
//            unregister(changeSet.getOriginReplicaName());
//
//            throw new MasterException("Cannot commit a clearing set.", e);
//        }
    }
}
