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
import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.UpdateBucket;
import org.march.sync.transform.Transformer;

public class SynchronizationTest {

    private UUID name1 = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private UUID name2 = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private Leader leader;
    private Member member1, member2;

    private StringConstant a = new StringConstant("A");
    private StringConstant b = new StringConstant("B");

    private BufferedBucketHandler<UpdateBucket> downLink1;
    private BufferedBucketHandler<UpdateBucket> downLink2;
    private BufferedBucketHandler<UpdateBucket> upLink1;
    private BufferedBucketHandler<UpdateBucket> upLink2;

    static Transformer TRANSFORMER = new Transformer();

    @Before
    public void setup() throws LeaderException, EndpointException, MemberException {
        leader = new Leader( TRANSFORMER);
        member1 = new Member(name1, TRANSFORMER);
        member2 = new Member(name2, TRANSFORMER);

        downLink1 = new BufferedBucketHandler<UpdateBucket>((member, bucket)-> { try { member1.update(bucket); } catch (Throwable e) {}});
        downLink2 = new BufferedBucketHandler<UpdateBucket>((member, bucket)-> { try { member2.update(bucket); } catch (Throwable e) {}});
        upLink1 = new BufferedBucketHandler<UpdateBucket>((member,bucket) ->{
            try {
                leader.update(bucket);
            } catch (LeaderException e) {
                e.printStackTrace();
            }
        });
        upLink2 = new BufferedBucketHandler<UpdateBucket>((member,bucket)->{
            try {
                leader.update(bucket);
            } catch (LeaderException e) {
                e.printStackTrace();
            }
        });

        leader.onBucket((member, bucket)->{
            if (member.equals(name1)){
                downLink1.handle(member, bucket);
            } else if (member.equals(name2)){
                downLink2.handle(member, bucket);
            }
        });

        member1.onBucket(upLink1);
        member2.onBucket(upLink2);

        leader.share(new Operation[0], (operation) -> {});

        member1.initialize(leader.register(name1));
        member2.initialize(leader.register(name2));
    }


    private void flushAll(){
        upLink1.flush();
        upLink2.flush();
        downLink1.flush();
        downLink2.flush();
    };

    @Test
    public void testOneWaySynchronization() throws EndpointException, MemberException, ObjectException, CommandException {
        member1.apply(null, new Set("a", a));

        flushAll();

        assertEquals(member2.find(null, "a"), a);
    }

    @Test
    public void testTwoWaySynchronizationNoConflict() throws EndpointException, MemberException, ObjectException, CommandException {
        member1.apply(null, new Set("a", a));
        member2.apply(null, new Set("b", b));

        flushAll();

        assertEquals(member2.find(null, "a"), a);
        assertEquals(member1.find(null, "b"), b);
    }

    @Test
    public void testTwoWaySynchronizationOnConflict() throws EndpointException, MemberException, ObjectException, CommandException {
        member1.apply(null, new Set("a", a));
        member2.apply(null, new Set("a", b));

        assertEquals(a, member1.find(null, "a"));
        assertEquals(b, member2.find(null, "a"));

        flushAll();

        assertEquals(a, member1.find(null, "a"));
        assertEquals(a, member2.find(null, "a"));
    }

}
