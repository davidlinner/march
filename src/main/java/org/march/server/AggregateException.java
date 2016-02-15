package org.march.server;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dli on 15.02.2016.
 */
public class AggregateException extends Exception {

    private LinkedList<Exception> exceptions = new LinkedList<>();

    public AggregateException() {
    }

    public AggregateException(String message) {
        super(message);
    }

    public void addException(Exception e){
        exceptions.add(e);
    }

    public List<Exception> getExceptions(){
        return Collections.unmodifiableList(exceptions);
    }
}
