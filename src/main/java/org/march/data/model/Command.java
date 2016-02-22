package org.march.data.model;

import java.io.Serializable;


public interface Command extends Cloneable, Serializable {
    Command clone();
}
