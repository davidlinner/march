package org.march.sync.channel;

import java.util.concurrent.locks.ReentrantLock;

import org.march.sync.transform.Transformer;

public class LeaderChannel extends TransformingChannel {

    public LeaderChannel(Transformer transformer, ReentrantLock lock) {
        super(transformer, lock);
    }
    
    public LeaderChannel(Transformer transformer) {
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
