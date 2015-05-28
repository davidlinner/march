package org.march.sync.endpoint;

import java.util.concurrent.locks.ReentrantLock;

import org.march.sync.transform.Transformer;

public class LeaderEndpoint extends Endpoint {

    public LeaderEndpoint(Transformer transformer, ReentrantLock lock) {
        super(transformer, lock);
    }
    
    public LeaderEndpoint(Transformer transformer) {
        super(transformer);
    }

    @Override
    protected int getLocalTime(Message message) {
        return message.getLeaderTime();
    }

    @Override
    protected void setLocalTime(Message message, int time) {
        message.setLeaderTime(time);
        
    }

    @Override
    protected int getRemoteTime(Message message) {
        return message.getMemberTime();
    }

    @Override
    protected void setRemoteTime(Message message, int time) {
        message.setMemberTime(time);        
    }
}
