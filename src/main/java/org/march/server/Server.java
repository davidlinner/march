package org.march.server;

import org.march.server.endpoint.*;
import org.march.data.Resource;
import org.march.data.ResourceConnector;
import org.march.sync.endpoint.ChangeSet;
import org.march.sync.endpoint.UpdateException;
import org.march.sync.master.DuplicateRegistrationException;
import org.march.sync.master.Master;
import org.march.sync.master.MasterException;
import org.march.sync.master.MasterState;
import org.march.sync.transform.Transformer;

import java.util.*;

public class Server {

    private ResourceConnector resourceConnector;

    private Map<String, Transformer> transformers;

    private Map<String, Master> masters;

    private Map<UUID, MessageEndpoint> routes;

    public Server(){
        this.transformers = Collections.synchronizedMap(new HashMap<>());

        this.masters = Collections.synchronizedMap(new HashMap<>());

        this.routes = Collections.synchronizedMap(new HashMap<>());

    }

    public void start(){
        //todo: change state
        // start scheduler thread maintenance
    }

    public void stop(){
        //overkill
    }

    public void addClient(MessageEndpoint messageEndpoint){
        // todo: check state

        MessageListener messageListener = new MessageListener() {
            @Override
            public void receive(Message message) {
                Server.this.receive(messageEndpoint, message);
            }
        };

        messageEndpoint.setMessageListener(messageListener);
    }

    public void removeClient(MessageEndpoint messageEndpoint){
        messageEndpoint.setMessageListener(null);

        //todo: look for all know routes for replicas, remove routes and unregister replicas from master
        //todo: else throw server exception
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

    private void receive(MessageEndpoint endpoint, Message message){
        if(message instanceof UpdateMessage){
            receive(endpoint, (UpdateMessage)message);
        } else if (message instanceof ReplicateMessage){
            receive(endpoint, (ReplicateMessage)message);
        }
    }

    private void receive(MessageEndpoint endpoint, UpdateMessage updateMessage){
        String scope = updateMessage.getScope();
        Master master = masters.get(scope);
        try {
            AbstractUpdateEndpoint updateEndpoint = (AbstractUpdateEndpoint)master.getEndpoint();
            updateEndpoint.receive(updateMessage.getReplicaName(), updateMessage.getChangeSet());
        } catch (UpdateException e) {
            endpoint.send(
                    new ErrorMessage(
                            updateMessage.getReplicaName(),
                            String.format("Cannot delegate update for replica '%s'.", updateMessage.getReplicaName()),
                            ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

    private void receive(MessageEndpoint endpoint, ReplicateMessage replicateMessage){
        Master master = null;

        synchronized (masters) {
            if (masters.containsKey(replicateMessage.getScope()) &&
                    masters.get(replicateMessage.getScope()).getState() != MasterState.ACTIVE) {

                // todo: sort out when this situation could occur
                endpoint.send(new ErrorMessage(replicateMessage.getReplicaName(), "Master not active.", ErrorCode.METHOD_NOT_ALLOWED));

            } else if (!masters.containsKey(replicateMessage.getScope())) {

                String scope = replicateMessage.getScope();
                Resource resource = resourceConnector.get(scope);
                if (resource == null) {
                    endpoint.send(
                            new ErrorMessage(
                                    replicateMessage.getReplicaName(),
                                    String.format("Resource '%s' not found.", replicateMessage.getScope()),
                                    ErrorCode.RESOURCE_NOT_FOUND));

                }

                Transformer transformer = transformers.get(resource.getType());
                if (transformer == null) {
                    endpoint.send(
                            new ErrorMessage(
                                    replicateMessage.getReplicaName(),
                                    String.format("No transformer for operations on '%s' found.", replicateMessage.getScope()),
                                    ErrorCode.UNSUPPORTED_MEDIA_TYPE));
                }

                master = new Master(new AbstractUpdateEndpoint() {
                    @Override
                    public void send(UUID replicaName, ChangeSet changeSet) throws UpdateException {
                        MessageEndpoint channel = Server.this.routes.get(replicaName);
                        channel.send(new UpdateMessage(scope, replicaName, changeSet));
                    }
                });

                masters.put(scope, master);

                try {
                    master.activate(resource, transformer);
                } catch (DuplicateRegistrationException e) {
                    endpoint.send(
                            new ErrorMessage(
                                    replicateMessage.getReplicaName(),
                                    String.format("Replica already registered '%s'.", replicateMessage.getScope()),
                                    ErrorCode.DUPLICATE_REGISTRATION));
                } catch (MasterException e) {
                    endpoint.send(
                            new ErrorMessage(
                                    replicateMessage.getReplicaName(),
                                    String.format("Cannot initialize replication master for '%s'.", replicateMessage.getScope()),
                                    ErrorCode.INTERNAL_SERVER_ERROR));
                }
            }
        }

        this.routes.put(replicateMessage.getReplicaName(), endpoint);

        try {
            master.register(replicateMessage.getReplicaName());
        } catch (MasterException e) {
            endpoint.send(
                    new ErrorMessage(
                            replicateMessage.getReplicaName(),
                            String.format("Failed to register replica for '%s'.", replicateMessage.getScope()),
                            ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

    private void receive(MessageEndpoint endpoint, ErrorMessage errorMessage){
        //todo: something smart
    }

}
