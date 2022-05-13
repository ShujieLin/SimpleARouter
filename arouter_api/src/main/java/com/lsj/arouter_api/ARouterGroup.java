package com.lsj.arouter_api;

import java.util.Map;

/**
 * @date: 2022/4/14
 * @author: linshujie
 */
public interface ARouterGroup {
    Map<String,Class<? extends ARouterPath>> getGroupMap();
}
