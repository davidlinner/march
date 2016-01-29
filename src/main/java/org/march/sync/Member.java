package org.march.sync;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.march.data.Command;
import org.march.data.CommandException;
import org.march.data.Data;
import org.march.data.Model;
import org.march.data.ObjectException;
import org.march.data.Operation;
import org.march.data.Pointer;
import org.march.data.simple.SimpleModel;
import org.march.sync.endpoint.*;
import org.march.sync.transform.Transformer;


public class Member {
    // add state
    private UUID name;

    private Clock clock;

    private MemberEndpoint channel;

    private Model model;

    private HashSet<OperationHandler> commandHandlers = new HashSet<OperationHandler>();

    private BucketHandler<UpdateBucket> bucketHandler;

    public Member(UUID name, Transformer transformer){
        this.name = name;

        clock   = new Clock();
        channel = new MemberEndpoint(transformer);

        model   = new SimpleModel();
    }

    public void initialize(BaseBucket bucket) throws MemberException {
        try {
            model.apply(bucket.getOperations());
            channel.setRemoteTime(bucket.getLeaderTime());
            clock.setTime(bucket.getMemberTime());
        } catch (ObjectException | CommandException e) {
            throw new MemberException("Cannot initialize member.", e);
        }
    }

    public void onBucket(BucketHandler<UpdateBucket> bucketHandler){
        this.bucketHandler = bucketHandler;
    }

    public synchronized void apply(Pointer pointer, Command command) throws MemberException{
        try {
            model.apply(pointer, command);

            UpdateBucket bucket = new UpdateBucket(this.name, clock.tick(), channel.getRemoteTime(),
                    new Operation[]{new Operation(pointer, command)});

            bucket = channel.send(bucket);

            this.bucketHandler.handle(null, bucket);
        } catch (EndpointException e) {
            //TODO: check reinit on channelexception
            throw new MemberException(e);
        } catch (ObjectException|CommandException e){
            throw new MemberException(e);
        }
    }

    public synchronized void update(UpdateBucket bucket) throws MemberException {
        try {


            bucket = channel.receive(bucket);

            for(Operation operation: bucket.getOperations()){
                model.apply(operation.getPointer(), operation.getCommand());

                for(OperationHandler handler: Member.this.commandHandlers){
                    handler.handleOperation(operation);
                }
            }
        } catch (ObjectException|CommandException  e) {
            throw new MemberException("Cannot apply changes to state consistently.", e);
        } catch (EndpointException e) {
            throw new MemberException("Failed to contextualize bucket.", e);
        }
    }

    public synchronized Data find(Pointer pointer, String identifier)
            throws ObjectException, CommandException {
        return model.find(pointer, identifier);
    }

    public synchronized Data find(Pointer pointer, int index) throws ObjectException,
            CommandException {
        return model.find(pointer, index);
    }

    public void onCommand(OperationHandler... handlers){
        this.commandHandlers.addAll(Arrays.asList(handlers));
    }

    public void offCommand(OperationHandler... handlers){
        if(handlers.length == 0){
            this.commandHandlers.clear();
        }
    }
    
}