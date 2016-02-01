package org.march.sync;

import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.BucketDeliveryException;
import org.march.sync.endpoint.BucketEndpoint;
import org.march.sync.endpoint.BucketListener;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by dli on 01.02.2016.
 */
public class PipeBucketEndpoint implements BucketEndpoint {

    private LinkedList<BucketListener> listeners;

    private LinkedList<Message> buffer;

    private PipeBucketEndpoint endpoint;

    public PipeBucketEndpoint(){
        buffer = new LinkedList<Message>();
        listeners = new LinkedList<BucketListener>();
    }

    public void connect(PipeBucketEndpoint endpoint){
        this.endpoint = endpoint;
    }

    public void disconnect(){
        this.endpoint = null;
    }

    @Override
    public void addReceiveListener(BucketListener handler) {
        listeners.add(handler);
    }

    @Override
    public void removeReceiveListener(BucketListener handler) {
        listeners.remove(handler);
    }

    @Override
    public void deliver(UUID member, Bucket bucket) throws BucketDeliveryException {
        buffer.add(new Message(member, bucket));
    }

    public void flush() throws BucketDeliveryException {
        Message message;
        while((message = buffer.poll()) != null) {
            for (BucketListener listener : endpoint.listeners) {
                listener.deliver(message.member, message.bucket);
            }
        }
    }

    private class Message{
        UUID member;
        Bucket bucket;

        public Message(UUID member, Bucket bucket) {
            this.bucket = bucket;
            this.member = member;
        }
    }

}
