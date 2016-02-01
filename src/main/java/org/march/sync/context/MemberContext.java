package org.march.sync.context;

import org.march.sync.endpoint.Bucket;
import org.march.sync.transform.Transformer;

public class MemberContext extends Context {

    public MemberContext(Transformer transformer) {
        super(transformer);
    }

    @Override
    protected int getLocalTime(Bucket message) {
        return message.getLeaderTime();
    }

    @Override
    protected void setLocalTime(Bucket bucket, int time) {
        bucket.setLeaderTime(time);
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
