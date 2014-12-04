package org.march.sync;

import static org.junit.Assert.*;

import org.junit.Test;
import org.march.sync.Clock;
import org.march.sync.UncertainTemporalRelationException;

public class ClockTest {

    @Test
    public void testAfter() throws UncertainTemporalRelationException {
        assertTrue(Clock.after(1, 0));
        assertTrue(Clock.after(0, (int)Math.pow(2, 24)-1));        
    }
    
    @Test
    public void testBefore() throws UncertainTemporalRelationException {
        assertTrue(Clock.before(0, 1));
        assertTrue(Clock.before((int)Math.pow(2, 24)-1, 0));        
    }
    
    @Test(expected=UncertainTemporalRelationException.class)
    public void testUncertaintyHeading() throws UncertainTemporalRelationException {        
        Clock.after((int)Math.pow(2, 24) / 2, 0); 
    }
    
    @Test(expected=UncertainTemporalRelationException.class)
    public void testUncertaintyTrailing() throws UncertainTemporalRelationException {
        Clock.after(0,(int)Math.pow(2, 24) / 2); 
    }

    @Test
    public void testTick() throws UncertainTemporalRelationException {        
        Clock clock = new Clock(0);        
        assertEquals(clock.tick(), 1);                
        assertEquals(clock.adjust((int)Math.pow(2, 24) - 1), (int)Math.pow(2, 24)-1);        
        assertEquals(clock.tick(), 0);
    }

}
