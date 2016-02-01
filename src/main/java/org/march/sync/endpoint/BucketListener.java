package org.march.sync.endpoint;


import java.util.UUID;

public interface BucketListener {
    void deliver(UUID member, Bucket bucket) throws BucketDeliveryException;
}
