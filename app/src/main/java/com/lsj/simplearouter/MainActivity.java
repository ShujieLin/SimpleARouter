package com.lsj.simplearouter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.lsj.arouter_annotations.ARouter;
import com.lsj.arouter_api.RouterManager;
import com.lsj.module_one.ModuleFirstActivity;

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
        /*
        一： 原生跳转
        Intent intent = new Intent(this, ModuleSecondActivity.class);
        startActivity(intent);
        **/

        /*
        二：通过生成的代码跳转
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
        **/

        /*
        三：封装为库
         */
        RouterManager.getInstance()
                .build("/module_second/ModuleSecondActivity")
                .withString("name", "helloworld！")
                .withInt("testInt",520)
                .withBoolean("testBoolean",true)
                .navigation(this);
    }
}