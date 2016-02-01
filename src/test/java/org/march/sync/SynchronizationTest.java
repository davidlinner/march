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
import org.march.sync.context.*;
import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.BucketDeliveryException;
import org.march.sync.endpoint.BucketEndpoint;
import org.march.sync.endpoint.BucketListener;
import org.march.sync.leader.LeaderException;
import org.march.sync.member.MemberException;
import org.march.sync.transform.Transformer;

public class SynchronizationTest {

    private UUID name1 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private UUID name2 = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private Leader leader;
    private Member member1, member2;

    private StringConstant a = new StringConstant("A");
    private StringConstant b = new StringConstant("B");

    PipeBucketEndpoint client1, client2, server1, server2;

    static Transformer TRANSFORMER = new Transformer();

    @Before
    public void setup() throws LeaderException, ContextException, MemberException, BucketDeliveryException {

        // fake network
        server1 = new PipeBucketEndpoint();
        server2 = new PipeBucketEndpoint();
        client1 = new PipeBucketEndpoint();
        client2 = new PipeBucketEndpoint();

        server1.connect(client1);
        client1.connect(server1);

        server2.connect(client2);
        client2.connect(server2);

        // setup
        leader = new Leader(new BucketEndpoint() {
            // fake dispatcher

            @Override
            public void addReceiveListener(BucketListener handler) {
                server1.addReceiveListener(handler);
                server2.addReceiveListener(handler);
            }

            @Override
            public void removeReceiveListener(BucketListener handler) {
                server1.removeReceiveListener(handler);
                server2.removeReceiveListener(handler);
            }

            @Override
            public void deliver(UUID member, Bucket bucket) throws BucketDeliveryException {
                if(name1.equals(member))
                    server1.deliver(member, bucket);
                else
                    server2.deliver(member, bucket);
            }
        });

        member1 = new Member(client1);
        member2 = new Member(client2);


        // initialize
        leader.share(new Operation[0], TRANSFORMER, null);

        member1.open(TRANSFORMER);
        member2.open(TRANSFORMER);

        // join
        leader.register(name1);
        leader.register(name2);

        flushAll();
    }


    private void flushAll() throws BucketDeliveryException {
        client1.flush();
        client2.flush();
        server1.flush();
        server2.flush();
    };

    @Test
    public void testOneWaySynchronization() throws ContextException, MemberException, ObjectException, CommandException, BucketDeliveryException {
        member1.apply(null, new Set("a", a));

        flushAll();

        assertEquals(member2.find(null, "a"), a);
    }

    @Test
    public void testTwoWaySynchronizationNoConflict() throws ContextException, MemberException, ObjectException, CommandException, BucketDeliveryException {
        member1.apply(null, new Set("a", a));
        member2.apply(null, new Set("b", b));

        flushAll();

        assertEquals(member2.find(null, "a"), a);
        assertEquals(member1.find(null, "b"), b);
    }

    @Test
    public void testTwoWaySynchronizationOnConflict() throws ContextException, MemberException, ObjectException, CommandException, BucketDeliveryException {
        member1.apply(null, new Set("a", a));
        member2.apply(null, new Set("a", b));

        assertEquals(a, member1.find(null, "a"));
        assertEquals(b, member2.find(null, "a"));

        flushAll();

        assertEquals(a, member1.find(null, "a"));
        assertEquals(a, member2.find(null, "a"));
    }

}
