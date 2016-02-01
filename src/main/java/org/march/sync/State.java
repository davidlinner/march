package org.march.sync;

/**
 * Created by dli on 28.01.2016.
 */
public enum State {

    INITIALIZED,
    READY,
    SHARING,
    TERMINATING,
    TERMINATED;

    public static boolean isReceiving(State state){
        return SHARING.equals(state) || TERMINATING.equals(state);
    }
}
