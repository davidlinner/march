package org.march.sync.channel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.march.data.Operation;
import org.march.data.Tools;
import org.march.data.command.Nil;


public class ChangeSet implements Cloneable, Serializable{
        
    private static final long serialVersionUID = 9031957263780176801L;

    private UUID replicaName;

    private int replicaTime, masterTime;
        
    private List<Operation> operations;

    public ChangeSet(){
    }    
    
    public ChangeSet(UUID replicaName, int replicaTime, int masterTime,
                     List<Operation> operations) {
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

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

//    public ChangeSet memberTime(int memberTime){
//        this.replicaTime = memberTime;
//        return this;
//    }


    @Override
    public String toString() {
        return "ChangeSet{" +
                "replicaName=" + replicaName +
                ", replicaTime=" + replicaTime +
                ", masterTime=" + masterTime +
                ", operations=" + operations +
                '}';
    }

    @Override
    public ChangeSet clone() {
        return new ChangeSet(this.getReplicaName(), this.getReplicaTime(), this.getMasterTime(), Tools.clone(this.getOperations()));
    }

    public boolean isEmpty(){
       return this.operations == null || this.operations.isEmpty();
    }

}
