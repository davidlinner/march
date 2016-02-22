package org.march.sync;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.march.data.Resource;
import org.march.data.model.CommandException;
import org.march.data.model.ObjectException;
import org.march.data.model.Operation;
import org.march.data.model.StringConstant;
import org.march.data.command.Set;
import org.march.sync.endpoint.UpdateException;
import org.march.sync.endpoint.UpdateListener;
import org.march.sync.backlog.*;
import org.march.sync.endpoint.ChangeSet;
import org.march.sync.endpoint.UpdateEndpoint;
import org.march.sync.master.Master;
import org.march.sync.master.MasterException;
import org.march.sync.replica.Replica;
import org.march.sync.replica.ReplicaException;
import org.march.sync.transform.Transformer;

public class SynchronizationTest {

    private UUID name1 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private UUID name2 = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private Master master;
    private Replica replica1, replica2;

    private StringConstant a = new StringConstant("A");
    private StringConstant b = new StringConstant("B");

    PipeUpdateEndpoint client1, client2, server1, server2;

    static Transformer TRANSFORMER = new Transformer();

    @Before
    public void setup() throws MasterException, BacklogException, ReplicaException, UpdateException {

        // fake network
        server1 = new PipeUpdateEndpoint();
        server2 = new PipeUpdateEndpoint();
        client1 = new PipeUpdateEndpoint();
        client2 = new PipeUpdateEndpoint();

        server1.connect(client1);
        client1.connect(server1);

        server2.connect(client2);
        client2.connect(server2);

        // setup
        master = new Master(new UpdateEndpoint() {
            // fake dispatcher

            @Override
            public void setUpdateListener(UpdateListener handler) {
                server1.setUpdateListener(handler);
                server2.setUpdateListener(handler);
            }

            @Override
            public void send(UUID member, ChangeSet changeSet) throws UpdateException {
                if(name1.equals(member))
                    server1.send(member, changeSet);
                else
                    server2.send(member, changeSet);
            }
        });

        replica1 = new Replica(client1);
        replica2 = new Replica(client2);

        // initialize
        master.activate(new Resource() {
            @Override
            public String getType() {
                return "";
            }

            @Override
            public List<Operation> getData() {
                return Collections.emptyList();
            }

            @Override
            public void setData(List<Operation> operations) {

            }

            @Override
            public void update(Operation... operations) {

            }
        }, TRANSFORMER);

        replica1.activate(TRANSFORMER);
        replica2.activate(TRANSFORMER);

        // join
        master.register(name1);
        master.register(name2);

        flushAll();
    }


    private void flushAll() throws UpdateException {
        client1.flush();
        client2.flush();
        server1.flush();
        server2.flush();
    };

    @Test
    public void testOneWaySynchronization() throws BacklogException, ReplicaException, ObjectException, CommandException, UpdateException {
        replica1.apply(null, new Set("a", a));

        flushAll();

        assertEquals(replica2.find(null, "a"), a);
    }

    @Test
    public void testTwoWaySynchronizationNoConflict() throws BacklogException, ReplicaException, ObjectException, CommandException, UpdateException {
        replica1.apply(null, new Set("a", a));
        replica2.apply(null, new Set("b", b));

        flushAll();

        assertEquals(replica2.find(null, "a"), a);
        assertEquals(replica1.find(null, "b"), b);
    }

    @Test
    public void testTwoWaySynchronizationOnConflict() throws BacklogException, ReplicaException, ObjectException, CommandException, UpdateException {
        replica1.apply(null, new Set("a", a));
        replica2.apply(null, new Set("a", b));

        assertEquals(a, replica1.find(null, "a"));
        assertEquals(b, replica2.find(null, "a"));

        flushAll();

        assertEquals(a, replica1.find(null, "a"));
        assertEquals(a, replica2.find(null, "a"));
    }

}
