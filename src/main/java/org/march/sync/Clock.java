package org.march.sync;

/**
 * Simple clock implementation that overflows after 2^16 ticks. 
 * @author dli
 *
 */
public class Clock {
    
    private final static int LOGICAL_HOUR = (int)Math.pow(2, 24);
    
    private final static int CERTAINTY_MARGIN = LOGICAL_HOUR / 100;
            
    private int time;
            
    public Clock() {
        this(0);
    }
    
    public Clock(int time) {
        this.time = time;
    }

    public int tick(){
        return this.time < LOGICAL_HOUR - 1 ? ++this.time : (this.time = 0);
    }
    
    public int adjust(int time){        
        return this.time = time; 
    }        

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
    
    public static boolean after(int t1, int t2) throws UncertainTemporalRelationException{                
                
        if(t1 > t2){
            int trailing  = t1 - t2,
                heading   = LOGICAL_HOUR - t1 + t2;
            
            if(Math.min(trailing, heading) > CERTAINTY_MARGIN) throw new UncertainTemporalRelationException();
            
            return trailing < heading;
        } else if (t1 < t2){
            int trailing  = t2 - t1,
                heading   = LOGICAL_HOUR - t2 + t1;
                
            if(Math.min(trailing, heading) > CERTAINTY_MARGIN) throw new UncertainTemporalRelationException();
                
            return heading < trailing;
        } 
        
        return false;
    }
    
    public static boolean before(int t1, int t2) throws UncertainTemporalRelationException{
        return t1 == t2 ? false : !after(t1, t2);
    }
    
    public static boolean equal(int t1, int t2) throws UncertainTemporalRelationException{
        return t1 == t2;
    }
        
}
