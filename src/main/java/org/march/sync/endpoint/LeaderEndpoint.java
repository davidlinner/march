package org.march.sync.endpoint;

import java.util.concurrent.locks.ReentrantLock;

import org.march.sync.transform.Transformer;

public class LeaderEndpoint extends Endpoint {

    public LeaderEndpoint(Transformer transformer, ReentrantLock lock) {
        super(transformer, lock);
    }

    @Override
    protected int getLocalTime(Bucket message) {
        return message.getLeaderTime();
    }

    @Override
    protected void setLocalTime(Bucket message, int time) {
        message.setLeaderTime(time);
        
    }

    @Override
    protected int getRemoteTime(Bucket message) {
        return message.getMemberTime();
    }

    @Override
    protected void setRemoteTime(Bucket message, int time) {
        message.setMemberTime(time);        
    }
}
