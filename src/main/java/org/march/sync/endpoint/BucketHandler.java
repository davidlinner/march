package org.march.sync.endpoint;


import java.util.UUID;

public interface BucketHandler {
    void handle(UUID member, Bucket bucket);
}
