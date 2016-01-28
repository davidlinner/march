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
import org.march.sync.endpoint.EndpointException;
import org.march.sync.transform.Transformer;

public class SynchronizationTest {

    private UUID name1 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private UUID name2 = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private Leader leader;
    private Member member1, member2;

    private StringConstant a = new StringConstant("A");
    private StringConstant b = new StringConstant("B");

    private LazyBucketHandler lazyHandler;

    static Transformer TRANSFORMER = new Transformer();

    @Before
    public void setup() throws LeaderException, EndpointException{
        leader = new Leader( TRANSFORMER);
        member1 = new Member(name1, TRANSFORMER);
        member2 = new Member(name2, TRANSFORMER);

        lazyHandler = new LazyBucketHandler((member, bucket)->{
            try {
                if (member.equals(name1)){
                    member1.update(bucket);
                } else if (member.equals(name2)){
                    member2.update(bucket);
                }
            } catch (MemberException e) {
                e.printStackTrace();
            }
        });

        leader.onBucket(lazyHandler);

        member1.onBucket((member,bucket)->{
            try {
                leader.update(bucket);
            } catch (LeaderException e) {
                e.printStackTrace();
            }
        });

        member2.onBucket((member,bucket)->{
            try {
                leader.update(bucket);
            } catch (LeaderException e) {
                e.printStackTrace();
            }
        });

        leader.share(new Operation[0], (operation)->{});

        leader.register(name1);
        leader.register(name2);
    }


    @Test
    public void testOneWaySynchronization() throws EndpointException, MemberException, ObjectException, CommandException {
        member1.apply(null, new Set("a", a));

        lazyHandler.flush();

        assertEquals(member2.find(null, "a"), a);
    }

    @Test
    public void testTwoWaySynchronizationNoConflict() throws EndpointException, MemberException, ObjectException, CommandException {
        member1.apply(null, new Set("a", a));
        member2.apply(null, new Set("b", b));


        lazyHandler.flush();

        assertEquals(member2.find(null, "a"), a);
        assertEquals(member1.find(null, "b"), b);
    }

    @Test
    public void testTwoWaySynchronizationOnConflict() throws EndpointException, MemberException, ObjectException, CommandException {
        member1.apply(null, new Set("a", a));
        member2.apply(null, new Set("a", b));

        assertEquals(a, member1.find(null, "a"));
        assertEquals(b, member2.find(null, "a"));

        lazyHandler.flush();

        assertEquals(a, member1.find(null, "a"));
        assertEquals(a, member2.find(null, "a"));
    }

}
