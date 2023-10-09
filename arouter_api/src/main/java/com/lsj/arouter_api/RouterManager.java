package com.lsj.arouter_api;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.RequiresApi;

import com.lsj.arouter_annotations.bean.RouterBean;

/**
 * 用于管理路由
 *
 * @date: 2022/5/19
 * @author: linshujie
 */
public class RouterManager {
    private static final String TAG = "RouterManager";
    // 单例模式
    private static volatile RouterManager instance;//双重锁机制，赋予可见性，避免极端情况下的CPU重排列。
    private String path;
    private String group;

    private final static String FILE_GROUP_NAME = "ARouter$Group$";

    //缓存
    private LruCache<String, ARouterGroup> groupLruCache;
    private LruCache<String, ARouterPath> pathLruCache;

    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    public RouterManager() {
        groupLruCache = new LruCache<>(100);
        pathLruCache = new LruCache<>(100);
    }


    /**
     * 构建BundleManager
     * @param path
     * @return
     */
    public BundleManager build(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalArgumentException("注解的路径的开头必须为:'/' ，例如:/module/MainActivity");
        }

        if (path.lastIndexOf("/") == 0) {
            throw new IllegalArgumentException("组名和组件名中间需要使用'/'分隔，例如:/module/MainActivity");
        }

        //组名
        String finalGroup = path.substring(1, path.indexOf("/", 1)); // finalGroup = order

        if (TextUtils.isEmpty(finalGroup)) {
            throw new IllegalArgumentException("组名不能为空");
        }

        // TODO: 2022/5/19 更多的检查
        this.path =  path;
        this.group = finalGroup;

        return new BundleManager();
    }

    /**
     * 进行导航
     * @param context
     * @param bundleManager
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Object navigation(Context context, BundleManager bundleManager) {
        String groupClassName = context.getPackageName() + "." + FILE_GROUP_NAME + group;
        Log.v(TAG, "navigation: groupClassName = " + groupClassName);

        try {
            //读取路由组Group类文件
            ARouterGroup loadGroup = groupLruCache.get(group);
            if (null == loadGroup) {
                Class<?> aClass = Class.forName(groupClassName);
                loadGroup = (ARouterGroup) aClass.newInstance();
                // 保存到缓存
                groupLruCache.put(group, loadGroup);
            }

            if (loadGroup.getGroupMap().isEmpty()) {
                throw new RuntimeException("ARouterPath is null.");
            }

            //读取路由Path类文件
            ARouterPath loadPath = pathLruCache.get(path);
            if (null == loadPath) {
                Class<? extends ARouterPath> clazz = loadGroup.getGroupMap().get(group);
                //缓存路径
                loadPath = clazz.newInstance();
                pathLruCache.put(path, loadPath);
            }

            //跳转
            if (loadPath != null) {
                if (loadPath.getPathMap().isEmpty()) {
                    throw new RuntimeException("Map<String, RouterBean> is null");
                }

                //获取具体的存储到的信息
                RouterBean routerBean = loadPath.getPathMap().get(path);

                if (routerBean != null) {
                    switch (routerBean.getTypeEnum()) {
                        case ACTIVITY:
                            Intent intent = new Intent(context, routerBean.getMyClass());
                            intent.putExtras(bundleManager.getBundle()); // 携带参数
                            context.startActivity(intent, bundleManager.getBundle());
                            break;
                        // TODO: 2022/5/19 后续可以扩展，例如绑定fragment的跳转等
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
