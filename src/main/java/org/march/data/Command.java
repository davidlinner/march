package org.march.data;

import java.io.Serializable;


public interface Command extends Cloneable, Serializable {
    Command clone();
}
