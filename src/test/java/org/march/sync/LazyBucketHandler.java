package org.march.sync;

import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.BucketHandler;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by dli on 28.01.2016.
 */
public class LazyBucketHandler implements BucketHandler{

    private LinkedList<Message> queue = new LinkedList<Message>();

    private BucketHandler delegate;

    public LazyBucketHandler(BucketHandler delegate){
        this.delegate = delegate;
    }

    @Override
    public void handle(UUID member,Bucket bucket) {
        queue.add(new Message(member, bucket));
    }

    public void flush(){
        Message message;

        while((message = queue.poll()) != null){
            delegate.handle(message.member, message.bucket);
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
