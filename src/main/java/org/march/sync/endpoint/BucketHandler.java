package org.march.sync.endpoint;


import java.util.UUID;

public interface BucketHandler <T extends Bucket> {
    void handle(UUID member, T bucket);
}
