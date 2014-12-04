package org.march.sync.channel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;


public class Message implements Cloneable, Serializable{
        
    private static final long serialVersionUID = 9031957263780176801L;

    private UUID member;

    private int memberTime, leaderTime;
        
    private Operation[] operations;
    
    public Message (){        
    }    
    
    public Message(UUID member, int memberTime, int leaderTime,
            Operation[] operations) {
        super();
        this.member = member;
        this.memberTime = memberTime;
        this.leaderTime = leaderTime;
        this.operations = operations;
    }

    public UUID getMember() {
        return member;
    }

    public void setMember(UUID member) {
        this.member = member;
    }

    public int getMemberTime() {
        return memberTime;
    }

    public void setMemberTime(int memberTime) {
        this.memberTime = memberTime;
    }

    public int getLeaderTime() {
        return leaderTime;
    }

    public void setLeaderTime(int leaderTime) {
        this.leaderTime = leaderTime;
    }

    public Operation[] getOperations() {
        return operations;
    }

    public void setOperations(Operation[] operations) {
        this.operations = operations;
    }        
    
    public Message clone(){
        return new Message(member, memberTime, leaderTime, operations);
    }
    
    public Message memberTime(int memberTime){
        this.memberTime = memberTime;
        return this;
    }

    @Override
    public String toString() {
        return "Message [member=" + member + ", memberTime=" + memberTime
                + ", leaderTime=" + leaderTime + ", operations="
                + Arrays.toString(operations) + "]";
    }
    
    
}
