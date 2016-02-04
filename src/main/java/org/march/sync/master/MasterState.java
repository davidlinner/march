package org.march.sync.master;

/**
 * Created by dli on 28.01.2016.
 */
public enum MasterState {

    INITIALIZED,
    READY,
    SHARING,
    TERMINATING,
    TERMINATED;

    public static boolean isReceiving(MasterState masterState){
        return SHARING.equals(masterState) || TERMINATING.equals(masterState);
    }
}
