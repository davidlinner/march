package org.march.sync;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.march.data.CommandException;
import org.march.data.ObjectException;
import org.march.data.Operation;
import org.march.data.StringConstant;
import org.march.data.command.Set;
import org.march.sync.channel.ChannelListener;
import org.march.sync.context.*;
import org.march.sync.channel.ChangeSet;
import org.march.sync.channel.ChannelException;
import org.march.sync.channel.Channel;
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

    PipeChannel client1, client2, server1, server2;

    static Transformer TRANSFORMER = new Transformer();

    @Before
    public void setup() throws MasterException, BacklogException, ReplicaException, ChannelException {

        // fake network
        server1 = new PipeChannel();
        server2 = new PipeChannel();
        client1 = new PipeChannel();
        client2 = new PipeChannel();

        server1.connect(client1);
        client1.connect(server1);

        server2.connect(client2);
        client2.connect(server2);

        // setup
        master = new Master(new Channel() {
            // fake dispatcher

            @Override
            public void addReceiveListener(ChannelListener handler) {
                server1.addReceiveListener(handler);
                server2.addReceiveListener(handler);
            }

            @Override
            public void removeReceiveListener(ChannelListener handler) {
                server1.removeReceiveListener(handler);
                server2.removeReceiveListener(handler);
            }

            @Override
            public void send(UUID member, ChangeSet changeSet) throws ChannelException {
                if(name1.equals(member))
                    server1.send(member, changeSet);
                else
                    server2.send(member, changeSet);
            }
        });

        replica1 = new Replica(client1);
        replica2 = new Replica(client2);


        // initialize
        master.share(new Operation[0], TRANSFORMER, null);

        replica1.open(TRANSFORMER);
        replica2.open(TRANSFORMER);

        // join
        master.register(name1);
        master.register(name2);

        flushAll();
    }


    private void flushAll() throws ChannelException {
        client1.flush();
        client2.flush();
        server1.flush();
        server2.flush();
    };

    @Test
    public void testOneWaySynchronization() throws BacklogException, ReplicaException, ObjectException, CommandException, ChannelException {
        replica1.apply(null, new Set("a", a));

        flushAll();

        assertEquals(replica2.find(null, "a"), a);
    }

    @Test
    public void testTwoWaySynchronizationNoConflict() throws BacklogException, ReplicaException, ObjectException, CommandException, ChannelException {
        replica1.apply(null, new Set("a", a));
        replica2.apply(null, new Set("b", b));

        flushAll();

        assertEquals(replica2.find(null, "a"), a);
        assertEquals(replica1.find(null, "b"), b);
    }

    @Test
    public void testTwoWaySynchronizationOnConflict() throws BacklogException, ReplicaException, ObjectException, CommandException, ChannelException {
        replica1.apply(null, new Set("a", a));
        replica2.apply(null, new Set("a", b));

        assertEquals(a, replica1.find(null, "a"));
        assertEquals(b, replica2.find(null, "a"));

        flushAll();

        assertEquals(a, replica1.find(null, "a"));
        assertEquals(a, replica2.find(null, "a"));
    }

}
