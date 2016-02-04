package org.march.sync;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.march.data.Operation;
import org.march.data.Pointer;
import org.march.data.StringConstant;
import org.march.data.command.Insert;
import org.march.sync.channel.ChangeSet;
import org.march.sync.context.BacklogException;
import org.march.sync.context.MasterBacklog;
import org.march.sync.transform.InsertInsertInclusion;
import org.march.sync.transform.Transformer;

public class MasterBacklogTest {

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

    private MasterBacklog context;


    @Before
    public void setupContext() throws BacklogException {
        context = new MasterBacklog(TRANSFORMER);
    }

    @Test
    public void testLeaderContextSend() throws BacklogException {

        Clock clk = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        ChangeSet m0 = new ChangeSet(member0, clk.tick(), 0, ol0);
        ChangeSet m1 = new ChangeSet(member0, clk.tick(), 0, ol1);

        context.append(m0);
        context.append(m1);

        assertEquals(context.getRemoteTime(), 0);
    }

    @Test
    public void testLeaderContextReceive() throws BacklogException {

        Clock clk = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        ChangeSet m0 = new ChangeSet(member0, 0, clk.tick(), ol0);
        ChangeSet m1 = new ChangeSet(member0, 0, clk.tick(), ol1);

        context.update(m0);
        context.update(m1);

        assertEquals(context.getRemoteTime(), 2);

    }

    @Test
    public void testLeaderContextSynchronizationOnContextInequivalence() throws BacklogException {

        Clock cm = new Clock();
        Clock cl = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        ChangeSet mm = new ChangeSet(member0, cm.tick(), 0, ol0);
        ChangeSet ml = new ChangeSet(member1, 0, cl.tick(), ol1);

        context.append(mm);
        ml = context.update(ml);

        assertEquals(c2, ml.getOperations()[0]);
        assertEquals(d2, ml.getOperations()[1]);
    }

    @Test
    public void testLeaderContextSynchronizationOnContextEquivalence() throws BacklogException {
        Clock cm = new Clock();
        Clock cl = new Clock();

        Operation[] ol0 = new Operation[]{a0, b0},
                    ol1 = new Operation[]{c0, d0};

        ChangeSet ml = new ChangeSet(member0, cm.tick(), 0, ol0);
        ChangeSet mm = new ChangeSet(member1, cm.getTime(), cl.tick(), ol1);

        context.append(ml);
        mm = context.update(mm);

        assertEquals(c0, mm.getOperations()[0]);
        assertEquals(d0, mm.getOperations()[1]);
    }
}
