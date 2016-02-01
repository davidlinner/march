package org.march.sync.member;

import org.march.sync.Member;

/**
 * Created by dli on 31.01.2016.
 */
public interface ClosingListener extends Listener {
    void closing(Member member);
}
