package org.march.sync.channel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

import org.march.data.Operation;
import org.march.data.Tools;


public class ChangeSet implements Cloneable, Serializable{
        
    private static final long serialVersionUID = 9031957263780176801L;

    private UUID replicaName;

    private int replicaTime, masterTime;
        
    private Operation[] operations;

    public ChangeSet(){
    }    
    
    public ChangeSet(UUID replicaName, int replicaTime, int masterTime,
                     Operation[] operations) {
        super();
        this.replicaName = replicaName;
        this.replicaTime = replicaTime;
        this.masterTime = masterTime;
        this.operations = operations;
    }

    public UUID getReplicaName() {
        return replicaName;
    }

    public void setReplicaName(UUID replicaName) {
        this.replicaName = replicaName;
    }

    public int getReplicaTime() {
        return replicaTime;
    }

    public void setReplicaTime(int replicaTime) {
        this.replicaTime = replicaTime;
    }

    public int getMasterTime() {
        return masterTime;
    }

    public void setMasterTime(int masterTime) {
        this.masterTime = masterTime;
    }

    public Operation[] getOperations() {
        return operations;
    }

    public void setOperations(Operation[] operations) {
        this.operations = operations;
    }

//    public ChangeSet memberTime(int memberTime){
//        this.replicaTime = memberTime;
//        return this;
//    }

    @Override
    public String toString() {
        return "ChangeSet [replicaName=" + replicaName + ", replicaTime=" + replicaTime
                + ", masterTime=" + masterTime + ", operations="
                + Arrays.toString(operations) + "]";
    }

    @Override
    public ChangeSet clone() {
        return new ChangeSet(this.getReplicaName(), this.getReplicaTime(), this.getMasterTime(), Tools.clone(this.getOperations()));
    }
}
