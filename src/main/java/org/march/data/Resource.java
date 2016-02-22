package org.march.data;

import org.march.data.model.Operation;

import java.util.List;

/**
 * Created by dli on 14.02.2016.
 */
public interface Resource {
    String getType();

    List<Operation> getData();

    void setData(List<Operation> operations);

    void update(Operation... operations);
}
