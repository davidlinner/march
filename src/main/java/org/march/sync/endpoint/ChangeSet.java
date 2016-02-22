package org.march.sync.endpoint;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.march.data.model.Operation;
import org.march.data.model.Tools;


public class ChangeSet implements Cloneable, Serializable{
        
    private static final long serialVersionUID = 9031957263780176801L;

    private UUID originReplicaName;

    private int replicaTime, masterTime;
        
    private List<Operation> operations;

    public ChangeSet(){
    }    
    
    public ChangeSet(UUID originReplicaName, int replicaTime, int masterTime,
                     List<Operation> operations) {
        super();
        this.originReplicaName = originReplicaName;
        this.replicaTime = replicaTime;
        this.masterTime = masterTime;
        this.operations = operations;
    }

    public UUID getOriginReplicaName() {
        return originReplicaName;
    }

    public void setOriginReplicaName(UUID originReplicaName) {
        this.originReplicaName = originReplicaName;
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
                "originReplicaName=" + originReplicaName +
                ", replicaTime=" + replicaTime +
                ", masterTime=" + masterTime +
                ", operations=" + operations +
                '}';
    }

    @Override
    public ChangeSet clone() {
        return new ChangeSet(this.getOriginReplicaName(), this.getReplicaTime(), this.getMasterTime(), Tools.clone(this.getOperations()));
    }

    public boolean isEmpty(){
       return this.operations == null || this.operations.isEmpty();
    }

}
