package org.march.sync.member;

import org.march.sync.Member;

/**
 * Created by dli on 31.01.2016.
 */
public interface OpenListener extends Listener {
    void opened(Member member);
}
