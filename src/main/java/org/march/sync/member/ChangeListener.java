package org.march.sync.member;

import org.march.data.Operation;
import org.march.sync.Member;

/**
 * Created by dli on 31.01.2016.
 */
public interface ChangeListener extends Listener {
    void changed(Member member, Operation... operations);
}
