package org.march.sync;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.march.data.model.Operation;
import org.march.data.model.Pointer;
import org.march.data.model.StringConstant;
import org.march.data.model.Tools;
import org.march.data.command.Insert;
import org.march.sync.channel.ChangeSet;
import org.march.sync.backlog.BacklogException;
import org.march.sync.backlog.MasterBacklog;
import org.march.sync.transform.InsertInsertInclusion;
import org.march.sync.transform.Transformer;

public class MasterBacklogTest {

    private UUID replicaName0 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private UUID replicaName1 = UUID.fromString("11111111-1111-1111-1111-111111111111");

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

    private MasterBacklog masterBacklog;


    @Before
    public void setupContext() throws BacklogException {
        masterBacklog = new MasterBacklog(TRANSFORMER);
    }

    @Test
    public void testMasterBacklogSend() throws BacklogException {

        Clock clk = new Clock();

        List<Operation> ol0 = Tools.asList(a0, b0),
                        ol1 = Tools.asList(c0, d0);

        ChangeSet m0 = new ChangeSet(replicaName0, clk.tick(), 0, ol0);
        ChangeSet m1 = new ChangeSet(replicaName0, clk.tick(), 0, ol1);

        masterBacklog.append(m0);
        masterBacklog.append(m1);

        assertEquals(masterBacklog.getRemoteTime(), 0);
    }

    @Test
    public void testMasterBacklogReceive() throws BacklogException {

        Clock clk = new Clock();

        List<Operation> ol0 = Tools.asList(a0, b0),
                        ol1 = Tools.asList(c0, d0);

        ChangeSet m0 = new ChangeSet(replicaName0, 0, clk.tick(), ol0);
        ChangeSet m1 = new ChangeSet(replicaName0, 0, clk.tick(), ol1);

        masterBacklog.update(m0);
        masterBacklog.update(m1);

        assertEquals(masterBacklog.getRemoteTime(), 2);

    }

    @Test
    public void testMasterBacklogSynchronizationOnContextInequivalence() throws BacklogException {

        Clock cm = new Clock();
        Clock cl = new Clock();

        List<Operation> ol0 = Tools.asList(a0, b0),
                        ol1 = Tools.asList(c0, d0);

        ChangeSet mm = new ChangeSet(replicaName0, cm.tick(), 0, ol0);
        ChangeSet ml = new ChangeSet(replicaName1, 0, cl.tick(), ol1);

        masterBacklog.append(mm);
        ml = masterBacklog.update(ml);

        assertEquals(c2, ml.getOperations().get(0));
        assertEquals(d2, ml.getOperations().get(1));
    }

    @Test
    public void testMasterBacklogSynchronizationOnContextEquivalence() throws BacklogException {
        Clock cm = new Clock();
        Clock cl = new Clock();

        List<Operation> ol0 = Tools.asList(a0, b0),
                        ol1 = Tools.asList(c0, d0);

        ChangeSet ml = new ChangeSet(replicaName0, cm.tick(), 0, ol0);
        ChangeSet mm = new ChangeSet(replicaName1, cm.getTime(), cl.tick(), ol1);

        masterBacklog.append(ml);
        mm = masterBacklog.update(mm);

        assertEquals(c0, mm.getOperations().get(0));
        assertEquals(d0, mm.getOperations().get(1));
    }
}
