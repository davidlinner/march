package org.march.sync.replica;

/**
 * Created by dli on 28.01.2016.
 */
public enum ReplicaState {

    INACTIVE,
    ACTIVATING,
    ACTIVE,
    DEACTIVATING,
    DEACTIVATED,
    INVALID;

    public static boolean isAcceptingRemoteChanges(ReplicaState state){
        return ACTIVE.equals(state) || DEACTIVATING.equals(state);
    }
}
