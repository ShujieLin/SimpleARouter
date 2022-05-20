package com.lsj.arouter_api;

import android.app.Activity;
import android.util.LruCache;

/**
 * @date: 2022/5/19
 * @author: linshujie
 */
public class ParameterManager {
    private static ParameterManager instance;
    // private boolean isCallback;

    public static ParameterManager getInstance() {
        if (instance == null) {
            synchronized (ParameterManager.class) {
                if (instance == null) {
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    private LruCache<String, ParameterGet> cache;
    //后缀
    static final String FILE_SUFFIX_NAME = "$Parameter";

    ParameterManager(){
        cache = new LruCache<>(100);
    }

    /**
     *
     * @param activity
     */
    public void loadParameter(Activity activity) {
        String className = activity.getClass().getName();

        //提供缓存机制，提高性能
        ParameterGet parameterLoad = cache.get(className);
        if (null == parameterLoad) {
            try {
                //通过反射获取生成的参数类代码的对象
                Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
                parameterLoad = (ParameterGet) aClass.newInstance();
                cache.put(className, parameterLoad); // 保存到缓存
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        parameterLoad.getParameter(activity); // 最终的执行  会执行我们生成的类
    }
}
