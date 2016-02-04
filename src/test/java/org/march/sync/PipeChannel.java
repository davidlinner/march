package org.march.sync;

import org.march.sync.channel.ChangeSet;
import org.march.sync.channel.ChannelException;
import org.march.sync.channel.ChannelListener;
import org.march.sync.channel.Channel;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by dli on 01.02.2016.
 */
public class PipeChannel implements Channel {

    private LinkedList<ChannelListener> listeners;

    private LinkedList<Message> buffer;

    private PipeChannel endpoint;

    public PipeChannel(){
        buffer = new LinkedList<Message>();
        listeners = new LinkedList<ChannelListener>();
    }

    public void connect(PipeChannel endpoint){
        this.endpoint = endpoint;
    }

    public void disconnect(){
        this.endpoint = null;
    }

    @Override
    public void addReceiveListener(ChannelListener handler) {
        listeners.add(handler);
    }

    @Override
    public void removeReceiveListener(ChannelListener handler) {
        listeners.remove(handler);
    }

    @Override
    public void send(UUID member, ChangeSet changeSet) throws ChannelException {
        buffer.add(new Message(member, changeSet));
    }

    public void flush() throws ChannelException {
        Message message;
        while((message = buffer.poll()) != null) {
            for (ChannelListener listener : endpoint.listeners) {
                listener.send(message.member, message.changeSet);
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
