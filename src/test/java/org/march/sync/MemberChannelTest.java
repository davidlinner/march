package org.march.sync;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.march.data.Operation;
import org.march.data.Pointer;
import org.march.data.StringConstant;
import org.march.data.command.Insert;
import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.MemberEndpoint;
import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.BucketHandler;
import org.march.sync.endpoint.UpdateBucket;
import org.march.sync.transform.InsertInsertInclusion;
import org.march.sync.transform.Transformer;

public class MemberChannelTest {

    private UUID member0 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private UUID member1 = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private Pointer p = new Pointer(UUID.randomUUID());

    private StringConstant a = new StringConstant("a");
    private StringConstant b = new StringConstant("b");
    private StringConstant c = new StringConstant("c");
    private StringConstant d = new StringConstant("d");

    Operation a0 = new Operation(p, new Insert(0, a));

    Operation b0 = new Operation(p, new Insert(0, b));

    Operation c0 = new Operation(p, new Insert(0, c));
    Operation c2 = new Operation(p, new Insert(2, c));

    Operation d0 = new Operation(p, new Insert(0, d));
    Operation d2 = new Operation(p, new Insert(2, d));

    static Transformer TRANSFORMER = new Transformer();
    static {
        TRANSFORMER.addInclusion(new InsertInsertInclusion());
    }

    private MemberEndpoint channel;


    @Before
    public void setupChannel() throws EndpointException{
        channel = new MemberEndpoint(TRANSFORMER);
    }

    @Test
    public void testMemberChannelSend() throws EndpointException {

        Clock clk = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        UpdateBucket m0 = new UpdateBucket(member0, clk.tick(), 0, ol0);
        UpdateBucket m1 = new UpdateBucket(member0, clk.tick(), 0, ol1);

        channel.send(m0);
        channel.send(m1);

        assertEquals(channel.getRemoteTime(), 0);
    }

    @Test
    public void testMemberChannelReceive() throws EndpointException {

        Clock clk = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        UpdateBucket m0 = new UpdateBucket(member0, 0, clk.tick(), ol0);
        UpdateBucket m1 = new UpdateBucket(member0, 0, clk.tick(), ol1);

        channel.receive(m0);
        channel.receive(m1);

        assertEquals(channel.getRemoteTime(), 2);

    }

    @Test
    public void testMemberChannelSynchronizationOnContextInequivalence() throws EndpointException {

        Clock cm = new Clock();
        Clock cl = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        UpdateBucket mm = new UpdateBucket(member0, cm.tick(), 0, ol0);
        UpdateBucket ml = new UpdateBucket(member1, 0, cl.tick(), ol1);

        mm = channel.send(mm);
        ml = channel.receive(ml);

        assertEquals(c2, ml.getOperations()[0]);
        assertEquals(d2, ml.getOperations()[1]);
    }

    @Test
    public void testMemberChannelSynchronizationOnContextEquivalence() throws EndpointException {
        Clock cm = new Clock();
        Clock cl = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        UpdateBucket ml = new UpdateBucket(member0, cm.tick(), 0, ol0);
        UpdateBucket mm = new UpdateBucket(member1, cm.getTime(), cl.tick(), ol1);

        ml = channel.send(ml);
        mm = channel.receive(mm);

        assertEquals(c0, mm.getOperations()[0]);
        assertEquals(d0, mm.getOperations()[1]);
    }
}
