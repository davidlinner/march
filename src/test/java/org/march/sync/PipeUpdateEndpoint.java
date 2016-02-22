package org.march.sync;

import org.march.sync.endpoint.*;
import org.march.sync.endpoint.UpdateEndpoint;
import org.march.sync.endpoint.UpdateListener;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by dli on 01.02.2016.
 */
public class PipeUpdateEndpoint implements UpdateEndpoint {

    private LinkedList<UpdateListener> listeners;

    private LinkedList<Message> buffer;

    private PipeUpdateEndpoint endpoint;

    public PipeUpdateEndpoint(){
        buffer = new LinkedList<Message>();
        listeners = new LinkedList<UpdateListener>();
    }

    public void connect(PipeUpdateEndpoint endpoint){
        this.endpoint = endpoint;
    }

    public void disconnect(){
        this.endpoint = null;
    }

    @Override
    public void setUpdateListener(UpdateListener handler) {
        listeners.add(handler);
    }

    @Override
    public void send(UUID member, ChangeSet changeSet) throws UpdateException {
        buffer.add(new Message(member, changeSet));
    }

    public void flush() throws UpdateException {
        Message message;
        while((message = buffer.poll()) != null) {
            for (UpdateListener listener : endpoint.listeners) {
                listener.receive(message.member, message.changeSet);
            }
        }
    }

    private class Message{
        UUID member;
        ChangeSet changeSet;

        public Message(UUID member, ChangeSet changeSet) {
            this.changeSet = changeSet;
            this.member = member;
        }
    }

}
