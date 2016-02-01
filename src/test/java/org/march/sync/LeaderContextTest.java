package org.march.sync;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.march.data.Operation;
import org.march.data.Pointer;
import org.march.data.StringConstant;
import org.march.data.command.Insert;
import org.march.sync.context.ContextException;
import org.march.sync.context.LeaderContext;
import org.march.sync.endpoint.UpdateBucket;
import org.march.sync.transform.InsertInsertInclusion;
import org.march.sync.transform.Transformer;

public class LeaderContextTest {

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

    private LeaderContext context;


    @Before
    public void setupContext() throws ContextException {
        context = new LeaderContext(TRANSFORMER);
    }

    @Test
    public void testLeaderContextSend() throws ContextException {

        Clock clk = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        UpdateBucket m0 = new UpdateBucket(member0, clk.tick(), 0, ol0);
        UpdateBucket m1 = new UpdateBucket(member0, clk.tick(), 0, ol1);

        context.include(m0);
        context.include(m1);

        assertEquals(context.getRemoteTime(), 0);
    }

    @Test
    public void testLeaderContextReceive() throws ContextException {

        Clock clk = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        UpdateBucket m0 = new UpdateBucket(member0, 0, clk.tick(), ol0);
        UpdateBucket m1 = new UpdateBucket(member0, 0, clk.tick(), ol1);

        context.adapt(m0);
        context.adapt(m1);

        assertEquals(context.getRemoteTime(), 2);

    }

    @Test
    public void testLeaderContextSynchronizationOnContextInequivalence() throws ContextException {

        Clock cm = new Clock();
        Clock cl = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        UpdateBucket mm = new UpdateBucket(member0, cm.tick(), 0, ol0);
        UpdateBucket ml = new UpdateBucket(member1, 0, cl.tick(), ol1);

        context.include(mm);
        ml = context.adapt(ml);

        assertEquals(c2, ml.getOperations()[0]);
        assertEquals(d2, ml.getOperations()[1]);
    }

    @Test
    public void testLeaderContextSynchronizationOnContextEquivalence() throws ContextException {
        Clock cm = new Clock();
        Clock cl = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        UpdateBucket ml = new UpdateBucket(member0, cm.tick(), 0, ol0);
        UpdateBucket mm = new UpdateBucket(member1, cm.getTime(), cl.tick(), ol1);

        context.include(ml);
        mm = context.adapt(mm);

        assertEquals(c0, mm.getOperations()[0]);
        assertEquals(d0, mm.getOperations()[1]);
    }
}
