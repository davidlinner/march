package org.march.sync.endpoint;

import java.util.concurrent.locks.ReentrantLock;

import org.march.sync.transform.Transformer;

public class MemberEndpoint extends Endpoint {

    public MemberEndpoint(Transformer transformer) {
        super(transformer);
    }
    
    public MemberEndpoint(Transformer transformer, ReentrantLock lock) {
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
