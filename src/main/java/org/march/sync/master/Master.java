package org.march.sync.master;

import java.util.*;

import org.march.data.Resource;
import org.march.data.command.Nil;
import org.march.data.model.*;
import org.march.data.simple.SimpleModel;
import org.march.sync.Clock;
import org.march.sync.backlog.*;
import org.march.sync.endpoint.*;
import org.march.sync.transform.Transformer;

public class Master {
    
    private HashMap<UUID, ReplicaBacklog> backlogs;
   
    private Transformer transformer;
    
    private Clock clock;
    
    private Model model;

    private UpdateListener updateListener;

    private UpdateEndpoint endpoint;

    private Resource resource;

    private MasterState state = MasterState.INACTIVE;

    public Master(UpdateEndpoint endpoint){
        this.backlogs       = new HashMap<UUID, ReplicaBacklog>();
        this.clock          = new Clock();
        this.model          = new SimpleModel();

        this.endpoint = endpoint;
    }
    
    public synchronized void activate(Resource resource, Transformer transformer) throws MasterException {

        if (!MasterState.INACTIVE.equals(state))
            throw new MasterException("Cannot reset data once sharing was started.");

        this.transformer    = transformer;
        this.resource       = resource;

        List<Operation> operations = resource.getData();

        try {
            this.model.apply(operations.toArray(new Operation[operations.size()]));
        } catch (ObjectException | CommandException e) {
            throw new MasterException("Cannot load operations into a consistent state representation model.", e);
        }

        this.endpoint.setUpdateListener(this.updateListener = new UpdateListener() {
            @Override
            public void receive(UUID member, ChangeSet changeSet) throws UpdateException {
                try {
                    Master.this.update(changeSet);
                } catch (MasterException e) {
                    throw new UpdateException("Cannot receive received changeSet.", e);
                }
            }
        });

        state = MasterState.ACTIVE;
    }

    public synchronized void deactivate() throws MasterException {
        this.endpoint.setUpdateListener(null);

        this.updateListener = null;

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
                this.endpoint.send(replicaName, base);
            } catch (UpdateException e) {
                throw new MasterException("Cannot initialize new replicaName.", e);
            }
        } else {
            throw new DuplicateRegistrationException("Replica is already subscribed.");
        }
    }
    
    public synchronized void unregister(UUID replicaName){
        ReplicaBacklog replicaBacklog = backlogs.remove(replicaName);
    }

    public synchronized MasterState getState(){
        return this.state;
    }

    public UpdateEndpoint getEndpoint(){
        return this.endpoint;
    }

    private synchronized void update(ChangeSet changeSet) throws MasterException {

        if (!MasterState.ACTIVE.equals(state))
            throw new MasterException("Can only receive updates when actively sharing data.");

        ReplicaBacklog originBacklog = backlogs.get(changeSet.getOriginReplicaName());

        if (originBacklog == null)
            throw new MasterException(String.format("Replica '%s' is unknown.", changeSet.getOriginReplicaName()));

        if(changeSet.isEmpty()){
            // replica is challenging an update of the time
            try {
                this.endpoint.send(changeSet.getOriginReplicaName(), new ChangeSet(
                        changeSet.getOriginReplicaName(),
                        originBacklog.getRemoteTime(),
                        this.clock.getTime(),
                        Collections.emptyList()));

                return;
            } catch (UpdateException e) {
                unregister(changeSet.getOriginReplicaName());
                throw new MasterException("Cannot receive a clearing set.", e);
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

        // put change set into context of other replicas and receive
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

                    this.endpoint.send(replicaName, update);

                } catch (BacklogException | UpdateException e) {
                    unregister(replicaName);
                    //todo: collect errors or log them
                }
            }
        }

        // in case of a strong deviation of clocks receive an empty endpoint to origin replica to update clocks (effectively clear queues)
//        try {
//            if(Clock.lag(this.clock.getTime(), originBacklog.getLocalTime()) > 50) {
//                this.endpoint.receive(changeSet.getOriginReplicaName(), new ChangeSet(
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
//            throw new MasterException("Cannot receive a clearing set.", e);
//        }
    }
}
