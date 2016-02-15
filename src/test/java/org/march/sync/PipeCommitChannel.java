package org.march.sync;

import org.march.sync.channel.ChangeSet;
import org.march.sync.channel.CommitException;
import org.march.sync.channel.CommitListener;
import org.march.sync.channel.CommitChannel;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by dli on 01.02.2016.
 */
public class PipeCommitChannel implements CommitChannel {

    private LinkedList<CommitListener> listeners;

    private LinkedList<Message> buffer;

    private PipeCommitChannel endpoint;

    public PipeCommitChannel(){
        buffer = new LinkedList<Message>();
        listeners = new LinkedList<CommitListener>();
    }

    public void connect(PipeCommitChannel endpoint){
        this.endpoint = endpoint;
    }

    public void disconnect(){
        this.endpoint = null;
    }

    @Override
    public void addReceiveListener(CommitListener handler) {
        listeners.add(handler);
    }

    @Override
    public void removeReceiveListener(CommitListener handler) {
        listeners.remove(handler);
    }

    @Override
    public void commit(UUID member, ChangeSet changeSet) throws CommitException {
        buffer.add(new Message(member, changeSet));
    }

    public void flush() throws CommitException {
        Message message;
        while((message = buffer.poll()) != null) {
            for (CommitListener listener : endpoint.listeners) {
                listener.commit(message.member, message.changeSet);
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
