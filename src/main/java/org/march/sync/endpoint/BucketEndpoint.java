package org.march.sync.endpoint;

/**
 * Created by dli on 01.02.2016.
 */
public interface BucketEndpoint extends BucketListener {
    void addReceiveListener(BucketListener handler);
    void removeReceiveListener(BucketListener handler);
}
