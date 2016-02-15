package org.march.server;

import org.march.server.channel.*;
import org.march.data.Resource;
import org.march.data.ResourceConnector;
import org.march.sync.channel.ChangeSet;
import org.march.sync.channel.CommitException;
import org.march.sync.master.Master;
import org.march.sync.master.MasterException;
import org.march.sync.master.MasterState;
import org.march.sync.transform.Transformer;

import java.util.*;

public class Server {

    private ResourceConnector resourceConnector;

    private Map<String, Transformer> transformers;

    private Map<String, Master> masters;

    private Map<UUID, MessageChannel> routes;

    private Map<MessageChannel, MessageListener> messageListeners;

    public Server(){
        this.transformers = Collections.synchronizedMap(new HashMap<>());

        this.masters = Collections.synchronizedMap(new HashMap<>());

        this.routes = Collections.synchronizedMap(new HashMap<>());

        this.messageListeners = Collections.synchronizedMap(new HashMap<>());
    }

    public void start(){
        //todo: change state
        // start scheduler thread maintenance
    }

    public void stop(){
        //overkill
    }

    public void addClient(MessageChannel messageChannel){
        // todo: check state

        MessageListener messageListener = new MessageListener() {
            @Override
            public void send(Message message) {
                Server.this.receive(messageChannel, message);
            }
        };

        messageListeners.put(messageChannel, messageListener);
        messageChannel.addMessageListener(messageListener);
    }

    public void removeClient(MessageChannel messageChannel){
        MessageListener messageListener = messageListeners.remove(messageChannel);
        if(messageListener != null){
            messageChannel.removeMessageListener(messageListener);

            //todo: look for all know routes for replicas, remove routes and unregister replicas from master
        } //todo: else throw server exception
    }

    public void addTransformer(String schema, Transformer transformer){
        transformers.put(schema, transformer);
    }

    public void removeTransformer(String schema){
        transformers.remove(schema);
    }

    public ResourceConnector getResourceConnector() {
        return resourceConnector;
    }

    public void setResourceConnector(ResourceConnector resourceConnector) {
        this.resourceConnector = resourceConnector;
    }

    private void receive(MessageChannel channel, Message message){
        if(message instanceof CommitMessage){
            receive(channel, (CommitMessage)message);
        } else if (message instanceof ReplicateMessage){
            receive(channel, (ReplicateMessage)message);
        }
    }

    private void receive(MessageChannel channel, CommitMessage commitMessage){
        String scope = commitMessage.getScope();
        Master master = masters.get(scope);
        try {
            AbstractCommitChannel commitChannel = (AbstractCommitChannel)master.getCommitChannel();
            commitChannel.delegate(commitMessage.getReplicaName(), commitMessage.getChangeSet());
        } catch (CommitException e) {
            //todo: commit error message 5x
        }
    }

    private void receive(MessageChannel channel, ReplicateMessage replicateMessage){
        Master master = null;

        synchronized (masters) {
            if (masters.get(replicateMessage.getScope()).getState() != MasterState.ACTIVE) {
                //todo: commit error or avoid at all 4x
            } else if (!masters.containsKey(replicateMessage.getScope())) {

                String scope = replicateMessage.getScope();
                Resource resource = resourceConnector.get(scope);
                if (resource == null) {
                    //todo: try commit error message 4x
                }

                Transformer transformer = transformers.get(resource.getType());
                if (transformer == null) {
                    // todo: try sending error message 4x
                }

                master = new Master(new AbstractCommitChannel() {
                    @Override
                    public void commit(UUID replicaName, ChangeSet changeSet) throws CommitException {
                        MessageChannel channel = Server.this.routes.get(replicaName);
                        channel.send(new CommitMessage(scope, replicaName, changeSet));
                    }
                });

                masters.put(scope, master);

                try {
                    master.activate(resource, transformer);
                } catch (MasterException e) {
                    // todo: commit error message 5x
                }
            }
        }

        this.routes.put(replicateMessage.getReplicaName(), channel);

        try {
            master.register(replicateMessage.getReplicaName());
        } catch (MasterException e) {
            //todo: commit error message
        }
    }

}
