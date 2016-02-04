package org.march.sync.master;

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
import org.march.sync.Clock;
import org.march.sync.context.*;
import org.march.sync.channel.*;
import org.march.sync.transform.Transformer;

public class Master {
    
    private HashMap<UUID, ReplicaBacklog> backlogs;
   
    private Transformer transformer;
    
    private Clock clock;
    
    private Model model;

    private OperationHandler operationHandler;

    private ChannelListener channelListener;

    private Channel channel;

    private MasterState state = MasterState.INITIALIZED;

    public Master(Channel channel){
        this.backlogs       = new HashMap<UUID, ReplicaBacklog>();
        this.clock          = new Clock();
        this.model          = new SimpleModel();

        this.channel = channel;
    }
    
    public synchronized void share(Operation [] operations, Transformer transformer, OperationHandler operationHandler) throws MasterException {

        if (!MasterState.INITIALIZED.equals(state))
            throw new MasterException("Cannot reset data once sharing was started.");

        this.transformer    = transformer;

        this.operationHandler = operationHandler;

        try {
            this.model.apply(operations);
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

        state = MasterState.SHARING;
    }

    public synchronized void unshare(){
        //todo: think of a graceful shutdown - this will kill locally comitted changes without notifying replicas
        for(UUID member: backlogs.keySet()){
            unregister(member);
        }

        this.channel.removeReceiveListener(this.channelListener);

        this.channelListener = null;
        this.operationHandler = null;

        state = MasterState.TERMINATED;
    }
    
    public synchronized Operation[] read() {
        return this.model.serialize();
    }

    public synchronized void register(UUID member) throws MasterException {

        if (state != MasterState.SHARING) throw new MasterException("Can only add members when actively sharing data.");

        if (!backlogs.containsKey(member)) {
            final ReplicaBacklog endpoint = new ReplicaBacklog(this.transformer);
            this.backlogs.put(member, endpoint);

            ChangeSet base = new ChangeSet(member, endpoint.getRemoteTime(), this.clock.getTime(), this.model.serialize());
            try {
                this.channel.send(member, base);
            } catch (ChannelException e) {
                throw new MasterException("Cannot initialize new member.", e);
            }
        } else {
            throw new MasterException("Replica is already subscribed.");
        }
    }
    
    public synchronized void unregister(UUID member){
        ReplicaBacklog endpoint = backlogs.remove(member);
    }

    private synchronized void update(ChangeSet changeSet) throws MasterException {

        if (!MasterState.SHARING.equals(state))
            throw new MasterException("Can only accept updates when actively sharing data.");

        ReplicaBacklog originBacklog = backlogs.get(changeSet.getReplicaName());

        if (originBacklog == null)
            throw new MasterException(String.format("Replica '%s' is unknown.", changeSet.getReplicaName()));

        // put change set into context and apply to state
        try {
            changeSet = originBacklog.update(changeSet);

            model.test(changeSet.getOperations());

            this.clock.tick();

            model.apply(changeSet.getOperations());

            if (this.operationHandler != null) {
                for (Operation operation : changeSet.getOperations()) {
                    operationHandler.handleOperation(operation);
                }
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
                            operations.toArray(new Operation[operations.size()]));

                    backlog.append(update);

                    this.channel.send(replicaName, update);

                } catch (BacklogException | ChannelException e) {
                    unregister(replicaName);
                    //todo: collect errors or log them
                }
            }
        }
    }
        
}
