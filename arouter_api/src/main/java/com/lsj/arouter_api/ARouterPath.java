package com.lsj.arouter_api;

import com.lsj.arouter_annotations.bean.RouterBean;

import java.util.Map;

/**
 * @description:
 * @date: 2022/4/14
 * @author: linshujie
 */
public interface ARouterPath {
    Map<String, RouterBean> getPathMap();
}