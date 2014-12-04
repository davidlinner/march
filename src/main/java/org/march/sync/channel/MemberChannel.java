package org.march.sync.channel;

import java.util.concurrent.locks.ReentrantLock;

import org.march.sync.transform.Transformer;

public class MemberChannel extends TransformingChannel {

    public MemberChannel(Transformer transformer) {
        super(transformer);
    }
    
    public MemberChannel(Transformer transformer, ReentrantLock lock) {
        super(transformer, lock);
    }

    @Override
    protected int getLocalTime(Message message) {
        return message.getMemberTime();
    }

    @Override
    protected void setLocalTime(Message message, int time) {        
        message.setMemberTime(time);
    }

    @Override
    protected int getRemoteTime(Message message) {
        return message.getLeaderTime();
    }

    @Override
    protected void setRemoteTime(Message message, int time) {
        message.setLeaderTime(time);        
    }    
}
