package com.lsj.compiler.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @date: 2022/4/15
 * @author: linshujie
 */
public class ProcessorUtils {
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * 判空
     * @param coll
     * @return
     */
    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

}
