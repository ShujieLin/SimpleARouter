package com.lsj.simplearouter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.lsj.arouter_annotations.ARouter;
import com.lsj.arouter_annotations.bean.RouterBean;
import com.lsj.arouter_api.ARouterPath;
import com.lsj.common.KLog;
import com.lsj.module_one.ModuleFirstActivity;
import com.lsj.module_second.ModuleSecondActivity;

import java.util.Map;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void gotoModuleFirst(View view) {
        Intent intent = new Intent(this, ModuleFirstActivity.class);
        startActivity(intent);
    }


    public void gotoModuleSecond(View view) {
//        Intent intent = new Intent(this, ModuleSecondActivity.class);
//        startActivity(intent);
        ARouter$Group$module_second moduleSecond = new ARouter$Group$module_second();
        //获取组
        Map<String, Class<? extends ARouterPath>> secondGroupMap = moduleSecond.getGroupMap();
        //获取存储路径的生成类
        Class<? extends ARouterPath> aClass = secondGroupMap.get("module_second");

        try {
            ARouterPath aRouterPath = aClass.newInstance();
            Map<String, RouterBean> pathMap = aRouterPath.getPathMap();
            RouterBean routerBean = pathMap.get("/module_second/ModuleSecondActivity");
            if (routerBean != null){
                KLog.d(routerBean.toString());
                Intent intent = new Intent(this,routerBean.getMyClass());
                startActivity(intent);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }


    }


}