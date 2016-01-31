package org.march.sync;

import org.march.sync.Member;

/**
 * Created by dli on 31.01.2016.
 */
public interface CloseListener extends Listener {
    void closed(Member member);
}
